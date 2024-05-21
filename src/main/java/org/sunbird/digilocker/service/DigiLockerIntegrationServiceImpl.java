package org.sunbird.digilocker.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.ContentService;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.digilocker.model.*;
import org.sunbird.user.service.UserUtilityService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DigiLockerIntegrationServiceImpl implements DigiLockerIntegrationService {

    @Autowired
    UserUtilityService userUtilityService;

    @Autowired
    CassandraOperation cassandraOperation;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    @Autowired
    CbExtServerProperties serverProperties;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ContentService contentService;
    private Logger logger = LoggerFactory.getLogger(getClass().getName());
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    @Override
    public PullURIResponse generateURIResponse(String digiLockerHmac, String requestBody) {
        PullURIResponse response = new PullURIResponse();
        ResponseStatus responseStatus = response.getResponseStatus();
        responseStatus.setTs(dateFormat.format(new Date()));
        if (!validateRequest(digiLockerHmac, requestBody)) {
            responseStatus.setStatus("0");
            response.getDocDetails().setDocContent("Document is not available.");
            logger.error("Not able to validate the request for hmac: " + digiLockerHmac + " requestBody:" + requestBody);
            return response;
        }
        PullURIRequest request = null;
        try {
            objectMapper = new XmlMapper();
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            request = objectMapper.readValue(requestBody, PullURIRequest.class);
        } catch (IOException e) {
            logger.error("Not able to process the request requestBody: " + requestBody, e);
            responseStatus.setStatus("0");
            response.getDocDetails().setDocContent("Document is not available.");
            return response;
        }
        CertificateAddInfoDTO certificateAddInfoDTO = new CertificateAddInfoDTO();
        try {
            responseStatus.setTxn(request.getTxn());
            Map<String, Object> getUserInfo = userUtilityService.getUserDetails(Constants.PHONE, request.getDocDetails().getMobile());
            String certificateAccessCode = request.getDocDetails().getCertificateAccessCode();
            if (MapUtils.isNotEmpty(getUserInfo) && StringUtils.isNotBlank(certificateAccessCode)) {
                Map<String, Object> profileDetails = (Map<String, Object>) getUserInfo.get(Constants.PROFILE_DETAILS);
                String userId = (String) getUserInfo.get(Constants.IDENTIFIER);
                Map<String, Object> personalDetails = new HashMap<>();
                if (MapUtils.isNotEmpty(profileDetails)) {
                    personalDetails = (Map<String, Object>) profileDetails.get(Constants.PERSONAL_DETAILS);
                } else {
                    logger.error("Profile details is not available for user: " + userId);
                }
                if (StringUtils.isNotBlank(userId)) {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put(Constants.USER_ID, userId);
                    List<Map<String, Object>> userEnrollmentInfo = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                            Constants.KEYSPACE_SUNBIRD_COURSES, Constants.TABLE_USER_ENROLMENT, userInfo, Arrays.asList(Constants.USER_ID, Constants.COURSE_ID, Constants.ISSUED_CERTIFICATES));
                    if (CollectionUtils.isNotEmpty(userEnrollmentInfo)) {
                        userEnrollmentInfo = userEnrollmentInfo.stream()
                                .filter(enrollment -> {
                                    List<Map<String, Object>> issuedCertificates = (List<Map<String, Object>>) enrollment.get(Constants.ISSUED_USER_CERTIFICATE);
                                    return CollectionUtils.isNotEmpty(issuedCertificates) &&
                                            issuedCertificates.stream()
                                                    .map(cert -> certificateAccessCode.equalsIgnoreCase((String) cert.get("token")))
                                                    .anyMatch(Boolean.TRUE::equals);
                                }).collect(Collectors.toList());
                        if ((CollectionUtils.isEmpty(userEnrollmentInfo)) || (CollectionUtils.isNotEmpty(userEnrollmentInfo) && userEnrollmentInfo.size() > 1)) {
                            logger.error("Issue with getting the userEnrollment List" + userEnrollmentInfo);
                            response.setResponseStatus(new ResponseStatus("0", dateFormat.format(new Date()), request.getTxn()));
                            response.getDocDetails().setDocContent("Document is not available.");
                            return response;
                        } else {
                            URIResponseDocDetails docDetails = response.getDocDetails();
                            IssuedTo issuedTo = docDetails.getIssuedTo();
                            Persons persons = issuedTo.getPersons();
                            Person person = persons.getPerson();

                            person.setName(request.getDocDetails().getFullName());
                            if (MapUtils.isNotEmpty(personalDetails)) {
                                person.setDob((String) personalDetails.get(Constants.DOB));
                                person.setGender((String) personalDetails.get(Constants.GENDER));
                            } else {
                                responseStatus.setStatus("0");
                                logger.error("Personal details is not available for user: " + userId);
                            }
                            person.setPhone(request.getDocDetails().getMobile());
                            persons.setPerson(person);
                            issuedTo.setPersons(persons);
                            docDetails.setIssuedTo(issuedTo);
                            String docId = request.getDocDetails().getCertificateAccessCode() + StringUtils.substring(userId, 0, 4);
                            Map<String, Object> dockerLookUpInfo = new HashMap<>();
                            dockerLookUpInfo.put(Constants.DOC_ID, docId);
                            dockerLookUpInfo.put(Constants.DIGI_LOCKER_ID, request.getDocDetails().getDigiLockerId());
                            dockerLookUpInfo.put(Constants.USER_ID, userId);
                            dockerLookUpInfo.put(Constants.ACCESS_TOKEN, certificateAccessCode);
                            List<Map<String, Object>> userEnrollment = (List<Map<String, Object>>) userEnrollmentInfo.get(0).get(Constants.ISSUED_USER_CERTIFICATE);
                            userEnrollment = userEnrollment.stream().filter(enroll -> ((String) enroll.get("token")).equalsIgnoreCase(certificateAccessCode)).collect(Collectors.toList());
                            dockerLookUpInfo.put(Constants.CERTIFICATE_ID, userEnrollment.get(0).get(Constants.IDENTIFIER));
                            try {
                                dockerLookUpInfo.put(Constants.LAST_ISSUED_ON, dateFormat.parse((String) userEnrollment.get(0).get(Constants.LAST_ISSUED_ON)));
                            } catch (ParseException e) {
                                responseStatus.setStatus("0");
                                logger.error("Not able to parse date");
                            }
                            dockerLookUpInfo.put(Constants.COURSE_ID, userEnrollmentInfo.get(0).get(Constants.COURSE_ID));
                            dockerLookUpInfo.put(Constants.CERTIFICATE_NAME, userEnrollment.get(0).get(Constants.NAME));
                            dockerLookUpInfo.put(Constants.CREATED_DATE, new Date());
                            dockerLookUpInfo.put(Constants.DOC_TYPE, request.getDocDetails().getDocType());
                            if (addUpdateDigiLockerLookup(dockerLookUpInfo)) {
                                String content = getCertificate((String) dockerLookUpInfo.get(Constants.CERTIFICATE_ID));
                                if (StringUtils.isNotEmpty(content)) {
                                    docDetails.setDocContent(content.toString());
                                    certificateAddInfoDTO.setCertificateId(certificateAccessCode);
                                    certificateAddInfoDTO.setDocumentInfo(request.getDocDetails().getDocType());
                                    certificateAddInfoDTO.setCertificateName((String)dockerLookUpInfo.get(Constants.CERTIFICATE_NAME));
                                    certificateAddInfoDTO.setDocumentName(DocumentType.getValueForKey(request.getDocDetails().getDocType()));
                                    certificateAddInfoDTO.setCertificateIssueOn(simpleDateFormat.format((Date)dockerLookUpInfo.get(Constants.LAST_ISSUED_ON)));
                                    certificateAddInfoDTO.setUserName((String)getUserInfo.get(Constants.FIRSTNAME));
                                    certificateAddInfoDTO.setSwd((String)getUserInfo.get(Constants.CHANNEL));
                                    certificateAddInfoDTO.setSwdIndicator(String.valueOf(((String)getUserInfo.get(Constants.CHANNEL)).charAt(0)));
                                    docDetails.setDataContent(encodeBytesToBase64((convertObjectToJsonBytes(addCertificateInfo(certificateAddInfoDTO)))));
                                    docDetails.setURI(serverProperties.getDigiLockerIssuerId() + "-" + request.getDocDetails().getDocType() + "-" + docId);
                                    responseStatus.setStatus("1");
                                    Map<String, Object> contentResponse = contentService.readContentFromCache((String)dockerLookUpInfo.get(Constants.COURSE_ID), Arrays.asList(Constants.NAME));
                                    if (MapUtils.isNotEmpty(contentResponse)) {
                                        docDetails.setDocDescription((String) contentResponse.get(Constants.NAME));
                                    }
                                } else {
                                    logger.error("Not able to generate Pdf certificate for URI: " + docDetails.getURI());
                                    responseStatus.setStatus("0");
                                }
                                response.setDocDetails(docDetails);
                            } else {
                                logger.error("Error while updating the document lookup table for userId: " + userId + " for request: " + request);
                                responseStatus.setStatus("0");
                            }
                        }
                    } else {
                        responseStatus.setStatus("0");
                        logger.error("Enrollment list is empty for userId: " + userId + " for request: " + request);
                    }
                } else {
                    responseStatus.setStatus("0");
                    logger.error("Error while getting the userId for request:" + request);
                }
            } else {
                logger.error("Error while getting the user Info request:" + request);
                responseStatus.setStatus("0");
            }
        } catch (Exception e) {
            logger.error("Some exception while processing the request: ", e);
            responseStatus.setStatus("0");
        }
        if (responseStatus.getStatus().equals("0")) {
            response.getDocDetails().setDocContent("Document is not available.");
        }
        response.setResponseStatus(responseStatus);
        return response;
    }

    @Override
    public PullDocResponse generateDocResponse(String digiLockerHmac, String requestBody) {
        PullDocResponse response = new PullDocResponse();
        ResponseStatus responseStatus = response.getResponseStatus();
        if (!validateRequest(digiLockerHmac, requestBody)) {
            responseStatus.setStatus("0");
            response.getDocDetails().setDocContent("Document is not available.");
            logger.error("Not able to validate the request for hmac: " + digiLockerHmac + " requestBody:" + requestBody);
            return response;
        }
        PullDocRequest request = null;
        try {
            objectMapper = new XmlMapper();
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            request = objectMapper.readValue(requestBody, PullDocRequest.class);
        } catch (IOException e) {
            logger.error("Not able to process the request requestBody: " + requestBody, e);
            responseStatus.setStatus("0");
            response.getDocDetails().setDocContent("Document is not available.");
            return response;
        }
        CertificateAddInfoDTO certificateAddInfoDTO = new CertificateAddInfoDTO();
        try {
            DocResponseDetails docDetails = response.getDocDetails();
            responseStatus.setTs(dateFormat.format(new Date()));
            responseStatus.setTxn(request.getTxn());
            String[] uri = request.getDocDetails().getURI().split("-");
            Map<String, Object> digiLockerDocInfo = getDigiLockerDocInfo(uri[2], request.getDocDetails().getDigiLockerId());
            if (MapUtils.isNotEmpty(digiLockerDocInfo)) {
                String content = getCertificate((String) digiLockerDocInfo.get(Constants.CERTIFICATE_ID));
                if (StringUtils.isNotEmpty(content)) {
                    docDetails.setDocContent(content.toString());
                    Map<String, Object> getUserInfo = userUtilityService.getUserDetails(Constants.IDENTIFIER, (String)digiLockerDocInfo.get(Constants.USER_ID));
                    if (MapUtils.isNotEmpty(getUserInfo)) {
                        certificateAddInfoDTO.setCertificateId((String)digiLockerDocInfo.get(Constants.ACCESS_TOKEN));
                        certificateAddInfoDTO.setDocumentInfo(uri[1]);
                        certificateAddInfoDTO.setCertificateName((String)digiLockerDocInfo.get(Constants.CERTIFICATE_NAME));
                        certificateAddInfoDTO.setDocumentName(DocumentType.getValueForKey(uri[1]));
                        certificateAddInfoDTO.setCertificateIssueOn(simpleDateFormat.format((Date)digiLockerDocInfo.get(Constants.LAST_ISSUED_ON)));
                        certificateAddInfoDTO.setUserName((String)getUserInfo.get(Constants.FIRSTNAME));
                        certificateAddInfoDTO.setSwd((String)getUserInfo.get(Constants.CHANNEL));
                        certificateAddInfoDTO.setSwdIndicator(String.valueOf(((String)getUserInfo.get(Constants.CHANNEL)).charAt(0)));
                        docDetails.setDataContent(encodeBytesToBase64((convertObjectToJsonBytes(addCertificateInfo(certificateAddInfoDTO)))));
                        response.setDocDetails(docDetails);
                        Map<String, Object> contentResponse = contentService.readContentFromCache((String)digiLockerDocInfo.get(Constants.COURSE_ID), Arrays.asList(Constants.NAME));
                        if (MapUtils.isNotEmpty(contentResponse)) {
                            docDetails.setDocDescription((String) contentResponse.get(Constants.NAME));
                        }
                        responseStatus.setStatus("1");
                    } else {
                        logger.error("Error while getting the user Info request:" + request);
                        responseStatus.setStatus("0");
                    }
                } else {
                    logger.error("Not able to generate Pdf certificate for URI: " + request.getDocDetails().getURI());
                    responseStatus.setStatus("0");
                }
            } else {
                logger.error("Not able to find info at lookup for URI: " + request.getDocDetails().getURI());
                responseStatus.setStatus("0");
            }
        } catch (Exception e) {
            logger.error("Some exception while processing the request: ", e);
            responseStatus.setStatus("0");
        }
        if (responseStatus.getStatus().equals("0")) {
            response.getDocDetails().setDocContent("Document is not available.");
        }
        response.setResponseStatus(responseStatus);
        return response;
    }

    private String getCertificate(String certificateId) {
        Map<String, Object> response = (Map<String, Object>) outboundRequestHandlerService.fetchResult(serverProperties.getCertRegistryServiceBaseUrl() +
                serverProperties.getCertRegistryCertificateDownloadUrl() + certificateId);
        if (null != response && Constants.OK.equalsIgnoreCase((String) response.get(Constants.RESPONSE_CODE))) {
            Map<String, Object> certificateInfo = (Map<String, Object>) response.get(Constants.RESULT);
            if (MapUtils.isNotEmpty(certificateInfo)) {
                byte[] out = null;
                try {
                    out = generatePdfFromSvg((String) certificateInfo.get("printUri"));
                    if (out != null)
                        return encodeBytesToBase64(out);
                    else {
                        logger.error("Issue while finding the certificate and generate the bytes for certificateId: " + certificateId);
                    }
                } catch (Exception e) {
                    logger.error("Issue while finding the certificate", e);
                }
            }
        }
        return null;
    }

    private String encodeBytesToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public byte[] generatePdfFromSvg(String svgContent) throws IOException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = new HashMap<>();
            request.put("inputFormat", "svg");
            request.put("outputFormat", "pdf");
            request.put("printUri", svgContent);

            HttpEntity<Object> entity = new HttpEntity<>(request, headers);
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(
                    serverProperties.getPdfGeneratorServiceBaseUrl() + serverProperties.getPdfGeneratorSvgToPdfUrl(),
                    HttpMethod.POST,
                    entity,
                    byte[].class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity.getBody();
            } else {
                logger.error("Issue while get the data and response is: ", responseEntity);
            }
        } catch (Exception e) {
            logger.error("Issue while get the data from pdf generator repo", e);
        }
        return null;
    }

    private boolean addUpdateDigiLockerLookup(Map<String, Object> userDocInfo) {
        Map<String, Object> digiLockerInfoMap = getDigiLockerDocInfo((String) userDocInfo.get(Constants.DOC_ID), (String) userDocInfo.get(Constants.DIGI_LOCKER_ID));
        boolean isUpdatedDigiLockerTable = false;
        if (MapUtils.isEmpty(digiLockerInfoMap)) {
            SBApiResponse resp = cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_DIGILOCKER_DOC_INFO, userDocInfo);
            if (!resp.get(Constants.RESPONSE).equals(Constants.SUCCESS)) {
                isUpdatedDigiLockerTable = false;
            } else {
                isUpdatedDigiLockerTable = true;
            }
        } else {
            isUpdatedDigiLockerTable = true;
        }
        return isUpdatedDigiLockerTable;
    }

    private Map<String, Object> getDigiLockerDocInfo(String docId, String digiLockerId) {
        Map<String, Object> digiLockerInfo = new HashMap<>();
        digiLockerInfo.put(Constants.DOC_ID, docId);
        digiLockerInfo.put(Constants.DIGI_LOCKER_ID, digiLockerId);
        List<Map<String, Object>> digiLockerInfoMap = cassandraOperation.getRecordsByPropertiesWithoutFiltering(Constants.KEYSPACE_SUNBIRD,
                Constants.TABLE_DIGILOCKER_DOC_INFO, digiLockerInfo, null);
        if (CollectionUtils.isNotEmpty(digiLockerInfoMap)) {
            return digiLockerInfoMap.get(0);
        }
        return null;
    }

    private CertificateInfo addCertificateInfo(CertificateAddInfoDTO certificateAddInfoDTO) throws ParseException {
        CertificateInfo certificateInfo = new CertificateInfo();
        certificateInfo.getCertificateMetaData().setName(certificateAddInfoDTO.getDocumentName());
        certificateInfo.getCertificateMetaData().setNumber(certificateAddInfoDTO.getCertificateId());
        certificateInfo.getCertificateMetaData().setIssueDate(certificateAddInfoDTO.getCertificateIssueOn());
        certificateInfo.getCertificateMetaData().setType(certificateAddInfoDTO.getDocumentInfo());
        certificateInfo.getCertificateMetaData().getCertificateData().getCertificate().setNumber(certificateAddInfoDTO.getCertificateId());
        certificateInfo.getCertificateMetaData().getIssuedTo().getPersonInfo().setName(certificateAddInfoDTO.getUserName());
        certificateInfo.getCertificateMetaData().getIssuedTo().getPersonInfo().setSwd(certificateAddInfoDTO.getSwd());
        certificateInfo.getCertificateMetaData().getIssuedTo().getPersonInfo().setSwdIndicator(certificateAddInfoDTO.getSwdIndicator());
        certificateInfo.getCertificateMetaData().getIssuedTo().getPersonInfo().setName(certificateAddInfoDTO.getUserName());
        certificateInfo.getCertificateMetaData().getCertificateData().getCertificate().setPlace("iGOT");
        certificateInfo.getCertificateMetaData().getCertificateData().getCertificate().setDate(certificateAddInfoDTO.getCertificateIssueOn());
        return certificateInfo;
    }

    public byte[] convertObjectToJsonBytes(Object object) throws Exception {
        return objectMapper.writeValueAsBytes(object);
    }

    private boolean validateRequest(String digiLockerHmac, String request) {
        logger.debug("The digiLocker Hmac shared: " + digiLockerHmac);
        String hmacRequestValue = calculateHMACSHA256(request, serverProperties.getDigiLockerAPIKey());
        logger.debug("The hmacRequestValue is: "+ hmacRequestValue);
        if (hmacRequestValue.equals(digiLockerHmac)) {
            return true;
        }
        return false;
    }

    private String calculateHMACSHA256(String requestBody, String apiKey) {
        try {
            logger.debug("The API key is: " + apiKey);
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(apiKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hmacBytes = sha256Hmac.doFinal(requestBody.getBytes());

            // Convert bytes to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            byte[] base64EncodedHmacBytes = Base64.getEncoder().encode(hexString.toString().getBytes(StandardCharsets.UTF_8));
            return new String(base64EncodedHmacBytes);
        } catch (NoSuchAlgorithmException ex) {
            logger.error("Not able to convert SHA256: ", ex);
        } catch (InvalidKeyException e) {
            logger.error("Invalid Key for converting: ", e);
        }
        return "";
    }
}

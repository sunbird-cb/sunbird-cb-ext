package org.sunbird.digilocker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fop.svg.PDFTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.digilocker.model.*;
import org.sunbird.user.service.UserUtilityService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    @Autowired
    CbExtServerProperties serverProperties;

    private Logger logger = LoggerFactory.getLogger(getClass().getName());
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Override
    public PullURIResponse generateURIResponse(PullURIRequest request) {
        PullURIResponse response = new PullURIResponse();
        ResponseStatus responseStatus = response.getResponseStatus();
        CertificateAddInfoDTO certificateAddInfoDTO = new CertificateAddInfoDTO();
        try {
            responseStatus.setTs(dateFormat.format(new Date()));
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
                            docDetails.setURI(serverProperties.getDigiLockerIssuerId() + "-" + request.getDocDetails().getDocType() + "-" + docId);
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
                                    certificateAddInfoDTO.setCertificateId((String)dockerLookUpInfo.get(Constants.CERTIFICATE_ID));
                                    certificateAddInfoDTO.setDocumentInfo(request.getDocDetails().getDocType());
                                    certificateAddInfoDTO.setCertificateName((String)dockerLookUpInfo.get(Constants.CERTIFICATE_NAME));
                                    certificateAddInfoDTO.setDocumentName(DocumentType.getValueForKey(request.getDocDetails().getDocType()));
                                    certificateAddInfoDTO.setCertificateIssueOn((Date)dockerLookUpInfo.get(Constants.LAST_ISSUED_ON));
                                    docDetails.setDataContent(encodeBytesToBase64((convertObjectToJsonBytes(addCertificateInfo(certificateAddInfoDTO)))));
                                } else {
                                    logger.error("Not able to generate Pdf certificate for URI: " + docDetails.getURI());
                                    responseStatus.setStatus("0");
                                }
                                responseStatus.setStatus("1");
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
        response.setResponseStatus(responseStatus);
        return response;
    }

    @Override
    public PullDocResponse generateDocResponse(PullDocRequest request) {
        PullDocResponse response = new PullDocResponse();
        ResponseStatus responseStatus = response.getResponseStatus();
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
                    responseStatus.setStatus("1");
                } else {
                    logger.error("Not able to generate Pdf certificate for URI: " + request.getDocDetails().getURI());
                    responseStatus.setStatus("0");
                }
                certificateAddInfoDTO.setCertificateId((String)digiLockerDocInfo.get(Constants.CERTIFICATE_ID));
                certificateAddInfoDTO.setDocumentInfo(uri[1]);
                certificateAddInfoDTO.setCertificateName((String)digiLockerDocInfo.get(Constants.CERTIFICATE_NAME));
                certificateAddInfoDTO.setDocumentName(DocumentType.getValueForKey(uri[1]));
                certificateAddInfoDTO.setCertificateIssueOn((Date)digiLockerDocInfo.get(Constants.LAST_ISSUED_ON));
                docDetails.setDataContent(encodeBytesToBase64((convertObjectToJsonBytes(addCertificateInfo(certificateAddInfoDTO)))));
                response.setDocDetails(docDetails);
            } else {
                logger.error("Not able to find info at lookup for URI: " + request.getDocDetails().getURI());
                responseStatus.setStatus("0");
            }
        } catch (Exception e) {
            logger.error("Some exception while processing the request: ", e);
            responseStatus.setStatus("0");
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
                    out = generatePdfFromSvg(decodeUrl((String) certificateInfo.get("printUri")));
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

    public byte[] generatePdfFromSvg(String svgContent) throws IOException, TranscoderException {
        String svgContentDecode = svgContent.replace("data:image/svg+xml,", "");
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(svgContentDecode.getBytes()));
            TranscoderOutput output = new TranscoderOutput(outputStream);
            PDFTranscoder transcoder = new PDFTranscoder();
            transcoder.transcode(input, output);
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Error while generating pdf from svg: ", e);
        }
        return null;
    }

    private static String decodeUrl(String encodedUrl) throws UnsupportedEncodingException {
        StringBuilder decodedUrl = new StringBuilder();
        int index = 0;
        while (index < encodedUrl.length()) {
            char currentChar = encodedUrl.charAt(index);
            if (currentChar == '%') {
                String hex = encodedUrl.substring(index + 1, index + 3);
                decodedUrl.append((char) Integer.parseInt(hex, 16));
                index += 3;
            } else {
                decodedUrl.append(currentChar);
                index++;
            }
        }
        return decodedUrl.toString();
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

    private CertificateInfo addCertificateInfo(CertificateAddInfoDTO certificateAddInfoDTO) {
        CertificateInfo certificateInfo = new CertificateInfo();
        certificateInfo.getCertificateMetaData().setName(certificateAddInfoDTO.getDocumentName());
        certificateInfo.getCertificateMetaData().setNumber(certificateAddInfoDTO.getCertificateId());
        certificateInfo.getCertificateMetaData().setIssueDate(certificateAddInfoDTO.getCertificateIssueOn());
        certificateInfo.getCertificateMetaData().setType(certificateAddInfoDTO.getDocumentInfo());
        certificateInfo.getCertificateMetaData().getCertificateData().getCertificate().setNumber(certificateAddInfoDTO.getCertificateId());
        return certificateInfo;
    }

    public static byte[] convertObjectToJsonBytes(Object object) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsBytes(object);
    }
}

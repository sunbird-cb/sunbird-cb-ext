package org.sunbird.digilocker.service;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fop.svg.PDFTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.digilocker.model.PullDocRequest;
import org.sunbird.digilocker.model.PullDocResponse;
import org.sunbird.digilocker.model.PullURIRequest;
import org.sunbird.digilocker.model.PullURIResponse;
import org.sunbird.user.service.UserUtilityService;

import java.io.*;
import java.util.*;

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

    @Override
    public PullURIResponse generateURIResponse(PullURIRequest request) {
        PullURIResponse response = new PullURIResponse();
        if (userUtilityService.isUserExist(Constants.PHONE, request.getDocDetails().getMobile())) {
            response.setResponseStatus(new PullURIResponse.ResponseStatus("1", new Date().toString(), request.getTxn()));
            PullURIResponse.DocDetails docDetails = new PullURIResponse.DocDetails();
            PullURIResponse.IssuedTo issuedTo = new PullURIResponse.IssuedTo();
            PullURIResponse.Persons persons = new PullURIResponse.Persons();
            PullURIResponse.Person person = new PullURIResponse.Person();
            person.setName(request.getDocDetails().getFullName());
            person.setDob("31-12-1990");
            person.setGender("Male");
            person.setPhone(request.getDocDetails().getMobile());
            persons.setPerson(person);
            issuedTo.setPersons(persons);
            docDetails.setIssuedTo(issuedTo);
            docDetails.setURI("in.gov.karamyogi-" + request.getDocDetails().getDocType() + "-" + request.getDocDetails().getCertificateAccessCode());
            response.setDocDetails(docDetails);
        } else {
            response.setResponseStatus(new PullURIResponse.ResponseStatus("0", new Date().toString(), request.getTxn()));
        }
        return response;
    }

    @Override
    public PullDocResponse generateDocResponse(PullDocRequest request) {
        PullDocResponse response = new PullDocResponse();

        PullDocResponse.DocDetails docDetails = new PullDocResponse.DocDetails();
        String[] uri = request.getDocDetails().getURI().split("-");
        String content = getCertificate((String)getCertRecord(uri[2]).get(Constants.ID));
        if (StringUtils.isNotEmpty(content)) {
            encodeToFile(content);
            docDetails.setDocContent(content.toString());
            response.setResponseStatus(new PullDocResponse.ResponseStatus(1, new Date().toString(), request.getTxn()));
        } else {
            response.setResponseStatus(new PullDocResponse.ResponseStatus(0, new Date().toString(), request.getTxn()));
        }
        PullDocResponse.Person person = new PullDocResponse.Person();
        person.setName(request.getDocDetails().getDigiLockerId());
        person.setDob("31-12-1990");
        person.setGender("Male");
        docDetails.setDataContent(person);
        response.setDocDetails(docDetails);
        return response;
    }

    private Map<String, Object> getCertRecord(String accessToken) {
        Map<String, Object> certInfo = new HashMap<>();
        certInfo.put("accesscode", accessToken);
        List<Map<String, Object>> certInfoMap = cassandraOperation.getRecordsByProperties(
                Constants.KEYSPACE_SUNBIRD, "cert_registry", certInfo, null);
        return certInfoMap.get(0);
    }

    private String getCertificate(String certificateId) {
        Map<String, Object> response = (Map<String, Object>)outboundRequestHandlerService.fetchResult(serverProperties.getCertRegistryServiceBaseUrl() +
                serverProperties.getCertRegistryCertificateDownloadUrl() + certificateId);
        if (null != response && Constants.OK.equalsIgnoreCase((String) response.get(Constants.RESPONSE_CODE))) {
            Map<String, Object> certificateInfo = (Map<String, Object>) response.get(Constants.RESULT);
            if (MapUtils.isNotEmpty(certificateInfo)) {
                byte[] out = null;
                try {
                    out = generatePdfFromSvg(decodeUrl((String)certificateInfo.get("printUri")));
                    return encodeBytesToBase64(out);
                } catch (Exception e) {
                    logger.error(""+e);
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
        }
    }

    private void encodeToFile(String encodedString) {

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
            String filePath = "/home/sahilchaudhary/Desktop/files.pdf";

            // Write the decoded data to a file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(decodedBytes);
            }

            System.out.println("Decoded data saved to: " + filePath);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
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

}

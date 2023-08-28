package org.sunbird.progress.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.core.producer.Producer;

import java.util.HashMap;
import java.util.Map;

@Service
public class ContentProgressServiceImpl implements ContentProgressService {

    @Autowired
    private CbExtServerProperties cbExtServerProperties;
    @Autowired
    Producer kafkaProducer;

    private CbExtLogger logger = new CbExtLogger(getClass().getName());

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Marking the attendance for offline sessions
     *
     * @param authUserToken
     * @param request
     */
    @Override
    public SBApiResponse updateContentProgress(String authUserToken, SunbirdApiRequest requestBody) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_UPDATE_CONTENT_PROGRESS);
        Map<String, Object> contentProgressAttributes;
        Map<String, String> headersValues = new HashMap<>();
        headersValues.put("X-Authenticated-User-Token", authUserToken);
        headersValues.put("Authorization", cbExtServerProperties.getSbApiKey());
        try {
            contentProgressAttributes = new HashMap<>();
            contentProgressAttributes.put("requestBody", requestBody);
            contentProgressAttributes.put("headersValues", headersValues);
            kafkaProducer.push(cbExtServerProperties.getUpdateContentProgressKafkaTopic(), contentProgressAttributes);
            response.getParams().setStatus(Constants.SUCCESSFUL);
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception ex) {
            logger.error(ex);
            response.getParams().setErrmsg(String.format(Constants.UPDATE_CONTENT_PROGRESS_ERROR_MSG, ex.getMessage()));
            response.getParams().setStatus(Constants.FAILED);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
}

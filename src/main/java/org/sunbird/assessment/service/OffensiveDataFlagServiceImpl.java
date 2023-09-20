package org.sunbird.assessment.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.AccessTokenValidator;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("unchecked")
public class OffensiveDataFlagServiceImpl implements OffensiveDataFlagService {

    private final Logger logger = LoggerFactory.getLogger(OffensiveDataFlagServiceImpl.class);

    @Autowired
    CassandraOperation cassandraOperation;

    @Autowired
    AccessTokenValidator accessTokenValidator;

    @Override
    public SBApiResponse createFlag(Map<String, Object> requestBody, String token) {
        String errMsg = "";
        String userId = accessTokenValidator.fetchUserIdFromAccessToken(token);
        errMsg = validateRequest(requestBody, userId, Constants.ADD);
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.ADD_OFFENSIVE_DATA_FLAG);
        if (StringUtils.isEmpty(errMsg)) {
            Map<String, Object> request = (Map<String, Object>) requestBody.get(Constants.REQUEST);
            request.put(Constants.USER_ID, userId);
            request.put(Constants.CONTEXT_STATUS, Constants.DRAFT);
            request.put(Constants.CREATED_BY, userId);
            request.put(Constants.CREATED_DATE, new Timestamp(new java.util.Date().getTime()));
            SBApiResponse resp = cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_OFFENSIVE_DATA_FLAGS, request);
            if (!resp.get(Constants.RESPONSE).equals(Constants.SUCCESS)) {
                errMsg = "Failed to add records into DB";
            } else {
                response.getResult().put(Constants.STATUS, Constants.CREATED);
            }
        }
        if (StringUtils.isNotBlank(errMsg)) {
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(errMsg);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }
        return response;
    }

    @Override
    public SBApiResponse updateFlag(String token, Map<String, Object> requestBody) {
        String errMsg = "";
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.UPDATE_OFFENSIVE_DATA_FLAG);
        String userId = accessTokenValidator.fetchUserIdFromAccessToken(token);
        errMsg = validateRequest(requestBody, userId, Constants.UPDATE);
        if (StringUtils.isEmpty(errMsg)) {
            try {
                Map<String, Object> request = (Map<String, Object>) requestBody.get(Constants.REQUEST);
                Map<String, Object> compositeKeys = new HashMap<>();
                compositeKeys.put(Constants.USER_ID, userId);
                compositeKeys.put(Constants.CONTEXT_TYPE, request.get(Constants.CONTEXT_TYPE));
                compositeKeys.put(Constants.CONTEXT_TYPE_ID, request.get(Constants.CONTEXT_TYPE_ID));
                Map<String, Object> fieldsToBeUpdated = new HashMap<>();
                fieldsToBeUpdated.put(Constants.CONTEXT_STATUS, request.get(Constants.CONTEXT_STATUS));
                if(!ObjectUtils.isEmpty(request.get(Constants.ADDITIONAL_PARAMS))) {
                    fieldsToBeUpdated.put(Constants.ADDITIONAL_PARAMS, request.get(Constants.ADDITIONAL_PARAMS));
                }
                fieldsToBeUpdated.put(Constants.UPDATED_BY, userId);
                fieldsToBeUpdated.put(Constants.UPDATED_DATE, new Timestamp(new java.util.Date().getTime()));
                Map<String, Object> resp = cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_OFFENSIVE_DATA_FLAGS,
                        fieldsToBeUpdated, compositeKeys);
                if (!resp.get(Constants.RESPONSE).equals(Constants.SUCCESS)) {
                    errMsg = (String) resp.get(Constants.ERROR_MESSAGE);
                } else {
                    response.getResult().put(Constants.STATUS, Constants.UPDATED);
                }
            } catch (Exception e) {
                errMsg = String.format("Failed to update an offensive flag with the details. Exception: ", e.getMessage());
                logger.error(errMsg, e);
            }
        }
        if (StringUtils.isNotBlank(errMsg)) {
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(errMsg);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }
        return response;
    }

    @Override
    public SBApiResponse getFlaggedData(String token) {
        String userId = accessTokenValidator.fetchUserIdFromAccessToken(token);
        String errMsg = "";
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.GET_OFFENSIVE_DATA_FLAG);
        if (userId != null) {
            Map<String, Object> request = new HashMap<>();
            request.put(Constants.USER_ID, userId);
            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(
                    Constants.KEYSPACE_SUNBIRD, Constants.TABLE_OFFENSIVE_DATA_FLAGS, request, null);
            response.getResult().put("Content", existingDataList);
            response.getResult().put("Count", existingDataList.size());
        } else {
            errMsg = "Invalid User Id";
        }
        if (StringUtils.isNotBlank(errMsg)) {
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(errMsg);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }
        return response;
    }

    private String validateRequest(Map<String, Object> requestBody, String userId, String type) {
        if (StringUtils.isEmpty(userId)) {
            return "Invalid User Id";
        }
        Map<String, Object> request = (Map<String, Object>) requestBody.get(Constants.REQUEST);
        if (ObjectUtils.isEmpty(request)) {
            return "Invalid Request.";
        }

        StringBuilder errMsg = new StringBuilder();
        List<String> missingAttributes = new ArrayList<>();
        List<String> errList = new ArrayList<>();

        String contextType = (String) request.get(Constants.CONTEXT_TYPE);
        if (StringUtils.isBlank(contextType)) {
            missingAttributes.add(Constants.CONTEXT_TYPE);
        }

        String contextTypeId = (String) request.get(Constants.CONTEXT_TYPE_ID);
        if (StringUtils.isBlank(contextTypeId)) {
            missingAttributes.add(Constants.CONTEXT_TYPE_ID);
        }

        if(type.equalsIgnoreCase(Constants.UPDATE)) {
            String contextStatus = (String) request.get(Constants.CONTEXT_STATUS);
            if (StringUtils.isBlank(contextStatus)) {
                missingAttributes.add(Constants.CONTEXT_STATUS);
            }
        }

        if (!missingAttributes.isEmpty()) {
            errMsg.append("Request doesn't have mandatory parameters - [").append(missingAttributes)
                    .append("]. ");
        }

        if (!errList.isEmpty()) {
            errMsg.append(errList.toString());
        }
        return errMsg.toString();
    }
}
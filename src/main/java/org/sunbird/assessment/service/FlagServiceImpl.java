package org.sunbird.assessment.service;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.assessment.repo.FlagRepository;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.common.util.RequestInterceptor;

import java.util.*;

@Service
@SuppressWarnings("unchecked")
public class FlagServiceImpl implements FlagService {

    private final Logger logger = LoggerFactory.getLogger(FlagServiceImpl.class);

    @Autowired
    FlagRepository flagRepository;

    @Override
    public SBApiResponse createFlag(Map<String, Object> requestBody, String token) {
        String userId = validateAuthTokenAndFetchUserId(token);
        String errMsg = "";
        SBApiResponse response = new SBApiResponse();
        if (userId != null) {
            errMsg = validateAddRequest(requestBody, token);
            response = ProjectUtil.createDefaultResponse(Constants.ADD_OFFENSIVE_DATA_FLAG);
            if (StringUtils.isNotBlank(errMsg)) {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg(errMsg);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            try {
                Map<String, Object> request = (Map<String, Object>) requestBody.get(Constants.REQUEST);
                if (errMsg.length() == 0) {
                    Boolean dbResponse = flagRepository.addFlagDataToDB(userId,
                            request);
                    if (!dbResponse) {
                        errMsg = "Failed to add records into DB";
                    }
                }
            } catch (Exception e) {
                errMsg = String.format("Failed to create an offensive flag. Exception: ", e.getMessage());
                logger.error(errMsg, e);
            }
        }
        else
        {
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

    private String validateAddRequest(Map<String, Object> requestBody, String token) {
            Map<String, Object> request = (Map<String, Object>) requestBody.get(Constants.REQUEST);
            if (ObjectUtils.isEmpty(request)) {
                return "Invalid Flag Create request.";
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

            if (!missingAttributes.isEmpty()) {
                errMsg.append("Request doesn't have mandatory parameters - [").append(missingAttributes)
                        .append("]. ");
            }

            if (!errList.isEmpty()) {
                errMsg.append(errList.toString());
            }
            return errMsg.toString();
    }

    private String validateAuthTokenAndFetchUserId(String authUserToken) {
        return RequestInterceptor.fetchUserIdFromAccessToken(authUserToken);
    }

    @Override
    public SBApiResponse updateFlag(String token, Map<String, Object> requestBody) {
        String userId = validateAuthTokenAndFetchUserId(token);
        String errMsg = "";
        SBApiResponse response = new SBApiResponse();
        if (userId != null) {
            response = ProjectUtil.createDefaultResponse(Constants.UPDATE_OFFENSIVE_DATA_FLAG);

            errMsg = validateUpdateRequest(requestBody);
            if (StringUtils.isNotBlank(errMsg)) {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg(errMsg);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            try {
                Map<String, Object> request = (Map<String, Object>) requestBody.get(Constants.REQUEST);
                if (errMsg.length() == 0) {
                    Boolean dbResponse = flagRepository.updateFlaggedData(userId, request);
                    if (!dbResponse) {
                        errMsg = "Failed to update records into DB";
                    }
                }
            } catch (Exception e) {
                errMsg = String.format("Failed to update an offensive flag with the details. Exception: ", e.getMessage());
                logger.error(errMsg, e);
            }
        }
        else
            {
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

    @Override
    public SBApiResponse getFlaggedData(String token) {
        String userId = validateAuthTokenAndFetchUserId(token);
        String errMsg = "";
        SBApiResponse response = new SBApiResponse();
        if (userId != null) {
            response = ProjectUtil.createDefaultResponse(Constants.GET_OFFENSIVE_DATA_FLAG);
            try {
                    List<Map<String, Object>> dbResponse = flagRepository.getFlaggedData(userId);
                    if (dbResponse.isEmpty()) {
                        errMsg = "Failed to update records into DB";
                    }
                    else
                    {
                        response.getResult().put("Offensive Flags", dbResponse);
                    }
            } catch (Exception e) {
                errMsg = String.format("Failed to update an offensive flag with the details. Exception: ", e.getMessage());
                logger.error(errMsg, e);
            }
        }
        else
        {
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

    private String validateUpdateRequest(Map<String, Object> requestBody) {
        Map<String, Object> request = (Map<String, Object>) requestBody.get(Constants.REQUEST);
        if (ObjectUtils.isEmpty(requestBody)) {
            return "Invalid Flag Update request.";
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

        String contextStatus = (String) request.get(Constants.CONTEXT_STATUS);
        if (StringUtils.isBlank(contextStatus)) {
            missingAttributes.add(Constants.CONTEXT_STATUS);
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
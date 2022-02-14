package org.sunbird.profile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;
import java.util.*;

@Service
public class ProfileServiceImpl implements ProfileService{

    @Autowired
    CbExtServerProperties serverConfig;

    @Autowired
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    @Autowired
    private ObjectMapper mapper;
    private CbExtLogger log = new CbExtLogger(getClass().getName());

    @Override
    public SBApiResponse profileUpdate(Map<String,Object> request, String XAuthToken, String AuthToken) throws Exception {
        SBApiResponse response = new SBApiResponse(Constants.API_PROFILE_UPDATE);
        try {
            Map<String, Object> requestData = (Map<String, Object>) request.get(Constants.REQUEST);
            String userId = (String) requestData.get(Constants.USER_ID);
            Map<String, Object> profileDetailsMap = (Map<String, Object>) requestData.get(Constants.PROFILE_DETAILS);
            List<String> approvalFieldList = approvalFields(AuthToken, XAuthToken);
            Map<String, Object> transitionData = new HashMap<>();
            List<String> transitionList = new ArrayList<>();
            for (int i = 0; i < approvalFieldList.size(); i++) {
                if (profileDetailsMap.containsKey(approvalFieldList.get(i))) {
                    transitionList.add(approvalFieldList.get(i));
                    transitionData.put(approvalFieldList.get(i), profileDetailsMap.get(approvalFieldList.get(i)));
                    profileDetailsMap.remove(approvalFieldList.get(i));
                }
            }
            Map<String, String> header = new HashMap<>();
            header.put(Constants.AUTH_TOKEN,AuthToken);
            header.put(Constants.X_AUTH_TOKEN,XAuthToken);
            Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchUsingGetWithHeadersProfile(serverConfig.getSbUrl() + serverConfig.getLmsUserReadPath() + userId, header);
            Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
            Map<String, Object> responseMap = (Map<String, Object>) result.get(Constants.RESPONSE);
            String deptName = (String) responseMap.get(Constants.CHANNEL);
            Map<String, Object> profileDetailsRead = (Map<String, Object>) responseMap.get(Constants.PROFILE_DETAILS);

            Set<String> listDetails = profileDetailsMap.keySet();
            String[] listOfChangedDetails = listDetails.toArray(new String[listDetails.size()]);

            for (int i = 0; i < listOfChangedDetails.length; i++) {
                Map<String, Object> keyListRead = (Map<String, Object>) profileDetailsRead.get(listOfChangedDetails[i]);
                Map<String, Object> keyListRequest = (Map<String, Object>) profileDetailsMap.get(listOfChangedDetails[i]);
                Set<String> requestValue = keyListRequest.keySet();
                String[] listOfRequestValue = requestValue.toArray(new String[requestValue.size()]);
                for(int j = 0; j < listOfRequestValue.length; j++){
                    keyListRead.remove(listOfRequestValue[j]);
                    keyListRead.put(listOfRequestValue[j],keyListRequest.get(listOfRequestValue[j]));
                }
            }
            Map<String,Object>updateRequestValue = requestData;
            updateRequestValue.remove(Constants.PROFILE_DETAILS);
            updateRequestValue.put(Constants.PROFILE_DETAILS,profileDetailsRead);
            Map<String,Object> updateRequest = new HashMap<>();
            updateRequest.put(Constants.REQUEST,updateRequestValue);

            StringBuilder url = new StringBuilder();
            url.append(serverConfig.getSbUrl()).append(serverConfig.getLmsUserUpdatePath());
            HashMap<String, String> headerValues = new HashMap<>();
            headerValues.put(Constants.AUTH_TOKEN,AuthToken);
            headerValues.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
            Map<String,Object> updateResponse = (Map<String, Object>) mapper.convertValue(
                    outboundRequestHandlerService.fetchResultUsingPatch("http://learner-service:9000/private/user/v1/update", updateRequest, headerValues),
                    Map.class);
            Map<String,Object> workflowResponse = new HashMap<>();
            if (null!=transitionList) {
                List<Map<String, Object>> finalTransitionList = new ArrayList<>();
                for (int i = 0; i < transitionList.size(); i++) {
                    if (transitionData.get(transitionList.get(i)) instanceof ArrayList) {
                        List<Map<String, Object>> transList = (List<Map<String, Object>>) transitionData.get(transitionList.get(i));
                        for (int j = 0; j < transList.size(); j++) {
                            Map<String, Object> transData = new HashMap<>();
                            transData = transList.get(j);
                            Set<String> list = transData.keySet();
                            String[] innerData = list.toArray(new String[list.size()]);

                            for (int k = 0; k < innerData.length; k++) {
                                Map<String, Object> dataRay = new HashMap<>();
                                Map<String, Object> fromValue = new HashMap<>();
                                Map<String, Object> toValue = new HashMap<>();
                                toValue.put(innerData[k], transData.get(innerData[k]));
                                if (profileDetailsRead.get(transitionList.get(i)) instanceof ArrayList) {
                                    List<Map<String, Object>> readList = (List<Map<String, Object>>) profileDetailsRead.get(transitionList.get(i));
                                    Map<String, Object> readListData = readList.get(j);
                                    fromValue.put(innerData[k], readListData.get(innerData[k]));
                                    dataRay.put(Constants.OSID, readListData.get(Constants.OSID));
                                }
                                dataRay.put(Constants.FROM_VALUE, fromValue);
                                dataRay.put(Constants.TO_VALUE, toValue);
                                dataRay.put(Constants.FIELD_KEY, transitionList.get(i));
                                finalTransitionList.add(dataRay);
                            }
                        }
                    } else {
                        Map<String, Object> transListObject = new HashMap<>();
                        transListObject = (Map<String, Object>) transitionData.get(transitionList.get(i));
                        Set<String> listObject = transListObject.keySet();
                        String[] innerObjectData = listObject.toArray(new String[listObject.size()]);
                        for (int k = 0; k < innerObjectData.length; k++) {
                            Map<String, Object> updatedTransitionData = new HashMap<>();
                            Map<String, Object> fromValue = new HashMap<>();
                            Map<String, Object> toValue = new HashMap<>();
                            toValue.put(innerObjectData[k], transListObject.get(innerObjectData[k]));
                            Map<String, Object> readList = (Map<String, Object>) profileDetailsRead.get(transitionList.get(i));
                            fromValue.put(innerObjectData[k], readList.get(innerObjectData[k]));
                            updatedTransitionData.put(Constants.FROM_VALUE, fromValue);
                            updatedTransitionData.put(Constants.TO_VALUE, toValue);
                            updatedTransitionData.put(Constants.FIELD_KEY, transitionList.get(i));
                            updatedTransitionData.put(Constants.OSID, readList.get(Constants.OSID));
                            finalTransitionList.add(updatedTransitionData);
                        }
                    }
                }

                Map<String, Object> transitionRequests = new HashMap<>();
                transitionRequests.put(Constants.STATE, Constants.INITIATE);
                transitionRequests.put(Constants.ACTION, Constants.INITIATE);
                transitionRequests.put(Constants.USER_ID, userId);
                transitionRequests.put(Constants.APPLICATION_ID, userId);
                transitionRequests.put(Constants.ACTOR_USER_ID, userId);
                transitionRequests.put(Constants.SERVICE_NAME, Constants.PROFILE);
                transitionRequests.put(Constants.COMMENT, "");
                transitionRequests.put(Constants.WFID, "");
                transitionRequests.put(Constants.DEPT_NAME, deptName);
                transitionRequests.put(Constants.UPDATE_FIELD_VALUES, finalTransitionList);
                url = new StringBuilder();
                url.append(serverConfig.getWfServiceHost()).append(serverConfig.getWfServiceTransitionPath());
                headerValues.put("rootOrg","igot");
                headerValues.put("org","dopt");
                headerValues.put(Constants.X_AUTH_TOKEN,XAuthToken);
                workflowResponse =  outboundRequestHandlerService.fetchResultUsingPost("http://workflow-handler-service:5099/v1/workflow/transition", transitionRequests, headerValues);
            }

            if(null!=transitionList) {
                if (null != updateResponse ) {
                    Map<String, Object> resultValue = (Map<String, Object>) workflowResponse.get(Constants.RESULT);

                    if (null != workflowResponse && null != workflowResponse.get(Constants.RESULT)) {
                        response.getParams().setStatus((String) resultValue.get(Constants.STATUS));
                        response.getParams().setMsgid((String) resultValue.get(Constants.MESSAGE));
                        response.setResponseCode(HttpStatus.OK);
                    } else {
                        if (null != workflowResponse) {
                            response.getParams().setStatus((String) resultValue.get(Constants.STATUS));
                            response.getParams().setErr((String) resultValue.get(Constants.MESSAGE));
                            response.setResponseCode(HttpStatus.FORBIDDEN);
                        } else {
                            response.getParams().setStatus(Constants.FAILED);
                            response.getParams().setErr("workflow response is null but update response is present");
                            response.setResponseCode(HttpStatus.FORBIDDEN);
                        }
                    }
                } else {
                    response.getParams().setStatus(Constants.FAILED);
                    response.getParams().setErr("update response is null");
                    response.setResponseCode(HttpStatus.FORBIDDEN);

                }

            }else{
                if (null != updateResponse) {
                    response.getParams().setStatus(Constants.SUCCESS);
                    response.getParams().setMsgid("Details Updated");
                    response.setResponseCode(HttpStatus.OK);
                }else  {
                    response.getParams().setStatus(Constants.FAILED);
                    response.getParams().setErr("update response is null");
                    response.setResponseCode(HttpStatus.FORBIDDEN);
                }

            }
        } catch (Exception e) {
            log.error(e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErr(String.valueOf(e));
            response.setResponseCode(HttpStatus.FORBIDDEN);

        }
        return response;
    }


    public List<String> approvalFields(String AuthToken,String XAuthToken){
        Map<String, String> header = new HashMap<>();
        header.put(Constants.AUTH_TOKEN,AuthToken);
        header.put(Constants.X_AUTH_TOKEN,XAuthToken);
        Map<String,Object> approvalData = (Map<String, Object>) outboundRequestHandlerService.fetchUsingGetWithHeadersProfile(serverConfig.getSbUrl()+serverConfig.getLmsSystemSettingsPath(), header);
        Map<String,Object> approvalResult = (Map<String, Object>) approvalData.get(Constants.RESULT);
        Map<String,Object> approvalResponse = (Map<String, Object>) approvalResult.get(Constants.RESPONSE);
        String value = (String) approvalResponse.get(Constants.VALUE);
        List<String> approvalValues = new ArrayList<>();
        approvalValues.add(value);
        return approvalValues;
    }
}

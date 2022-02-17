package org.sunbird.profile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRespParam;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.service.UserUtilityServiceImpl;
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
    RedisCacheMgr redisCacheMgr;

    @Autowired
    UserUtilityServiceImpl userUtilityService;

    @Autowired
    private ObjectMapper mapper;
    private CbExtLogger log = new CbExtLogger(getClass().getName());

    @Override
    public SBApiResponse profileUpdate(Map<String,Object> request, String XAuthToken, String AuthToken) throws Exception {
        SBApiResponse response = new SBApiResponse(Constants.API_PROFILE_UPDATE);
        SunbirdApiRespParam resultObject = new SunbirdApiRespParam();
        try {
            Map<String, Object> requestData = (Map<String, Object>) request.get(Constants.REQUEST);
            String userId = (String) requestData.get(Constants.USER_ID);
            Map<String, Object> profileDetailsMap = (Map<String, Object>) requestData.get(Constants.PROFILE_DETAILS);
            List<String> approvalFieldList = approvalFields(AuthToken, XAuthToken);
            Map<String, Object> transitionData = new HashMap<>();
            for (String approvalList : approvalFieldList){
                if (profileDetailsMap.containsKey(approvalList)) {
                    transitionData.put(approvalList, profileDetailsMap.get(approvalList));
                    profileDetailsMap.remove(approvalList);
                }
            }
            Map<String, Object> responseMap = userUtilityService.getUsersReadData(userId, AuthToken, XAuthToken);
            String deptName = (String) responseMap.get(Constants.CHANNEL);
            Map<String, Object> existingProfileDetails = (Map<String, Object>) responseMap.get(Constants.PROFILE_DETAILS);
            StringBuilder url = new StringBuilder();
            HashMap<String, String> headerValues = new HashMap<>();
            headerValues.put(Constants.AUTH_TOKEN, AuthToken);
            headerValues.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
            Map<String, Object> workflowResponse = new HashMap<>();
            Map<String, Object> updateResponse = new HashMap<>();
            if (!profileDetailsMap.isEmpty()) {
                List<String> listOfChangedDetails = new ArrayList<>();
                for (String keys : profileDetailsMap.keySet()) {
                    listOfChangedDetails.add(keys);
                }

                for (String list : listOfChangedDetails) {
                    Map<String, Object> keyListRead = (Map<String, Object>) existingProfileDetails.get(list);
                    Map<String, Object> keyListRequest = (Map<String, Object>) profileDetailsMap.get(list);
                    for (String keysList : keyListRequest.keySet()) {
                        keyListRead.put(keysList,keyListRequest.get(keysList));
                    }
                }
                Map<String, Object> updateRequestValue = requestData;
                updateRequestValue.put(Constants.PROFILE_DETAILS, existingProfileDetails);
                Map<String, Object> updateRequest = new HashMap<>();
                updateRequest.put(Constants.REQUEST, updateRequestValue);

                url.append(serverConfig.getSbUrl()).append(serverConfig.getLmsUserUpdatePath());
                updateResponse =
                        outboundRequestHandlerService.fetchResultUsingPatch(serverConfig.getSbUrl()+serverConfig.getLmsUserUpdatePath(), updateRequest, headerValues);
                if (updateResponse.get(Constants.RESPONSE_CODE).equals(Constants.OK)){
                    resultObject.setStatus(Constants.SUCCESS);
                    response.getResult().put(Constants.PERSONAL_DETAILS,resultObject);
                    response.getParams().setStatus(Constants.SUCCESS);
                }else {
                    resultObject.setStatus(Constants.FAILED);
                    response.getResult().put(Constants.PERSONAL_DETAILS,resultObject);
                    response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    response.getParams().setStatus(Constants.FAILED);
                    return response;
                }

            }
            List<String> transitionList = new ArrayList<>();
            for (String key : transitionData.keySet()) {
                transitionList.add(key);
            }
            if (!transitionList.isEmpty()) {
                List<Map<String, Object>> finalTransitionList = new ArrayList<>();
                for (String listTransition : transitionList) {

                    if (transitionData.get(listTransition) instanceof ArrayList) {
                        List<Map<String, Object>> transList = (List<Map<String, Object>>) transitionData.get(listTransition);
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
                                if (existingProfileDetails.get(listTransition) instanceof ArrayList) {
                                    List<Map<String, Object>> readList = (List<Map<String, Object>>) existingProfileDetails.get(listTransition);
                                    Map<String, Object> readListData = readList.get(j);
                                    fromValue.put(innerData[k], readListData.get(innerData[k]));
                                    dataRay.put(Constants.OSID, readListData.get(Constants.OSID));
                                }
                                dataRay.put(Constants.FROM_VALUE, fromValue);
                                dataRay.put(Constants.TO_VALUE, toValue);
                                dataRay.put(Constants.FIELD_KEY, listTransition);
                                finalTransitionList.add(dataRay);
                            }
                        }
                    } else {
                        Map<String, Object> transListObject = new HashMap<>();
                        transListObject = (Map<String, Object>) transitionData.get(listTransition);
                        Set<String> listObject = transListObject.keySet();
                        String[] innerObjectData = listObject.toArray(new String[listObject.size()]);
                        for (int k = 0; k < innerObjectData.length; k++) {
                            Map<String, Object> updatedTransitionData = new HashMap<>();
                            Map<String, Object> fromValue = new HashMap<>();
                            Map<String, Object> toValue = new HashMap<>();
                            toValue.put(innerObjectData[k], transListObject.get(innerObjectData[k]));
                            Map<String, Object> readList = (Map<String, Object>) existingProfileDetails.get(listTransition);
                            fromValue.put(innerObjectData[k], readList.get(innerObjectData[k]));
                            updatedTransitionData.put(Constants.FROM_VALUE, fromValue);
                            updatedTransitionData.put(Constants.TO_VALUE, toValue);
                            updatedTransitionData.put(Constants.FIELD_KEY, listTransition);
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
                headerValues.put("rootOrg", "igot");
                headerValues.put(Constants.ROOT_ORG_CONSTANT, Constants.IGOT);
                headerValues.put(Constants.ORG_CONSTANT, Constants.DOPT);
                headerValues.put(Constants.X_AUTH_TOKEN, XAuthToken);
                workflowResponse = outboundRequestHandlerService.fetchResultUsingPost(serverConfig.getWfServiceHost() + serverConfig.getWfServiceTransitionPath(), transitionRequests, headerValues);

                Map<String, Object> resultValue = (Map<String, Object>) workflowResponse.get(Constants.RESULT);
                if (resultValue.get(Constants.STATUS).equals(Constants.OK)) {
                    response.setResponseCode(HttpStatus.OK);
                    resultObject.setStatus(Constants.SUCCESS);
                    response.getResult().put(Constants.TRANSITION_DETAILS,resultObject);
                } else {
                    response.setResponseCode(HttpStatus.OK);
                    resultObject.setStatus(Constants.FAILED);
                    resultObject.setErrmsg((String) resultValue.get(Constants.MESSAGE));
                    response.getResult().put(Constants.TRANSITION_DETAILS,resultObject);
                }
            }
        } catch (Exception e) {
            log.error(e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErr(String.valueOf(e));
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);

        }
        return response;
    }


    public List<String> approvalFields(String AuthToken,String XAuthToken){

        Map<String, Object> approvalFieldsCache = (Map<String, Object>) mapper.convertValue(redisCacheMgr.getCache(Constants.PROFILE_UPDATE_FIELDS),Map.class);

        if(!(CollectionUtils.isEmpty(approvalFieldsCache))){
            Map<String,Object> approvalResult = (Map<String, Object>) approvalFieldsCache.get(Constants.RESULT);
            Map<String,Object> approvalResponse = (Map<String, Object>) approvalResult.get(Constants.RESPONSE);
            String value = (String) approvalResponse.get(Constants.VALUE);
            List<String> approvalValues = new ArrayList<>();
            approvalValues.add(value);
            return approvalValues;
       }else {
            Map<String, String> header = new HashMap<>();
            header.put(Constants.AUTH_TOKEN, AuthToken);
            header.put(Constants.X_AUTH_TOKEN, XAuthToken);
            Map<String, Object> approvalData = (Map<String, Object>) outboundRequestHandlerService.fetchUsingGetWithHeadersProfile(serverConfig.getSbUrl() + serverConfig.getLmsSystemSettingsPath(), header);
            Map<String, Object> approvalResult = (Map<String, Object>) approvalData.get(Constants.RESULT);
            Map<String, Object> approvalResponse = (Map<String, Object>) approvalResult.get(Constants.RESPONSE);
            String value = (String) approvalResponse.get(Constants.VALUE);
            String strArray[] = value.split(" ");
            List<String> approvalValues = Arrays.asList(strArray);
            return approvalValues;
        }
    }
}

package org.sunbird.profile.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;

import java.util.*;

@Component
@Service
public class ProfileServiceImpl implements ProfileService{

    @Autowired
    CbExtServerProperties serverConfig;

    @Autowired
    RestTemplate restTemplate;

    public static JSONObject transitionRequest = new JSONObject();
    public static JSONArray transitionKeys = new JSONArray();
    public static JSONObject profileDetails = new JSONObject();
    public static JSONObject profile = new JSONObject();
    public static JSONObject readData = new JSONObject();
    public static JSONArray updatedField = new JSONArray();
    public static JSONObject requestData = new JSONObject();
    public static JSONObject transitionFrom = new JSONObject();
    private CbExtLogger logger = new CbExtLogger(getClass().getName());

    @Override
    public SBApiResponse profileUpdate(String request) throws Exception {

        SBApiResponse response = new SBApiResponse(Constants.API_PROFILE_UPDATE);
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Object> entity = new HttpEntity<Object>(headers);
            String read = restTemplate.exchange(serverConfig.getCourseServiceHost()+serverConfig.getLmsUserReadPath(), HttpMethod.GET, entity, String.class).getBody();
            readData = new JSONObject(read);
            String approvalFields = restTemplate.exchange(serverConfig.getCourseServiceHost()+serverConfig.getLmsSystemSettingsPath(), HttpMethod.GET, entity, String.class).getBody();
            requestData = new JSONObject(request);
            JSONObject approvalSet = new JSONObject(approvalFields);
            JSONObject approvalResult = (JSONObject) approvalSet.get(Constants.RESULT);
            JSONObject approvalResponse = (JSONObject) approvalResult.get(Constants.RESPONSE);
            String value = (String) approvalResponse.get(Constants.VALUE);
            JSONArray profileConfig = new JSONArray(value);
            profile = (JSONObject) requestData.get("request");
            profileDetails = (JSONObject) profile.get("profileDetails");
            String userId = (String) profileDetails.get(Constants.USER_ID);
            String deptName = (String) profileDetails.get(Constants.DEPT_NAME);

        for (int i = 0; i < profileConfig.length(); i++) {
            getKey(requestData, profileConfig.get(i).toString(), "update");

        }
            Map<String, Object> updateRequestBody = toMap(profileDetails);
            HttpEntity<Object> entityForUpdate = new HttpEntity<>(updateRequestBody, headers);
            Map<String, Object> updateResponse = restTemplate.exchange(serverConfig.getCourseServiceHost()+serverConfig.getLmsUserUpdatePath(), HttpMethod.PATCH, entityForUpdate, Map.class).getBody();

            for (int i = 0; i < transitionKeys.length(); i++) {
                getKey(transitionRequest, transitionKeys.get(i).toString(), "transition");

            }

            JSONObject transitionRequest = new JSONObject();
            transitionRequest.put(Constants.STATE, "INITIATE");
            transitionRequest.put(Constants.ACTION, "INITIATE");
            transitionRequest.put(Constants.USER_ID, userId);
            transitionRequest.put(Constants.APPLICATION_ID, userId);
            transitionRequest.put(Constants.ACTOR_USER_ID, userId);
            transitionRequest.put(Constants.DEPT_NAME, deptName);
            transitionRequest.put(Constants.UPDATED_FIELD_VALUES, updatedField);
            Map<String, Object> transitionRequestBody = toMap(transitionRequest);

            HttpEntity<Object> entityForTransition = new HttpEntity<>(transitionRequestBody, headers);
            Map<String, Object> TransitionResponse = restTemplate.exchange(serverConfig.getWfServiceHost() + serverConfig.getWfServiceTransitionPath(), HttpMethod.POST, entityForTransition, Map.class).getBody();
            if (updateResponse.get(Constants.RESPONSE_CODE).equals("OK") && TransitionResponse.get(Constants.RESPONSE_CODE).equals("OK")) {
                response.getParams().setStatus(Constants.SUCCESSFUL);
                response.setResponseCode(HttpStatus.CREATED);
                response.put(Constants.MESSAGE, "user details updated and send for approval");
            }else{
                response.getParams().setStatus(Constants.FAILED);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                response.put(Constants.MESSAGE, "user details not updated");
            }

        }catch (Exception e){
            logger.error(e);
            throw new Exception(e.toString());
        }
        return response;
    }
    public static void parseObject(JSONObject json,String key,String version){

        if(version.equals("update")) {
            transitionRequest.put(key, json.get(key));
            profileDetails.remove(key);
            transitionKeys.put(key);
        }

        if(version.equals("dynamic")) {
            transitionFrom = new JSONObject();
            transitionFrom.put(key, json.get(key));
            System.out.println(json.get(key));
        }
        if(version.equals("transition")) {
            JSONObject transit = new JSONObject();
            JSONObject transitionTo = new JSONObject();
            JSONObject approvalResponse = new JSONObject();
            approvalResponse = (JSONObject) json.get(key);

            Iterator keys = approvalResponse.keys();
            while(keys.hasNext()) {
                // loop to get the dynamic key
                transit = new JSONObject();
                transitionTo = new JSONObject();
                transit.put(Constants.FIELD_KEY, key);
                String currentDynamicKey = (String)keys.next();

                getKey(readData, currentDynamicKey, "dynamic");                // get the value of the dynamic key
                String currentDynamicValue = (String) approvalResponse.get(currentDynamicKey);
                transitionTo.put(currentDynamicKey, currentDynamicValue);
                transit.put("toValue", transitionTo);
                transit.put("fromValue", transitionFrom);
                updatedField.put(transit);
                System.out.println(currentDynamicValue);
                // do something here with the value...
            }

        }
    }

    public static void  getKey(JSONObject json,String key, String version){
        boolean exists = json.has(key);
        Iterator<?> keys;
        String nextKeys;

        if(!exists){
            keys=json.keys();
            while(keys.hasNext()) {
                nextKeys = (String) keys.next();
                try {
                    if(json.get(nextKeys) instanceof JSONObject){
                        if(exists == false){
                            getKey(json.getJSONObject(nextKeys),key,version);
                        }
                    }else if (json.get(nextKeys) instanceof JSONArray) {
                        JSONArray jsonarray = json.getJSONArray(nextKeys);
                        for (int i=0; i<jsonarray.length(); i++){
                            String jsonarrayString = jsonarray.get(i).toString();
                            JSONObject innerJson = new JSONObject(jsonarrayString);
                            if(exists == false){
                                getKey(innerJson,key,version);
                            }
                        }
                    }
                }catch (Exception e) {
                    System.out.println(e);

                }

            }

        }else {
            parseObject(json,key,version);
        }

    }

    public static Map<String, Object> toMap(JSONObject jsonObj)  {
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator<String> keys = jsonObj.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObj.get(key);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }   return map;
    }

    public static List<Object> toList(JSONArray array)  {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }
            else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }   return list;
    }


}


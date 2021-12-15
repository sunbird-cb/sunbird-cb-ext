package org.sunbird.profile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;

import java.util.*;

@Component
@Service
public class ProfileserviceImpl implements ProfileService{

    @Autowired
    CbExtServerProperties serverConfig;

    @Autowired
    RestTemplate restTemplate;


    public static JSONArray profKey = new JSONArray();
    public static JSONArray skillKey = new JSONArray();
    public static JSONArray acadKey = new JSONArray();
    public static JSONArray persKey = new JSONArray();
    public static JSONArray emplKey = new JSONArray();
    public static JSONArray updatedField = new JSONArray();
    public static JSONObject readFrom = new JSONObject();
    public static JSONObject profTo = new JSONObject();
    public static JSONObject skillTo = new JSONObject();
    public static JSONObject acadTo = new JSONObject();
    public static JSONObject persTo = new JSONObject();
    public static JSONObject emplTo = new JSONObject();
    public static JSONObject requestData = new JSONObject();
    public static JSONObject academicsRequest = new JSONObject();
    public static JSONObject professionalRequest = new JSONObject();
    public static JSONObject personalRequest = new JSONObject();
    public static JSONObject employmentRequest = new JSONObject();
    public static JSONObject skillsRequest = new JSONObject();

    @Override
    public SBApiResponse workflowTransition(String wfRequest) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        Object approvalFields = restTemplate.exchange(serverConfig.getWfServiceHost()+serverConfig.getLmsSystemSettingsPath(), HttpMethod.GET, entity, Map.class).getBody();
        JSONObject approvalSet = (JSONObject) JSONValue.parse(new ObjectMapper().writeValueAsString(approvalFields));
        JSONObject approvalResult = (JSONObject)approvalSet.get(Constants.RESULT);
        JSONObject approvalResponse = (JSONObject)approvalResult.get(Constants.RESPONSE);
        JSONArray profileConfig = (JSONArray)approvalResponse.get(Constants.VALUE);
        requestData = new JSONObject(wfRequest);
        JSONObject requestDataRequest = (JSONObject)requestData.get(Constants.REQUEST);
        String userId = (String) requestDataRequest.get(Constants.USER_ID);
        String deptName = (String) requestDataRequest.get(Constants.DEPT_NAME);
        JSONObject requestDataDetails = (JSONObject)requestDataRequest.get(Constants.PROFILE_DETAILS);
        if(requestDataDetails.get(Constants.ACADEMICS)!=null) {
            academicsRequest = (JSONObject) requestDataDetails.get(Constants.ACADEMICS);
        }
        if(requestDataDetails.get(Constants.PROFESIONAL_DETAILS)!=null) {
            professionalRequest = (JSONObject) requestDataDetails.get(Constants.PROFESIONAL_DETAILS);
        }
        if(requestDataDetails.get(Constants.PERSONAL_DETAILS)!=null) {
            personalRequest = (JSONObject) requestDataDetails.get(Constants.PERSONAL_DETAILS);
        }
        if(requestDataDetails.get(Constants.EMPLOYMENT_DETAILS)!=null) {
            employmentRequest = (JSONObject) requestDataDetails.get(Constants.EMPLOYMENT_DETAILS);
        }
        if(requestDataDetails.get(Constants.SKILLS)!=null) {
            skillsRequest = (JSONObject) requestDataDetails.get(Constants.SKILLS);
        }
        JSONArray paramSet = new JSONArray();
        paramSet.put(Constants.SKILLS);
        paramSet.put(Constants.PROFESIONAL_DETAILS);
        paramSet.put(Constants.EMPLOYMENT_DETAILS);
        paramSet.put(Constants.PERSONAL_DETAILS);
        paramSet.put(Constants.ACADEMICS);
        paramSet.put(Constants.READ_DATA);
        Object readSet =  restTemplate.exchange(serverConfig.getLmsServiceUrl()+serverConfig.getLmsUserReadPath()+userId, HttpMethod.GET, entity, Map.class).getBody();
        JSONObject readData = new JSONObject(readSet);
        JSONObject readDataResult = (JSONObject)readData.get(Constants.RESULT);
        JSONObject readDataResponse = (JSONObject)readDataResult.get(Constants.RESPONSE);
        JSONObject readDataDetails = (JSONObject)readDataResponse.get(Constants.PROFILE_DETAILS);

        for(int j=0; j<paramSet.length(); j++) {
            for (int i = 0; i < profileConfig.length(); i++) {
                if(paramSet.get(j).toString()=="skills") {
                    getKey(skillsRequest, profileConfig.get(i).toString(), "skills");

                }else if(paramSet.get(j).toString()=="professionalDetails"){
                    getKey(professionalRequest, profileConfig.get(i).toString(), "professionalDetails");

                }else if(paramSet.get(j).toString()=="employmentDetails"){
                    getKey(employmentRequest, profileConfig.get(i).toString(), "employmentDetails");

                }else if(paramSet.get(j).toString()=="personalDetails"){
                    getKey(personalRequest, profileConfig.get(i).toString(), "personalDetails");

                }else if(paramSet.get(j).toString()=="academics"){
                    getKey(academicsRequest, profileConfig.get(i).toString(), "academics");

                }else if(paramSet.get(j).toString()=="readData"){
                    getKey(readDataDetails, profileConfig.get(i).toString(), "readData");

                }
            }
        }

        for(int j=0; j<paramSet.length(); j++) {
            if(paramSet.get(j).toString()=="skills") {
                for (int i = 0; i < skillKey.length(); i++) {
                    JSONObject transit = new JSONObject();
                    JSONObject transitionFrom = new JSONObject();
                    JSONObject transitionTo = new JSONObject();
                    transit.put(Constants.FIELD_KEY, "skills");
                    transitionFrom.put(skillKey.get(i).toString(), readFrom.get(skillKey.get(i).toString()));
                    transitionTo.put(skillKey.get(i).toString(), skillTo.get(skillKey.get(i).toString()));
                    transit.put("toValue", transitionTo);
                    transit.put("fromValue", transitionFrom);
                    updatedField.put(transit);
                    skillsRequest.remove(skillKey.get(i).toString());
                }
            }
            if(paramSet.get(j).toString()=="professionalDetails") {
                for (int i = 0; i < profKey.length(); i++) {
                    JSONObject transit = new JSONObject();
                    JSONObject transitionFrom = new JSONObject();
                    JSONObject transitionTo = new JSONObject();
                    transit.put(Constants.FIELD_KEY, "professionalDetails");
                    transitionFrom.put(profKey.get(i).toString(), readFrom.get(profKey.get(i).toString()));
                    transitionTo.put(profKey.get(i).toString(), profTo.get(profKey.get(i).toString()));
                    transit.put("toValue", transitionTo);
                    transit.put("fromValue", transitionFrom);
                    updatedField.put(transit);
                    professionalRequest.remove(profKey.get(i).toString());
                }
            }
            if(paramSet.get(j).toString()=="employmentDetails") {
                for (int i = 0; i < emplKey.length(); i++) {
                    JSONObject transit = new JSONObject();
                    JSONObject transitionFrom = new JSONObject();
                    JSONObject transitionTo = new JSONObject();
                    transit.put(Constants.FIELD_KEY, "employmentDetails");
                    transitionFrom.put(emplKey.get(i).toString(), readFrom.get(emplKey.get(i).toString()));
                    transitionTo.put(emplKey.get(i).toString(), emplTo.get(emplKey.get(i).toString()));
                    transit.put("toValue", transitionTo);
                    transit.put("fromValue", transitionFrom);
                    updatedField.put(transit);
                    employmentRequest.remove(emplKey.get(i).toString());
                }
            }
            if(paramSet.get(j).toString()=="personalDetails") {
                for (int i = 0; i < persKey.length(); i++) {
                    JSONObject transit = new JSONObject();
                    JSONObject transitionFrom = new JSONObject();
                    JSONObject transitionTo = new JSONObject();
                    transit.put(Constants.FIELD_KEY, "personalDetails");
                    transitionFrom.put(persKey.get(i).toString(), readFrom.get(persKey.get(i).toString()));
                    transitionTo.put(persKey.get(i).toString(), persTo.get(persKey.get(i).toString()));
                    transit.put("toValue", transitionTo);
                    transit.put("fromValue", transitionFrom);
                    updatedField.put(transit);
                    personalRequest.remove(persKey.get(i).toString());
                }
            }
            if(paramSet.get(j).toString()=="academics") {
                for (int i = 0; i < emplKey.length(); i++) {
                    JSONObject transit = new JSONObject();
                    JSONObject transitionFrom = new JSONObject();
                    JSONObject transitionTo = new JSONObject();
                    transit.put(Constants.FIELD_KEY, "academics");
                    transitionFrom.put(acadKey.get(i).toString(), readFrom.get(acadKey.get(i).toString()));
                    transitionTo.put(acadKey.get(i).toString(), acadTo.get(acadKey.get(i).toString()));
                    transit.put("toValue", transitionTo);
                    transit.put("fromValue", transitionFrom);
                    updatedField.put(transit);
                    academicsRequest.remove(acadKey.get(i).toString());
                }
            }

        }
        Map<String, Object> map = personalRequest.toMap();
        map.putAll(professionalRequest.toMap());
        map.putAll(academicsRequest.toMap());
        map.putAll(employmentRequest.toMap());
        map.putAll(skillsRequest.toMap());
        HttpEntity<Object> entityForUpdate = new HttpEntity<>(map, headers);
        Object updateResponse =  restTemplate.exchange(serverConfig.getLmsServiceUrl()+serverConfig.getLmsUserUpdatePath(), HttpMethod.PATCH, entityForUpdate, Map.class).getBody();
        JSONObject transitionRequest = new JSONObject();
        transitionRequest.put(Constants.STATE,"INITIATE");
        transitionRequest.put(Constants.ACTION,"INITIATE");
        transitionRequest.put(Constants.USER_ID,userId);
        transitionRequest.put(Constants.APPLICATION_ID,userId);
        transitionRequest.put(Constants.ACTOR_USER_ID,userId);
        transitionRequest.put(Constants.DEPT_NAME,deptName);
        transitionRequest.put(Constants.UPDATED_FIELD_VALUES,updatedField);
        HttpEntity<Object> entityForTransition = new HttpEntity<>(transitionRequest, headers);
        Object TransitionResponse =  restTemplate.exchange(serverConfig.getWfServiceHost()+serverConfig.getWfServiceTransitionPath(), HttpMethod.POST, entityForTransition, Map.class).getBody();

        return null;
    }

    public static void parseObject(JSONObject json,String key,String version){

        if(version.equals("professionalDetails")) {
            if (json.get(key).toString() != null) {
                profTo.put(key,json.get(key));
                profKey.put(key);
            }
        }
        if(version.equals("skills")) {
            if (json.get(key).toString() != null) {
                skillTo.put(key,json.get(key));
                skillKey.put(key);
            }
        }
        if(version.equals("employmentDetails")) {
            if (json.get(key).toString() != null) {
                emplTo.put(key,json.get(key));
                emplKey.put(key);
            }
        }
        if(version.equals("personalDetails")) {
            if (json.get(key).toString() != null) {
                persTo.put(key,json.get(key));
                persKey.put(key);
            }
        }
        if(version.equals("academics")) {
            if (json.get(key).toString() != null) {
                acadTo.put(key,json.get(key));
                acadKey.put(key);
            }
        }
        if(version.equals("readData")) {
            if (json.get(key).toString() != null) {
                readFrom.put(key,json.get(key));
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

}



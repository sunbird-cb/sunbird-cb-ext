package org.sunbird.profile.service;

import org.sunbird.common.model.SBApiResponse;

import java.util.Map;

public interface ProfileService {
    SBApiResponse profileUpdate(Map<String,Object> request, String XAuthToken, String AuthToken) throws Exception;
    SBApiResponse orgProfileUpdate(Map<String,Object> request) throws Exception;
    SBApiResponse orgProfileRead(String orgId) throws Exception;
    SBApiResponse signupUser(Map<String,Object> request) throws Exception;
    //Boolean createUser(Map<String,Object> request)throws Exception;
    Map<String, Object> getUsersReadData(String userId, String authToken, String userAuthToken)throws Exception;
  //  boolean updateUser(Map<String,Object> request)throws Exception;
    boolean assignRole(Map<String ,Object > request)throws Exception;
    /* SBApiResponse getUserDetailsById(String userId, String authToken, String userAuthToken) ;*/
}

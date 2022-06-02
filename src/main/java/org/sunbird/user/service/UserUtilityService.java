package org.sunbird.user.service;

import java.util.List;
import java.util.Map;

import org.sunbird.telemetry.model.UserInfo;
import org.sunbird.user.registration.model.UserRegistration;

public interface UserUtilityService {

	boolean validateUser(String userId);

	boolean validateUser(String rootOrg, String userId);

	Map<String, Object> getUsersDataFromUserIds(String rootOrg, List<String> userIds, List<String> source);

	Map<String, Object> getUsersDataFromUserIds(List<String> userIds, List<String> fields, String authToken);

	Map<String, Object> updateLogin(UserInfo userLoginInfo);

	Map<String, Object> getUsersReadData(String userId, String authToken, String X_authToken);

	Boolean createUser(UserInfo userInfo, String xAuthToken);
}
package org.sunbird.user.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.telemetry.model.LastLoginInfo;
import org.sunbird.user.registration.model.UserRegistration;

public interface UserUtilityService {

	boolean validateUser(String userId);

	boolean validateUser(String rootOrg, String userId);

	Map<String, Object> getUsersDataFromUserIds(String rootOrg, List<String> userIds, List<String> source);

	Map<String, Object> getUsersDataFromUserIds(List<String> userIds, List<String> fields, String authToken);

	Map<String, Object> updateLogin(LastLoginInfo userLoginInfo);

	Map<String, Object> getUsersReadData(String userId, String authToken, String X_authToken);

	boolean createUser(UserRegistration userRegistration);

	boolean updateUser(UserRegistration userRegistration);

	boolean getActivationLink(UserRegistration userRegistration);

	boolean createNodeBBUser(UserRegistration userRegistration);

	public boolean assignRole(String sbOrgId, String userId, String objectDetails);

	public Map<String, Map<String, String>> getUserDetails(List<String> userIds, List<String> fields);

	public void getUserDetailsFromDB(List<String> userIds, List<String> fields,
			Map<String, Map<String, String>> userInfoMap);
	
	public void enrichUserInfo(List<String> fields, Map<String, Map<String, String>> userInfoMap);

	boolean isUserExist(String key, String value);

	boolean validateGroup(String group);

	String createBulkUploadUser(UserRegistration userRegistration);

	String updateBulkUploadUser(UserRegistration userRegistration);

	Map<String, Map<String, Object>> getUserDetailsFromES(List<String> userIdList, List<String> userFields) throws IOException;

	Map<String, Object> getUsersDataFromLookup(String email, String authToken);
	SBApiResponse recommendContent(String authUserToken, Map<String, Object> orgRequest);

	Map<String, Object> getUserDetails(String key, String value);

	boolean validateGender(String gender);

	boolean validateCategory(String category);
}
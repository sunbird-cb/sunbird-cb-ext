package org.sunbird.common.service;

import java.util.List;
import java.util.Map;

import org.sunbird.portal.department.model.LastLoginInfo;

public interface UserUtilityService {

	boolean validateUser(String rootOrg, String userId);

	Map<String, Object> updateLogin(LastLoginInfo userLoginInfo);

	Map<String, Object> getUsersDataFromUserIds(List<String> userIds, List<String> fields, String authToken);

	Map<String, Object> getUsersDataFromUserIds(String rootOrg, List<String> userIds, List<String> source);

}

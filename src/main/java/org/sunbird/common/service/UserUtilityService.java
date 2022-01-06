package org.sunbird.common.service;

import java.util.List;
import java.util.Map;

public interface UserUtilityService {

	boolean validateUser(String rootOrg, String userId);

	Map<String, Object> getUsersDataFromUserIds(String rootOrg, List<String> userIds, List<String> source);

	Map<String, Object> getUsersDataFromUserIds(List<String> userIds, List<String> fields, String authToken);

}

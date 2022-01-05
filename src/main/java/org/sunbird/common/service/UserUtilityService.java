package org.sunbird.common.service;

import java.util.List;
import java.util.Map;

public interface UserUtilityService {

	Map<String, Object> getUsersDataFromUserIds(String rootOrg, List<String> userIds, List<String> source);

	boolean validateUser(String rootOrg, String userId);

}

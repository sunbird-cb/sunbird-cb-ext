package org.sunbird.profile.service;

import org.sunbird.common.model.SBApiResponse;
import java.util.Map;

public interface ProfileService {
	SBApiResponse profileUpdate(Map<String, Object> request, String XAuthToken, String AuthToken) throws Exception;

	SBApiResponse orgProfileUpdate(Map<String, Object> request) throws Exception;

	SBApiResponse orgProfileRead(String orgId) throws Exception;

	SBApiResponse userBasicInfo(String userId);

	SBApiResponse userBasicProfileUpdate(Map<String, Object> request);

	SBApiResponse userAutoComplete(String searchTerm);

	SBApiResponse migrateUser(Map<String, Object> request, String userToken, String authToken);
}

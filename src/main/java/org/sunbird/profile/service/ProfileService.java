package org.sunbird.profile.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;
import org.sunbird.common.model.SBApiResponse;

public interface ProfileService {
	SBApiResponse profileUpdate(Map<String, Object> request, String XAuthToken, String AuthToken) throws Exception;

	SBApiResponse orgProfileUpdate(Map<String, Object> request) throws Exception;

	SBApiResponse orgProfileRead(String orgId) throws Exception;

	SBApiResponse userBasicInfo(String userId);

	SBApiResponse userBasicProfileUpdate(Map<String, Object> request);

	SBApiResponse userAutoComplete(String searchTerm);

	SBApiResponse migrateUser(Map<String, Object> request, String userToken, String authToken);

	SBApiResponse userSignup(Map<String, Object> request);

	SBApiResponse bulkUpload(MultipartFile mFile, String orgId, String orgName, String userId);

	SBApiResponse getBulkUploadDetails(String orgId);
	
	SBApiResponse getNotificationPreferencesById(String userId);

	SBApiResponse updateNotificationPreference(String userId,Map<String,Object> request);
}

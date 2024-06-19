package org.sunbird.profile.service;

import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.common.model.SBApiResponse;


public interface ProfileService {
	SBApiResponse profileUpdate(Map<String, Object> request, String XAuthToken, String AuthToken, String rootOrgId) ;

	SBApiResponse orgProfileUpdate(Map<String, Object> request) throws Exception;

	SBApiResponse orgProfileRead(String orgId) throws Exception;

	SBApiResponse userBasicInfo(String userId);

	SBApiResponse userBasicProfileUpdate(Map<String, Object> request);

	SBApiResponse userAutoComplete(String searchTerm);

	SBApiResponse userAdminAutoComplete(String searchTerm, String rootOrgId);

	SBApiResponse migrateUser(Map<String, Object> request, String userToken, String authToken);

	SBApiResponse userSignup(Map<String, Object> request);

	SBApiResponse bulkUpload(MultipartFile mFile, String orgId, String orgName, String userId, String userAuthToken);

	SBApiResponse getBulkUploadDetails(String orgId);

	SBApiResponse getUserEnrollmentReport();
	
	SBApiResponse getUserReport();

	ResponseEntity<Resource> downloadFile(String fileName);

	SBApiResponse getGroupList();

	SBApiResponse profileMDOAdminUpdate(Map<String, Object> request, String XAuthToken, String AuthToken, String rootOrgId) throws Exception;

	SBApiResponse profileExternalSystemUpdate(Map<String, Object> request, String authToken);

	/**
	 * Updates the user profile with version 2.
	 *
	 * @param request   The request containing profile update data.
	 * @param userToken The user token for authentication.
	 * @param authToken The authentication token.
	 * @param rootOrgId The root organization ID.
	 * @return SBApiResponse object containing the response of the profile update.
	 * @throws Exception if any error occurs during the profile update process.
	 */
	SBApiResponse profileUpdateV2(Map<String, Object> request, String userToken, String authToken, String rootOrgId) ;

	SBApiResponse bulkUploadV2(MultipartFile mFile, String orgId, String orgName, String userId, String userAuthToken);
}

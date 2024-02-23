package org.sunbird.profile.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.profile.service.ProfileService;

@RestController
public class ProfileController {

	@Autowired
	private ProfileService profileService;

	private Logger log = LoggerFactory.getLogger(getClass().getName());

	@PostMapping("/user/patch")
	public ResponseEntity<SBApiResponse> profileUpdate(
			@RequestHeader(value = Constants.X_AUTH_TOKEN, required = true) String userToken,
			@RequestHeader(value = Constants.AUTH_TOKEN, required = false) String authToken,
			@RequestHeader(value = Constants.X_AUTH_USER_ORG_ID, required = false) String rootOrgId,
			@RequestBody Map<String, Object> request) throws Exception {
		SBApiResponse response = profileService.profileUpdate(request, userToken, authToken, rootOrgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PatchMapping("/org/v1/profile/patch")
	public ResponseEntity<SBApiResponse> orgProfileUpdate(@RequestBody Map<String, Object> request) {
		SBApiResponse response = profileService.orgProfileUpdate(request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/org/v1/profile/read")
	public ResponseEntity<SBApiResponse> orgProfileRead(@RequestParam String orgId) throws Exception {
		SBApiResponse response = profileService.orgProfileRead(orgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/v1/basicInfo")
	public ResponseEntity<SBApiResponse> userBasicInfo(@RequestHeader(Constants.X_AUTH_USER_ID) String userId) {
		SBApiResponse response = profileService.userBasicInfo(userId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/user/v1/basicProfileUpdate")
	public ResponseEntity<SBApiResponse> parichayProfileUpdate(@RequestBody Map<String, Object> request) {
		SBApiResponse response = profileService.userBasicProfileUpdate(request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/v1/autocomplete/{searchTerm}")
	public ResponseEntity<SBApiResponse> userAutoComplete(@PathVariable("searchTerm") String searchTerm) {
		SBApiResponse response = profileService.userAutoComplete(searchTerm);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/v1/admin/autocomplete/{searchTerm}")
	public ResponseEntity<SBApiResponse> userMdoAutoComplete(@PathVariable("searchTerm") String searchTerm,
												 @RequestHeader(Constants.X_AUTH_USER_ORG_ID) String rootOrgId) {
		SBApiResponse response = profileService.userAdminAutoComplete(searchTerm, rootOrgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PatchMapping("/user/v1/migrate")
	private ResponseEntity<SBApiResponse> adminMigrateUser(@RequestHeader(Constants.X_AUTH_TOKEN) String userToken,
			@RequestHeader(Constants.AUTH_TOKEN) String authToken, @RequestBody Map<String, Object> request) {
		SBApiResponse response = profileService.migrateUser(request, userToken, authToken);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/user/v1/ext/signup")
	public ResponseEntity<SBApiResponse> userSignup(@RequestBody Map<String, Object> request) {
		SBApiResponse response = profileService.userSignup(request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/user/v1/bulkupload")
	public ResponseEntity<SBApiResponse> bulkUpload(@RequestParam(value = "file", required = true) MultipartFile multipartFile,
			@RequestHeader(Constants.X_AUTH_USER_ORG_ID) String rootOrgId,
			@RequestHeader(Constants.X_AUTH_USER_CHANNEL) String channel,
			@RequestHeader(Constants.X_AUTH_USER_ID) String userId) throws UnsupportedEncodingException {
		log.info(String.format("bulkupload channel name:%s,OrgId:%s",
				URLDecoder.decode(channel, "UTF-8"), rootOrgId));
		SBApiResponse uploadResponse = profileService.bulkUpload(multipartFile, rootOrgId, URLDecoder.decode(channel, "UTF-8"), userId);
		return new ResponseEntity<>(uploadResponse, uploadResponse.getResponseCode());
	}

	@GetMapping("/user/v1/bulkupload/{orgId}")
	public ResponseEntity<SBApiResponse> getBulkUploadDetails(@PathVariable("orgId") String orgId) {
		SBApiResponse response = profileService.getBulkUploadDetails(orgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/v1/enrollement/report")
	public ResponseEntity<SBApiResponse> getUserEnrollmentReport() {
		SBApiResponse response = profileService.getUserEnrollmentReport();
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/v1/report")
	public ResponseEntity<SBApiResponse> generateUserReport() {
		SBApiResponse response = profileService.getUserReport();
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/v1/bulkuser/download/{fileName}")
	public ResponseEntity<Resource> downloadFile(@PathVariable("fileName") String fileName) {
		return profileService.downloadFile(fileName);
	}

	@GetMapping("/user/v1/groups")
	public ResponseEntity<SBApiResponse> getGroupList() throws Exception {
		SBApiResponse response = profileService.getGroupList();
		return new ResponseEntity<>(response, response.getResponseCode());
	}
	@PostMapping("/user/admin/patch")
	public ResponseEntity<SBApiResponse> profileMDOAdminUpdate(
			@RequestHeader(value = Constants.X_AUTH_TOKEN, required = false) String userToken,
			@RequestHeader(value = Constants.AUTH_TOKEN, required = false) String authToken,
			@RequestHeader(value = Constants.X_AUTH_USER_ORG_ID, required = false) String rootOrgId,
			@RequestBody Map<String, Object> request) throws Exception {
		SBApiResponse response = profileService.profileMDOAdminUpdate(request, userToken, authToken, rootOrgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/user/v1/profile/externalsystem/update")
	public ResponseEntity<SBApiResponse> profileExternalSystemUpdate(
			@RequestHeader(value = Constants.X_AUTH_TOKEN, required = false) String userToken,
			@RequestBody Map<String, Object> request) {
		SBApiResponse response = profileService.profileExternalSystemUpdate(request, userToken);
		return new ResponseEntity<>(response, response.getResponseCode());
	}
}

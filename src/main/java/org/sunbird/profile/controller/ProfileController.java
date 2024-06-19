package org.sunbird.profile.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.sunbird.profile.service.UserBulkUploadService;

import javax.swing.plaf.PanelUI;

@RestController
public class ProfileController {

	@Autowired
	private ProfileService profileService;

	private Logger log = LoggerFactory.getLogger(getClass().getName());

	@PostMapping("/user/patch")
	public ResponseEntity<?> profileUpdate(
			@RequestHeader(value = Constants.X_AUTH_TOKEN, required = true) String userToken,
			@RequestHeader(value = Constants.AUTH_TOKEN, required = false) String authToken,
			@RequestHeader(value = Constants.X_AUTH_USER_ORG_ID, required = false) String rootOrgId,
			@RequestBody Map<String, Object> request)  {
		SBApiResponse response = profileService.profileUpdate(request, userToken, authToken, rootOrgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PatchMapping("/org/v1/profile/patch")
	public ResponseEntity<?> orgProfileUpdate(@RequestBody Map<String, Object> request) throws Exception {
		SBApiResponse response = profileService.orgProfileUpdate(request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/org/v1/profile/read")
	public ResponseEntity<?> orgProfileRead(@RequestParam String orgId) throws Exception {
		SBApiResponse response = profileService.orgProfileRead(orgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/v1/basicInfo")
	public ResponseEntity<?> userBasicInfo(@RequestHeader(Constants.X_AUTH_USER_ID) String userId) {
		SBApiResponse response = profileService.userBasicInfo(userId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/user/v1/basicProfileUpdate")
	public ResponseEntity<?> parichayProfileUpdate(@RequestBody Map<String, Object> request) {
		SBApiResponse response = profileService.userBasicProfileUpdate(request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/v1/autocomplete/{searchTerm}")
	public ResponseEntity<?> userAutoComplete(@PathVariable("searchTerm") String searchTerm) {
		SBApiResponse response = profileService.userAutoComplete(searchTerm);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/v1/admin/autocomplete/{searchTerm}")
	public ResponseEntity<?> userMdoAutoComplete(@PathVariable("searchTerm") String searchTerm,
												 @RequestHeader(Constants.X_AUTH_USER_ORG_ID) String rootOrgId) {
		SBApiResponse response = profileService.userAdminAutoComplete(searchTerm, rootOrgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PatchMapping("/user/v1/migrate")
	private ResponseEntity<?> adminMigrateUser(@RequestHeader(Constants.X_AUTH_TOKEN) String userToken,
			@RequestHeader(Constants.AUTH_TOKEN) String authToken, @RequestBody Map<String, Object> request) {
		SBApiResponse response = profileService.migrateUser(request, userToken, authToken);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/user/v1/ext/signup")
	public ResponseEntity<?> userSignup(@RequestBody Map<String, Object> request) {
		SBApiResponse response = profileService.userSignup(request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/user/v1/bulkupload")
	public ResponseEntity<?> bulkUpload(@RequestParam(value = "file", required = true) MultipartFile multipartFile,
			@RequestHeader(Constants.X_AUTH_USER_ORG_ID) String rootOrgId,
			@RequestHeader(Constants.X_AUTH_USER_CHANNEL) String channel,
			@RequestHeader(Constants.X_AUTH_USER_ID) String userId,
            @RequestHeader(Constants.X_AUTH_TOKEN) String userAuthToken) throws UnsupportedEncodingException {
		log.info(String.format("bulkupload channel name:%s,OrgId:%s",
				URLDecoder.decode(channel, "UTF-8"), rootOrgId));
		SBApiResponse uploadResponse = profileService.bulkUpload(multipartFile, rootOrgId, URLDecoder.decode(channel, "UTF-8"), userId, userAuthToken);
		return new ResponseEntity<>(uploadResponse, uploadResponse.getResponseCode());
	}

	@GetMapping("/user/v1/bulkupload/{orgId}")
	public ResponseEntity<?> getBulkUploadDetails(@PathVariable("orgId") String orgId) {
		SBApiResponse response = profileService.getBulkUploadDetails(orgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/v1/enrollement/report")
	public ResponseEntity<?> getUserEnrollmentReport() {
		SBApiResponse response = profileService.getUserEnrollmentReport();
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/v1/report")
	public ResponseEntity<?> generateUserReport() {
		SBApiResponse response = profileService.getUserReport();
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/v1/bulkuser/download/{fileName}")
	public ResponseEntity<?> downloadFile(@PathVariable("fileName") String fileName) {
		return profileService.downloadFile(fileName);
	}

	@GetMapping("/user/v1/groups")
	public ResponseEntity<?> getGroupList() throws Exception {
		SBApiResponse response = profileService.getGroupList();
		return new ResponseEntity<>(response, response.getResponseCode());
	}
	@PostMapping("/user/admin/patch")
	public ResponseEntity<?> profileMDOAdminUpdate(
			@RequestHeader(value = Constants.X_AUTH_TOKEN, required = false) String userToken,
			@RequestHeader(value = Constants.AUTH_TOKEN, required = false) String authToken,
			@RequestHeader(value = Constants.X_AUTH_USER_ORG_ID, required = false) String rootOrgId,
			@RequestBody Map<String, Object> request) throws Exception {
		SBApiResponse response = profileService.profileMDOAdminUpdate(request, userToken, authToken, rootOrgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/user/v1/profile/externalsystem/update")
	public ResponseEntity<?> profileExternalSystemUpdate(
			@RequestHeader(value = Constants.X_AUTH_TOKEN, required = false) String userToken,
			@RequestBody Map<String, Object> request) {
		SBApiResponse response = profileService.profileExternalSystemUpdate(request, userToken);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/v2/user/patch")
	public ResponseEntity<?> profileUpdateV2(
			@RequestHeader(value = Constants.X_AUTH_TOKEN, required = true) String userToken,
			@RequestHeader(value = Constants.AUTH_TOKEN, required = false) String authToken,
			@RequestHeader(value = Constants.X_AUTH_USER_ORG_ID, required = false) String rootOrgId,
			@RequestBody Map<String, Object> request) {
		SBApiResponse response = profileService.profileUpdateV2(request, userToken, authToken, rootOrgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/user/v2/bulkupload")
	public ResponseEntity<?> bulkUploadV2(@RequestParam(value = "file", required = true) MultipartFile multipartFile,
										@RequestHeader(Constants.X_AUTH_USER_ORG_ID) String rootOrgId,
										@RequestHeader(Constants.X_AUTH_USER_CHANNEL) String channel,
										@RequestHeader(Constants.X_AUTH_USER_ID) String userId,
										@RequestHeader(Constants.X_AUTH_TOKEN) String userAuthToken) throws UnsupportedEncodingException {
		log.info(String.format("bulkupload channel name:%s,OrgId:%s",
				URLDecoder.decode(channel, "UTF-8"), rootOrgId));
		SBApiResponse uploadResponse = profileService.bulkUpload(multipartFile, rootOrgId, URLDecoder.decode(channel, "UTF-8"), userId, userAuthToken);
		return new ResponseEntity<>(uploadResponse, uploadResponse.getResponseCode());
	}
}

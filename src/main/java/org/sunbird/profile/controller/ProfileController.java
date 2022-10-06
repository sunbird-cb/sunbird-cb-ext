package org.sunbird.profile.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

	@PostMapping("/user/patch")
	public ResponseEntity<?> profileUpdate(
			@RequestHeader(value = Constants.X_AUTH_TOKEN, required = false) String userToken,
			@RequestHeader(value = Constants.AUTH_TOKEN, required = false) String authToken,
			@RequestBody Map<String, Object> request) throws Exception {
		SBApiResponse response = profileService.profileUpdate(request, userToken, authToken);
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
			@RequestHeader(Constants.X_AUTH_USER_ORG_NAME) String orgName,
			@RequestHeader(Constants.X_AUTH_USER_ID) String userId) {
		SBApiResponse uploadResponse = profileService.bulkUpload(multipartFile, rootOrgId, orgName, userId);
		return new ResponseEntity<>(uploadResponse, uploadResponse.getResponseCode());
	}

	@GetMapping("/user/v1/bulkupload/{orgId}")
	public ResponseEntity<?> getBulkUploadDetails(@PathVariable("orgId") String orgId) {
		SBApiResponse response = profileService.getBulkUploadDetails(orgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}
}

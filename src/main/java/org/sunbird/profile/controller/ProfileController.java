package org.sunbird.profile.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.profile.service.ProfileService;
import java.util.Map;

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
}

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
	public ResponseEntity<?> profileUpdate(@RequestHeader(value = Constants.X_AUTH_TOKEN, required = false) String userToken,
			@RequestHeader(value = Constants.AUTH_TOKEN, required = false) String authToken, @RequestBody Map<String, Object> request)
			throws Exception {
		SBApiResponse response = profileService.profileUpdate(request, userToken, authToken);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PatchMapping("/org/v1/profile/patch")
	public ResponseEntity<?> orgProfileUpdate(@RequestBody Map<String, Object> request)
			throws Exception {
		SBApiResponse response = profileService.orgProfileUpdate(request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/org/v1/profile/read")
	public ResponseEntity<?> orgProfileRead(@RequestParam String orgId)
			throws Exception {
		SBApiResponse response = profileService.orgProfileRead(orgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/user/migrate")
	public ResponseEntity<?> adminMigrateUser(@RequestHeader(value = Constants.X_AUTH_TOKEN, required = false) String userToken,
										   @RequestHeader(value = Constants.AUTH_TOKEN, required = false) String authToken, @RequestBody Map<String, Object> request)
			throws Exception {
		SBApiResponse response = profileService.migrateUser(request, userToken, authToken);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

}

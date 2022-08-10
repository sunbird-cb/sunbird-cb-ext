package org.sunbird.profile.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.profile.model.User;
import org.sunbird.profile.service.ProfileService;
import org.sunbird.user.registration.model.UserRegistration;
import org.sunbird.user.registration.model.UserRegistrationInfo;

import java.util.Map;

@RestController
public class ProfileController {

	@Autowired
	private ProfileService profileService;

	@PostMapping("/user/patch")
	public ResponseEntity<?> profileUpdate(@RequestHeader(Constants.X_AUTH_TOKEN) String userToken,
			@RequestHeader(Constants.AUTH_TOKEN) String authToken, @RequestBody Map<String, Object> request)
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
	public ResponseEntity<?> orgProfileRead(@RequestParam String orgId) throws Exception {
		SBApiResponse response = profileService.orgProfileRead(orgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}
	@PostMapping("/user/v1/signup")
	public ResponseEntity<?> signupUser(@RequestBody Map<String,Object> request) throws Exception{
		SBApiResponse response = profileService.signupUser(request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	/*@GetMapping("/user/v1/readuser")
	public ResponseEntity<?> getUserDetails(@RequestHeader(Constants.X_AUTH_TOKEN) String userToken,
			@RequestHeader(Constants.AUTH_TOKEN) String authToken,@RequestParam String userId) throws Exception {
		SBApiResponse response = profileService.getUserDetailsById(userId,authToken, userToken);
		return new ResponseEntity<>(response.toString(), response.getResponseCode());
	}

	@PatchMapping("/user/v1/update")
	public ResponseEntity<?> updateUserById(@RequestBody Map<String,Object> request)throws Exception{
		String msg=profileService.updateUser(request);
		return new ResponseEntity<>(msg,HttpStatus.OK);
	}*/
}

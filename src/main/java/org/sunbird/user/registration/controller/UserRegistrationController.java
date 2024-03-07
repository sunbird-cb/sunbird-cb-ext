package org.sunbird.user.registration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.user.registration.model.UserRegistrationInfo;
import org.sunbird.user.registration.service.UserRegistrationService;
import java.util.Map;

/**
 * Provides REST APIs creating and updating the User Registration
 * 
 * @author karthik
 *
 */
@RestController
public class UserRegistrationController {

	UserRegistrationService userRegService;
	@Autowired
	public UserRegistrationController(UserRegistrationService userRegService) {
		this.userRegService = userRegService;
	}

	@PostMapping("/user/registration/v1/register")
	public ResponseEntity<SBApiResponse> registerUser(@RequestBody UserRegistrationInfo userRegIno) {
		SBApiResponse response = userRegService.registerUser(userRegIno);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/registration/v1/getUserRegistrationDetails")
	public ResponseEntity<SBApiResponse> getUserRegistrationDetails(@RequestParam String regCode) {
		SBApiResponse response = userRegService.getUserRegistrationDetails(regCode);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/registration/v1/getDeptDetails")
	public ResponseEntity<SBApiResponse> getDeptDetails() {
		SBApiResponse response = userRegService.getDeptDetails();
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/user/otp/v1/generate")
	public ResponseEntity<SBApiResponse> generateOTP(@RequestBody Map<String, Object> otpRequests) {
		SBApiResponse response = userRegService.generateOTP(otpRequests);
		return new ResponseEntity<>(response, response.getResponseCode());
	}
}

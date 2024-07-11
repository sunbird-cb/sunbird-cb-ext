package org.sunbird.user.registration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
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

	@Autowired
	UserRegistrationService userRegService;

	@PostMapping("/user/registration/v1/register")
	public ResponseEntity<SBApiResponse> registerUser(@RequestBody UserRegistrationInfo userRegIno) throws Exception {
		SBApiResponse response = userRegService.registerUser(userRegIno);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/registration/v1/getUserRegistrationDetails")
	public ResponseEntity<SBApiResponse> getUserRegistrationDetails(@RequestParam String regCode) throws Exception {
		SBApiResponse response = userRegService.getUserRegistrationDetails(regCode);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/registration/v1/getDeptDetails")
	public ResponseEntity<SBApiResponse> getDeptDetails() throws Exception {
		SBApiResponse response = userRegService.getDeptDetails();
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/user/otp/v1/generate")
	public ResponseEntity<SBApiResponse> generateOTP(@RequestBody Map<String, Object> otpRequests) throws Exception {
		SBApiResponse response = userRegService.generateOTP(otpRequests);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/user/email/approved/domains")
	public ResponseEntity<SBApiResponse> getApprovedDomain() throws Exception {
		SBApiResponse response = userRegService.getApprovedDomains();
		return new ResponseEntity<>(response, response.getResponseCode());
	}
}

package org.sunbird.user.registration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.user.registration.model.UserRegistrationInfo;
import org.sunbird.user.registration.service.UserRegistrationService;

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
}

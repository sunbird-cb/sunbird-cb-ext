package org.sunbird.user.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.telemetry.model.LastLoginInfo;
import org.sunbird.user.service.UserUtilityService;

@RestController
public class UserUtilityController {

	UserUtilityService userUtilService;
	@Autowired
	public UserUtilityController(UserUtilityService userUtilService) {
		this.userUtilService = userUtilService;
	}

	@PutMapping("/user/v1/updateLogin")
	public ResponseEntity<Map<String, Object>> updateLogin(@RequestBody LastLoginInfo userLoginInfo)
			throws NumberFormatException {
		return new ResponseEntity<>(userUtilService.updateLogin(userLoginInfo), HttpStatus.OK);
	}

	@PostMapping("/user/v1/content/recommend")
	public ResponseEntity<SBApiResponse> recommendContent(@RequestHeader(Constants.USER_TOKEN) String authUserToken, @RequestBody Map<String, Object> request) {
		SBApiResponse response = userUtilService.recommendContent(authUserToken, request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}
}
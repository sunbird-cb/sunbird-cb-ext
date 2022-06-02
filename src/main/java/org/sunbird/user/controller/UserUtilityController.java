package org.sunbird.user.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.telemetry.model.UserInfo;
import org.sunbird.user.service.UserUtilityService;

@RestController
public class UserUtilityController {

	@Autowired
	UserUtilityService userUtilService;

	@PutMapping("/user/v1/updateLogin")
	public ResponseEntity<Map<String, Object>> updateLogin(@RequestBody UserInfo userLoginInfo)
			throws NumberFormatException {
		return new ResponseEntity<>(userUtilService.updateLogin(userLoginInfo), HttpStatus.OK);
	}

	/**
	 * API to create user
	 * @param userInfo
	 * @return
	 * @throws NumberFormatException
	 */
	@PostMapping("/user/v1/createUser")
	public ResponseEntity<Boolean> createUser(@RequestBody UserInfo userInfo, @RequestHeader("X-Authenticated-User-Token") String xAuthToken) throws NumberFormatException {
		return new ResponseEntity<>(userUtilService.createUser(userInfo, xAuthToken), HttpStatus.OK);
	}
}
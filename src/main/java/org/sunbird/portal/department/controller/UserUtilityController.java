package org.sunbird.portal.department.controller;

import java.text.ParseException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.service.UserUtilityService;
import org.sunbird.portal.department.model.LastLoginInfo;

@RestController
public class UserUtilityController {
	@Autowired
	UserUtilityService userUtilService;

	@PutMapping("/user/v1/updateLogin")
	public ResponseEntity<Map<String, Object>> updateLogin(@RequestBody LastLoginInfo userLoginInfo)
			throws NumberFormatException, ParseException {
		return new ResponseEntity<>(userUtilService.updateLogin(userLoginInfo), HttpStatus.OK);
	}
}
package org.sunbird.progress.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.util.Constants;
import org.sunbird.progress.model.MandatoryContentResponse;
import org.sunbird.progress.service.MandatoryContentService;

@RestController
public class MandatoryContentController {

	@Autowired
	private MandatoryContentService service;

	/**
	 * @param rootOrg
	 * @param userId
	 * @return Status of mandatory content
	 * @throws Exception
	 */
	@GetMapping("/v1/check/mandatoryContentStatus")
	public ResponseEntity<MandatoryContentResponse> getMandatoryContentStatus(
			@RequestHeader("xAuthUser") String authUserToken, @RequestHeader("rootOrg") String rootOrg,
			@RequestHeader("org") String org, @RequestHeader("wid") String userId) {
		return new ResponseEntity<>(service.getMandatoryContentStatusForUser(authUserToken, rootOrg, org, userId),
				HttpStatus.OK);
	}

	@PostMapping("/v1/progress/getUserProgress")
	public ResponseEntity<Map<String, Object>> getUserProgress(@RequestBody SunbirdApiRequest requestBody,
			@RequestHeader(Constants.USER_TOKEN) String authUserToken) {
		return new ResponseEntity<>(service.getUserProgress(requestBody, authUserToken), HttpStatus.OK);
	}

}

package org.sunbird.assessment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.assessment.service.FlagService;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;

import javax.validation.Valid;
import java.util.Map;

@RestController
public class FlagController {

	@Autowired
	FlagService flagService;

	@PostMapping("/v1/offensive/data/flag/create")
	public ResponseEntity<SBApiResponse> createFlag(
			@Valid @RequestBody Map<String, Object> requestBody, @RequestHeader(Constants.X_AUTH_TOKEN) String token) throws Exception {
		SBApiResponse readResponse = flagService.createFlag(requestBody, token);
		return new ResponseEntity<>(readResponse, readResponse.getResponseCode());
	}

	@PatchMapping("/v1/offensive/data/flag/update")
	public ResponseEntity<SBApiResponse> updateFlag(@RequestHeader(Constants.X_AUTH_TOKEN) String token,
													@Valid @RequestBody Map<String, Object> request) {
		SBApiResponse response = flagService.updateFlag(token, request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/v1/offensive/data/flag/getFlaggedData")
	public ResponseEntity<SBApiResponse> getFlaggedData(@RequestHeader(Constants.X_AUTH_TOKEN) String token) throws Exception {
		SBApiResponse readResponse = flagService.getFlaggedData(token);
		return new ResponseEntity<>(readResponse, readResponse.getResponseCode());
	}


}

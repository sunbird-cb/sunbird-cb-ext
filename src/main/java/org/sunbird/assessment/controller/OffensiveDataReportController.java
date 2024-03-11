package org.sunbird.assessment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.assessment.service.OffensiveDataFlagService;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;

import javax.validation.Valid;
import java.util.Map;

@RestController
public class OffensiveDataReportController {

	OffensiveDataFlagService offensiveDataFlagService;
	@Autowired
	public OffensiveDataReportController(OffensiveDataFlagService offensiveDataFlagService) {
		this.offensiveDataFlagService = offensiveDataFlagService;
	}

	@PostMapping("/v1/offensive/data/flag")
	public ResponseEntity<SBApiResponse> createFlag(
			@Valid @RequestBody Map<String, Object> requestBody, @RequestHeader(Constants.X_AUTH_TOKEN) String token) {
		SBApiResponse readResponse = offensiveDataFlagService.createFlag(requestBody, token);
		return new ResponseEntity<>(readResponse, readResponse.getResponseCode());
	}

	@PatchMapping("/v1/offensive/data/flag")
	public ResponseEntity<SBApiResponse> updateFlag(@RequestHeader(Constants.X_AUTH_TOKEN) String token,
													@Valid @RequestBody Map<String, Object> request) {
		SBApiResponse response = offensiveDataFlagService.updateFlag(token, request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/v1/offensive/data/flag/getFlaggedData")
	public ResponseEntity<SBApiResponse> getFlaggedData(@RequestHeader(Constants.X_AUTH_TOKEN) String token) {
		SBApiResponse readResponse = offensiveDataFlagService.getFlaggedData(token);
		return new ResponseEntity<>(readResponse, readResponse.getResponseCode());
	}


}

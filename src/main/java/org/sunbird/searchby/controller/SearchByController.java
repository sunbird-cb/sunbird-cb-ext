package org.sunbird.searchby.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.assessment.dto.AssessmentSubmissionDTO;
import org.sunbird.searchby.dto.SearchByFilter;
import org.sunbird.searchby.service.SearchByService;

import javax.validation.Valid;

@RestController
public class SearchByController<requestBody> {

	@Autowired
	private SearchByService competencyService;

	@GetMapping("/v1/browseByCompetency")
	public ResponseEntity<?> browseByCompetency(@RequestHeader("x-authenticated-user-token") String authUserToken)
			throws Exception {
		return new ResponseEntity<>(competencyService.getCompetencyDetails(authUserToken), HttpStatus.OK);
	}

	@PostMapping("/v1/browseByCompetency")
	public ResponseEntity<?> browseByCompetencyByFilter(@RequestHeader("x-authenticated-user-token") String authUserToken,
														@Valid @RequestBody SearchByFilter filter)
			throws Exception {
		if (filter==null){
			throw new Exception("Invalid Request");
		}
		return new ResponseEntity<>(competencyService.getCompetencyDetailsByFilter(authUserToken, filter), HttpStatus.OK);
	}

	@GetMapping("/v1/browseByProvider")
	public ResponseEntity<?> browseByProvider(@RequestHeader("x-authenticated-user-token") String authUserToken)
			throws Exception {
		return new ResponseEntity<>(competencyService.getProviderDetails(authUserToken), HttpStatus.OK);
	}
}

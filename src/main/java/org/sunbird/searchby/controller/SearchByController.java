package org.sunbird.searchby.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.searchby.service.SearchByService;

@RestController
public class SearchByController {

	@Autowired
	private SearchByService competencyService;

	@GetMapping("/v1/browseByCompetency")
	public ResponseEntity<?> browseByCompetency(@RequestHeader("x-authenticated-user-token") String authUserToken)
			throws Exception {
		return new ResponseEntity<>(competencyService.getCompetencyDetails(authUserToken), HttpStatus.OK);
	}

	@GetMapping("/v1/browseByProvider")
	public ResponseEntity<?> browseByProvider(@RequestHeader("x-authenticated-user-token") String authUserToken)
			throws Exception {
		return new ResponseEntity<>(competencyService.getProviderDetails(authUserToken), HttpStatus.OK);
	}
}

package org.sunbird.searchby.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.FracApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.searchby.service.SearchByService;

@RestController
public class SearchByController {

	@Autowired
	private SearchByService searchByService;

	@GetMapping("/v1/browseByCompetency")
	public ResponseEntity<?> browseByCompetency(@RequestHeader(Constants.X_AUTH_TOKEN) String authUserToken)
			throws Exception {
		return new ResponseEntity<>(searchByService.getCompetencyDetails(authUserToken), HttpStatus.OK);
	}

	@GetMapping("/v1/browseByProvider")
	public ResponseEntity<?> browseByProvider(@RequestHeader(Constants.X_AUTH_TOKEN) String authUserToken)
			throws Exception {
		return new ResponseEntity<>(searchByService.getProviderDetails(authUserToken), HttpStatus.OK);
	}

	@GetMapping("/v1/listPositions")
	public ResponseEntity<?> listPositions(@RequestHeader(Constants.X_AUTH_TOKEN) String userToken) {
		FracApiResponse response = searchByService.listPositions(userToken);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatusInfo().getStatusCode()));
	}

	@GetMapping("/v1/competenciesByOrg/{orgId}")
	public ResponseEntity<?> listOfCompetenciesByOrg(@PathVariable String orgId, @RequestHeader(Constants.X_AUTH_TOKEN) String userToken) {
		return new ResponseEntity<>(searchByService.listCompetenciesByOrg(orgId, userToken), HttpStatus.OK);
	}
}

package org.sunbird.searchby.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.FracApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.searchby.model.CompetencyInfo;
import org.sunbird.searchby.model.ProviderInfo;
import org.sunbird.searchby.service.SearchByService;

import java.util.Collection;

@RestController
public class SearchByController {


	private final SearchByService searchByService;
	@Autowired
	public SearchByController(SearchByService searchByService) {
		this.searchByService = searchByService;
	}

	@GetMapping("/v1/browseByCompetency")
	public ResponseEntity<Collection<CompetencyInfo>> browseByCompetency(@RequestHeader(Constants.X_AUTH_TOKEN) String authUserToken)
			throws Exception {
		return new ResponseEntity<>(searchByService.getCompetencyDetails(authUserToken), HttpStatus.OK);
	}

	@GetMapping("/v1/browseByProvider")
	public ResponseEntity<Collection<ProviderInfo>> browseByProvider(@RequestHeader(Constants.X_AUTH_TOKEN) String authUserToken)
			throws Exception {
		return new ResponseEntity<>(searchByService.getProviderDetails(authUserToken), HttpStatus.OK);
	}

	@GetMapping("/v1/listPositions")
	public ResponseEntity<FracApiResponse> listPositions(@RequestHeader(Constants.X_AUTH_TOKEN) String userToken) {
		FracApiResponse response = searchByService.listPositions(userToken);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatusInfo().getStatusCode()));
	}
}

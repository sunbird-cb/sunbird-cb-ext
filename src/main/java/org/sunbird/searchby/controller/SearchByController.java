package org.sunbird.searchby.controller;

import java.util.Collection;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.exception.MyOwnRuntimeException;
import org.sunbird.searchby.model.CompetencyInfo;
import org.sunbird.searchby.model.ProviderInfo;
import org.sunbird.searchby.model.SearchByFilter;
import org.sunbird.searchby.service.SearchByService;

import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
public class SearchByController {

	@Autowired
	private SearchByService searchByService;

	// v1/readCompetency
	@GetMapping("/v1/browseByCompetency")
	public ResponseEntity<Collection<CompetencyInfo>> browseByCompetency(
			@RequestHeader("x-authenticated-user-token") String authUserToken)
			throws JsonProcessingException, MyOwnRuntimeException {
		return new ResponseEntity<>(searchByService.getCompetencyDetails(authUserToken), HttpStatus.OK);
	}

	@GetMapping("/v1/browseByProvider")
	public ResponseEntity<Collection<ProviderInfo>> browseByProvider(
			@RequestHeader("x-authenticated-user-token") String authUserToken) throws Exception {
		return new ResponseEntity<>(searchByService.getProviderDetails(authUserToken), HttpStatus.OK);
	}
}

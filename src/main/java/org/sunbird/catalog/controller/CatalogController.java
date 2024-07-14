package org.sunbird.catalog.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.catalog.model.Catalog;
import org.sunbird.catalog.service.CatalogServiceImpl;
import org.sunbird.common.model.SBApiResponse;

import java.util.Map;

@RestController
@RequestMapping("/v1/catalog")
public class CatalogController {
	@Autowired
	private CatalogServiceImpl catalogService;

	@GetMapping("/")
	public ResponseEntity<Catalog> getCatalog(@RequestHeader("x-authenticated-user-token") String authUserToken,
			@RequestParam(name = "consumption", required = false) boolean isEnrichConsumption) {
		return new ResponseEntity<>(catalogService.getCatalog(authUserToken, isEnrichConsumption), HttpStatus.OK);
	}

	@GetMapping("/sector")
	public ResponseEntity<SBApiResponse> getSectors() {
		SBApiResponse response = catalogService.getSectors();
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/sector/read/{sectorId}")
	public ResponseEntity<SBApiResponse> readSector(@PathVariable("sectorId") String sectorId) {
		SBApiResponse response = catalogService.readSector(sectorId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/sector/create")
	public ResponseEntity<SBApiResponse> createSector(@RequestBody Map<String, Object> request) {
		SBApiResponse response = catalogService.createSector(request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/subsector/create")
	public ResponseEntity<SBApiResponse> createSubSector(@RequestBody Map<String, Object> request) {
		SBApiResponse response = catalogService.createSubSector(request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}
}

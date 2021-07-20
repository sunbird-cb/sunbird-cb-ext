package org.sunbird.catalog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.catalog.model.Catalog;
import org.sunbird.catalog.service.CatalogServiceImpl;

@RestController
@RequestMapping("/v1/catalog")
public class CatalogController {
	@Autowired
	private CatalogServiceImpl catalogService;

	@GetMapping("/")
	public ResponseEntity<Catalog> getCatalog(@RequestHeader("x-authenticated-user-token") String authUserToken,
											  @RequestParam(name = "consumption", required = false) boolean isEnrichConsumption){
		return new ResponseEntity<>(catalogService.getCatalog(authUserToken, isEnrichConsumption), HttpStatus.OK);
	}
}

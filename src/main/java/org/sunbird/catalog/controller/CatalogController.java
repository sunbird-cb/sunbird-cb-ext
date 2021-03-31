package org.sunbird.catalog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.catalog.service.CatalogServiceImpl;

@RestController
@RequestMapping("/v1/catalog")
public class CatalogController {
	@Autowired
	private CatalogServiceImpl catalogService;

	@GetMapping("/")
	public ResponseEntity<?> getCatalog(@RequestHeader("xAuthUser") String authUserToken) throws Exception {
		String framework = "igot", category = "Taxonomy";
		return new ResponseEntity<>(catalogService.getCatalog(authUserToken, framework, category), HttpStatus.OK);
	}
}

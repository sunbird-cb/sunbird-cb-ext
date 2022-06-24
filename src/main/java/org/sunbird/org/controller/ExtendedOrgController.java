package org.sunbird.org.controller;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.org.service.ExtendedOrgService;

@RestController
public class ExtendedOrgController {

	@Autowired
	private ExtendedOrgService orgService;

	@PostMapping("/org/ext/v1/create")
	public ResponseEntity<SBApiResponse> createOrg(@RequestBody Map<String, Object> orgRequest,
			@RequestHeader(Constants.X_AUTH_TOKEN) String userToken) throws Exception {
		SBApiResponse response = orgService.createOrg(orgRequest, userToken);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/org/v1/list")
	public ResponseEntity<SBApiResponse> listOrg() {
		SBApiResponse response = orgService.listOrg(StringUtils.EMPTY);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/org/v1/list/{parentMapId}")
	public ResponseEntity<SBApiResponse> listOrg(@PathVariable("parentMapId") String parentMapId) {
		SBApiResponse response = orgService.listOrg(parentMapId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/org/v1/ext/search")
	public ResponseEntity<?> orgExtSearch(@RequestBody Map<String, Object> request) throws Exception {
		SBApiResponse response = orgService.orgExtSearch(request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}
}

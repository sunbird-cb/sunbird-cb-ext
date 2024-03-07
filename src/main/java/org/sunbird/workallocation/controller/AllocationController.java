package org.sunbird.workallocation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.Response;
import org.sunbird.workallocation.model.SearchCriteria;
import org.sunbird.workallocation.model.WorkAllocationDTO;
import org.sunbird.workallocation.service.AllocationService;

@RestController
@RequestMapping("/v1/workallocation")
public class AllocationController {

	@Autowired
	private AllocationService allocationService;

	@PostMapping("/add")
	public ResponseEntity<Response> add(@RequestHeader("Authorization") String authUserToken,
			@RequestHeader("userId") String userId, @RequestBody WorkAllocationDTO workAllocation) {
		return new ResponseEntity<>(allocationService.addWorkAllocation(authUserToken, userId, workAllocation),
				HttpStatus.OK);
	}

	@PostMapping("/update")
	public ResponseEntity<Response> update(@RequestHeader("Authorization") String authUserToken,
			@RequestHeader("userId") String userId, @RequestBody WorkAllocationDTO workAllocation) {
		return new ResponseEntity<>(allocationService.updateWorkAllocation(authUserToken, userId, workAllocation),
				HttpStatus.OK);
	}

	@PostMapping("/getUsers")
	public ResponseEntity<Response> getUsers(@RequestBody SearchCriteria searchCriteria) {
		return new ResponseEntity<>(allocationService.getUsers(searchCriteria), HttpStatus.OK);
	}

	@GetMapping("/users/autocomplete")
	public ResponseEntity<Response> userAutoComplete(@RequestParam("searchTerm") String searchTerm) {
		return new ResponseEntity<>(allocationService.userAutoComplete(searchTerm), HttpStatus.OK);
	}

	@GetMapping(value = "/getWAPdf/{userId}/{waId}", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<?> getWAPdf(@PathVariable("userId") String userId, @PathVariable("waId") String waId)
			throws Exception {
		byte[] out = null;
		try {
			out = allocationService.getWaPdf(userId, waId);
		} catch (Exception e) {
		}

		if (out == null) {
			throw new InternalError("Failed to generate PDF file.");
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.add("Content-Disposition", "inline; filename=wa_report.pdf");

		ResponseEntity<?> response = new ResponseEntity<>(out, headers, HttpStatus.OK);

		return response;
	}
}

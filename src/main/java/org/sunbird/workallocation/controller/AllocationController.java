package org.sunbird.workallocation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

	@GetMapping("/getWAPdf/{userId}/{waId}")
	public ResponseEntity<?> getWAPdf(@PathVariable("userId") String userId, @PathVariable("waId") String waId) {
		return new ResponseEntity<>(allocationService.getWaPdf(userId, waId), HttpStatus.OK);
	}
}

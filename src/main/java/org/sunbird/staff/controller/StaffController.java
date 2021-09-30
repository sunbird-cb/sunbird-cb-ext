package org.sunbird.staff.controller;

import javax.validation.Valid;

import org.sunbird.common.model.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.staff.model.StaffInfo;
import org.sunbird.staff.service.StaffService;

@RestController
public class StaffController {

	@Autowired
	StaffService staffService;

	@PostMapping("/staff/position")
	public ResponseEntity<Response> createStaffDetails(@RequestHeader("userId") String userId,
			@Valid @RequestBody StaffInfo requestBody) throws Exception {
		return new ResponseEntity<>(staffService.submitStaffDetails(requestBody, userId), HttpStatus.CREATED);
	}

	@GetMapping("/staff/position/{orgId}")
	public ResponseEntity<Response> getStaffDetails(@PathVariable("orgId") String orgId) throws Exception {
		return new ResponseEntity<Response>(staffService.getStaffDetails(orgId), HttpStatus.OK);
	}

	@PatchMapping("/staff/position")
	public ResponseEntity<Response> updateStaffDetails(@RequestHeader("userId") String userId,
			@Valid @RequestBody StaffInfo requestBody) throws Exception {
		return new ResponseEntity<>(staffService.updateStaffDetails(requestBody, userId), HttpStatus.OK);
	}

	@DeleteMapping("/staff/position")
	public ResponseEntity<Response> deleteStaffDetails(@RequestParam String orgId,
			@RequestParam(name = "id", required = true) String staffDetailsId) throws Exception {
		return new ResponseEntity<>(staffService.deleteStaffDetails(orgId, staffDetailsId), HttpStatus.OK);
	}

	@GetMapping("/orghistory/{orgId}/{auditType}")
	public ResponseEntity<Response> getStaffDetails(@PathVariable("orgId") String orgId,
			@PathVariable("auditType") String auditType) throws Exception {
		return new ResponseEntity<Response>(staffService.getAudit(orgId, auditType), HttpStatus.OK);
	}

}


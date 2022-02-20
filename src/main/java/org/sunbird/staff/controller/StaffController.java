package org.sunbird.staff.controller;

import java.io.IOException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.staff.model.StaffInfo;
import org.sunbird.staff.service.StaffService;

@RestController
public class StaffController {

	@Autowired
	StaffService staffService;

	@PostMapping("/staff/position")
	public ResponseEntity<SBApiResponse> createStaffDetails(@RequestHeader("x-authenticated-userid") String userId,
			@Valid @RequestBody StaffInfo requestBody) {
		SBApiResponse response = staffService.submitStaffDetails(requestBody, userId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@DeleteMapping("/staff/position")
	public ResponseEntity<SBApiResponse> deleteStaffDetails(@RequestParam String orgId,
			@RequestParam(name = "id", required = true) String staffDetailsId) {
		SBApiResponse response = staffService.deleteStaffDetails(orgId, staffDetailsId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/staff/position/{orgId}")
	public ResponseEntity<SBApiResponse> getStaffDetails(@PathVariable("orgId") String orgId) {
		SBApiResponse response = staffService.getStaffDetails(orgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/orghistory/{orgId}/staff")
	public ResponseEntity<SBApiResponse> getStaffHistoryDetails(@PathVariable("orgId") String orgId)
			throws IOException {
		SBApiResponse response = staffService.getStaffAudit(orgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PatchMapping("/staff/position")
	public ResponseEntity<SBApiResponse> updateStaffDetails(@RequestHeader("x-authenticated-userid") String userId,
			@Valid @RequestBody StaffInfo requestBody) {
		SBApiResponse response = staffService.updateStaffDetails(requestBody, userId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}
}

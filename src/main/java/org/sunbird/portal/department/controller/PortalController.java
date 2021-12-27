package org.sunbird.portal.department.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.portal.department.model.DeptPublicInfo;
import org.sunbird.portal.department.service.PortalService;
import org.sunbird.portal.department.service.SpvPortalService;

@RestController
public class PortalController {
	@Autowired
	PortalService portalService;

	@Autowired
	SpvPortalService spvPortalService;

	// ----------------- Public APIs --------------------
	@GetMapping("/portal/listDeptNames")
	public ResponseEntity<List<String>> getDeptNameList() {
		return new ResponseEntity<>(portalService.getDeptNameList(), HttpStatus.OK);
	}

	@GetMapping("/portal/getAllDept")
	public ResponseEntity<List<DeptPublicInfo>> getAllDepartment() {
		return new ResponseEntity<>(portalService.getAllDept(), HttpStatus.OK);
	}

	@GetMapping("/portal/deptSearch")
	public ResponseEntity<DeptPublicInfo> searchDepartment(
			@RequestParam(name = "friendlyName", required = true) String deptName) {
		return new ResponseEntity<>(portalService.searchDept(deptName), HttpStatus.OK);
	}

}
package org.sunbird.portal.department.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.portal.department.dto.DepartmentRole;
import org.sunbird.portal.department.dto.Role;
import org.sunbird.portal.department.service.RoleService;

import java.util.Set;

@RestController
public class RoleController {

	@Autowired
	RoleService roleService;

	@PostMapping("/portal/deptRole")
	public ResponseEntity<DepartmentRole> addDepartmentRole(@Valid @RequestBody DepartmentRole deptRole)
			throws Exception {
		return new ResponseEntity<>(roleService.addDepartmentRole(deptRole), HttpStatus.OK);
	}

	@DeleteMapping("/portal/deptRole/{dept_role_id}")
	public ResponseEntity<Boolean> removeDepartmentRole(@PathVariable("dept_role_id") Integer deptRoleId)
			throws Exception {
		return new ResponseEntity<>(roleService.removeDepartmentRole(deptRoleId), HttpStatus.OK);
	}

}
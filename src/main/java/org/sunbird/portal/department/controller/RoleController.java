package org.sunbird.portal.department.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.portal.department.service.RoleService;

@RestController
public class RoleController {

	@Autowired
	RoleService roleService;
}
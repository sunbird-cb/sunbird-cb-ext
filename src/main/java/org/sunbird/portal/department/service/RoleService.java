package org.sunbird.portal.department.service;

import org.sunbird.portal.department.dto.DepartmentRole;

public interface RoleService {

	DepartmentRole addDepartmentRole(DepartmentRole deptRole) throws Exception;

	boolean removeDepartmentRole(Integer deptRoleId) throws Exception;

}
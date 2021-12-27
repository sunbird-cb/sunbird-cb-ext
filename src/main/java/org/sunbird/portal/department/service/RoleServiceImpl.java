package org.sunbird.portal.department.service;

import javax.transaction.NotSupportedException;

import org.springframework.stereotype.Service;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.portal.department.dto.DepartmentRole;

@Service
public class RoleServiceImpl implements RoleService {

	private CbExtLogger logger = new CbExtLogger(RoleServiceImpl.class.getName());

	@Override
	public DepartmentRole addDepartmentRole(DepartmentRole deptRole) throws Exception {
		// DepartmentRole existingDeptRole =
		// deptRoleRepo.findByRoleIdAndDeptId(deptRole.getRoleId(),
		// deptRole.getDeptId());
		//
		// if (existingDeptRole != null) {
		// throw new Exception(
		// "DeptRole mapping exist for RoleId: " + deptRole.getRoleId() + ", deptId: " +
		// deptRole.getDeptId());
		// } else {
		// return deptRoleRepo.save(deptRole);
		// }
		throw new NotSupportedException("Yet to implement this method");
	}

	@Override
	public boolean removeDepartmentRole(Integer deptRoleId) throws Exception {
		// Optional<DepartmentRole> existingDeptRole =
		// deptRoleRepo.findById(deptRoleId);
		// if (existingDeptRole.isPresent()) {
		// deptRoleRepo.delete(existingDeptRole.get());
		// return true;
		// } else {
		// throw new Exception("DeptRole mapping doesn't exist for deptRoleId: " +
		// deptRoleId);
		// }
		throw new NotSupportedException("Yet to implement this method");
	}
}

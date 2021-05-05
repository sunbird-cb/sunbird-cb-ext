package org.sunbird.portal.department.service;

import org.sunbird.portal.department.model.DepartmentInfo;

public interface MdoPortalService {
	DepartmentInfo updateDepartment(DepartmentInfo deptInfo,String rootOrg) throws Exception;

	DepartmentInfo getMyDepartment(String userId, boolean isUserInfoRequired,String rootOrg) throws Exception;
	
	DepartmentInfo getMyFracDepartment(String userId, boolean isUserInfoRequired, String rootOrg) throws Exception;

	DepartmentInfo getMyCBCDepartment(String userId, boolean isUserInfoRequired, String rootOrg) throws Exception;
}

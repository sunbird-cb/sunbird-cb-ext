package org.sunbird.portal.department.service;

import java.util.List;

import org.sunbird.portal.department.dto.Department;
import org.sunbird.portal.department.dto.UserDepartmentRole;
import org.sunbird.portal.department.model.DepartmentInfo;
import org.sunbird.portal.department.model.DeptPublicInfo;
import org.sunbird.portal.department.model.SearchUserInfo;
import org.sunbird.portal.department.model.UserDepartmentInfo;

public interface PortalService {
	List<DepartmentInfo> getAllDepartments(String rootOrg);

	List<String> getDeptNameList();

	List<DeptPublicInfo> getAllDept();

	DeptPublicInfo searchDept(String deptName);

	DepartmentInfo getDepartmentById(Integer deptId, boolean isUserInfoRequired, String rootOrg);

	List<Department> getDepartmentsByUserId(String userId);

	UserDepartmentInfo addUserRoleInDepartment(UserDepartmentRole userDeptRole, String wid, String rootOrg, String org);

	UserDepartmentInfo updateUserRoleInDepartment(UserDepartmentRole userDeptRole, String wid, String rootOrg,
			String org);

	Boolean checkAdminPrivilage(Integer deptId, String userId, String rootOrg);

	Boolean checkMdoAdminPrivilage(String deptKey, String userId);

	DepartmentInfo getMyDepartmentDetails(String userId, boolean isUserInfoRequired);

	DepartmentInfo updateDepartment(DepartmentInfo deptInfo, String rootOrg);

	boolean isAdmin(String strDeptType, String roleName, String userId);

	boolean validateCBPUserLogin(String userId);

	boolean validateUserLogin(String userId, List<String> roles, String departmentType);

	boolean validateUserLoginForDepartment(String userId, String departmentType);

	DepartmentInfo addDepartment(String authUserToken, String userId, String userRoleName, DepartmentInfo deptInfo,
			String rootOrg);

	DepartmentInfo getMyDepartment(String userId, String rootOrg);

	DepartmentInfo getMyDepartment(String deptType, String userId, boolean isUserInfoRequired, String rootOrg);

	DepartmentInfo getMyCbpDepartment(String userId, String rootOrg);

	DepartmentInfo getMyDepartmentForRole(String roleName, String userId, boolean isUserInfoRequired, String rootOrg);

	DepartmentInfo getMyDepartmentForRoles(List<String> roleNames, String userId, boolean isUserInfoRequired,
			String rootOrg);

	List<SearchUserInfo> searchUserForRole(Integer deptId, String roleName, String userName);

	Boolean isUserActive(String userId);
}

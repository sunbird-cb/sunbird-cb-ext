package org.sunbird.portal.department.model;

public class UserDepartmentInfo {
	private String userId;
	private DepartmentInfo deptInfo;
	private Boolean isActive;
	private Boolean isBlocked;

	public DepartmentInfo getDeptInfo() {
		return deptInfo;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public Boolean getIsBlocked() {
		return isBlocked;
	}

	public String getUserId() {
		return userId;
	}

	public void setDeptInfo(DepartmentInfo deptInfo) {
		this.deptInfo = deptInfo;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public void setIsBlocked(Boolean isBlocked) {
		this.isBlocked = isBlocked;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}

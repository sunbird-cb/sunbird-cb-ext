package org.sunbird.workallocation.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkAllocationDTO {
	private String userId;
	private String userName;
	private String userEmail;
	private String waId;
	private String deptId;
	private String deptName;
	private List<RoleCompetency> roleCompetencyList;
	private String userPosition;
	private String positionId;
	private String status;

	public String getDeptId() {
		return deptId;
	}

	public String getDeptName() {
		return deptName;
	}

	public String getPositionId() {
		return positionId;
	}

	public List<RoleCompetency> getRoleCompetencyList() {
		return roleCompetencyList;
	}

	public String getStatus() {
		return status;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public String getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserPosition() {
		return userPosition;
	}

	public String getWaId() {
		return waId;
	}

	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public void setPositionId(String positionId) {
		this.positionId = positionId;
	}

	public void setRoleCompetencyList(List<RoleCompetency> roleCompetencyList) {
		this.roleCompetencyList = roleCompetencyList;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setUserPosition(String userPosition) {
		this.userPosition = userPosition;
	}

	public void setWaId(String waId) {
		this.waId = waId;
	}
}

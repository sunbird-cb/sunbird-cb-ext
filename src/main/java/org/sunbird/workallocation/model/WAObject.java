package org.sunbird.workallocation.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WAObject {
	private String id;
	private String deptId;
	private String deptName;
	private List<RoleCompetency> roleCompetencyList;
	private String userPosition;
	private String positionId;
	private long updatedAt;
	private String updatedBy;
	private long createdAt;
	private String createdBy;
	private String status;

	public long getCreatedAt() {
		return createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getDeptId() {
		return deptId;
	}

	public String getDeptName() {
		return deptName;
	}

	public String getId() {
		return id;
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

	public long getUpdatedAt() {
		return updatedAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public String getUserPosition() {
		return userPosition;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public void setId(String id) {
		this.id = id;
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

	public void setUpdatedAt(long updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public void setUserPosition(String userPosition) {
		this.userPosition = userPosition;
	}
}

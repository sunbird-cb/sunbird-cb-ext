package org.sunbird.workallocation.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkAllocationDTOV2 {
	private String id;
	private String userId;
	private String userName;
	private String userEmail;
	private List<RoleCompetency> roleCompetencyList;
	private List<ChildNode> unmappedActivities;
	private List<CompetencyDetails> unmappedCompetencies;
	private String userPosition;
	private String positionId;
	private String positionDescription;
	private String workOrderId;
	private long updatedAt;
	private String updatedBy;
	private String updatedByName;
	private int errorCount;
	private int progress;
	private long createdAt;
	private String createdBy;
	private String createdByName;

	public long getCreatedAt() {
		return createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getCreatedByName() {
		return createdByName;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public String getId() {
		return id;
	}

	public String getPositionDescription() {
		return positionDescription;
	}

	public String getPositionId() {
		return positionId;
	}

	public int getProgress() {
		return progress;
	}

	public List<RoleCompetency> getRoleCompetencyList() {
		return roleCompetencyList;
	}

	public List<ChildNode> getUnmappedActivities() {
		return unmappedActivities;
	}

	public List<CompetencyDetails> getUnmappedCompetencies() {
		return unmappedCompetencies;
	}

	public long getUpdatedAt() {
		return updatedAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public String getUpdatedByName() {
		return updatedByName;
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

	public String getWorkOrderId() {
		return workOrderId;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public void setCreatedByName(String createdByName) {
		this.createdByName = createdByName;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setPositionDescription(String positionDescription) {
		this.positionDescription = positionDescription;
	}

	public void setPositionId(String positionId) {
		this.positionId = positionId;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public void setRoleCompetencyList(List<RoleCompetency> roleCompetencyList) {
		this.roleCompetencyList = roleCompetencyList;
	}

	public void setUnmappedActivities(List<ChildNode> unmappedActivities) {
		this.unmappedActivities = unmappedActivities;
	}

	public void setUnmappedCompetencies(List<CompetencyDetails> unmappedCompetencies) {
		this.unmappedCompetencies = unmappedCompetencies;
	}

	public void setUpdatedAt(long updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public void setUpdatedByName(String updatedByName) {
		this.updatedByName = updatedByName;
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

	public void setWorkOrderId(String workOrderId) {
		this.workOrderId = workOrderId;
	}
}

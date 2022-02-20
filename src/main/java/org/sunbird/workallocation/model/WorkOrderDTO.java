package org.sunbird.workallocation.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkOrderDTO {
	private String id;
	private String name;
	private String deptId;
	private String deptName;
	private String status;
	private List<String> userIds;
	private String createdBy;
	private String createdByName;
	private long createdAt;
	private String updatedBy;
	private String updatedByName;
	private long updatedAt;
	private int progress;
	private int errorCount;
	private int rolesCount;
	private int activitiesCount;
	private int competenciesCount;
	private String publishedPdfLink;
	private String signedPdfLink;

	public void addUserId(String userId) {
		if (CollectionUtils.isEmpty(this.userIds)) {
			this.userIds = new ArrayList<>();
		}
		if (!this.userIds.contains(userId)) {
			this.userIds.add(userId);
		}
	}

	public int getActivitiesCount() {
		return activitiesCount;
	}

	public int getCompetenciesCount() {
		return competenciesCount;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getCreatedByName() {
		return createdByName;
	}

	public String getDeptId() {
		return deptId;
	}

	public String getDeptName() {
		return deptName;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getProgress() {
		return progress;
	}

	public String getPublishedPdfLink() {
		return publishedPdfLink;
	}

	public int getRolesCount() {
		return rolesCount;
	}

	public String getSignedPdfLink() {
		return signedPdfLink;
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

	public String getUpdatedByName() {
		return updatedByName;
	}

	public List<String> getUserIds() {
		return userIds;
	}

	public void setActivitiesCount(int activitiesCount) {
		this.activitiesCount = activitiesCount;
	}

	public void setCompetenciesCount(int competenciesCount) {
		this.competenciesCount = competenciesCount;
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

	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public void setPublishedPdfLink(String publishedPdfLink) {
		this.publishedPdfLink = publishedPdfLink;
	}

	public void setRolesCount(int rolesCount) {
		this.rolesCount = rolesCount;
	}

	public void setSignedPdfLink(String signedPdfLink) {
		this.signedPdfLink = signedPdfLink;
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

	public void setUpdatedByName(String updatedByName) {
		this.updatedByName = updatedByName;
	}

	public void setUserIds(List<String> userIds) {
		this.userIds = userIds;
	}
}

package org.sunbird.common.model;

import java.util.List;

public class SunbirdApiUserCourse {
	private long dateTime;
	private Object lastReadContentStatus;
	private String enrolledDate;
	private String contentId;
	private String description;
	private String courseLogoUrl;
	private String batchId;
	private Object content;
	private Object contentStatus;
	private Object lastReadContentId;
	private Object certstatus;
	private String courseId;
	private String collectionId;
	private String addedBy;
	private SunbirdApiBatch batch;
	private boolean active;
	private String userId;
	private List<Object> issuedCertificates;
	private Object completionPercentage;
	private String courseName;
	private List<Object> certificates;
	private Object completedOn;
	private int leafNodesCount;
	private int progress;
	private int status;

	public String getAddedBy() {
		return addedBy;
	}

	public SunbirdApiBatch getBatch() {
		return batch;
	}

	public String getBatchId() {
		return batchId;
	}

	public Object getCertstatus() {
		return certstatus;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public Object getCompletionPercentage() {
		return completionPercentage;
	}

	public Object getContent() {
		return content;
	}

	public String getContentId() {
		return contentId;
	}

	public Object getContentStatus() {
		return contentStatus;
	}

	public String getCourseId() {
		return courseId;
	}

	public String getCourseLogoUrl() {
		return courseLogoUrl;
	}

	public long getDateTime() {
		return dateTime;
	}

	public String getDescription() {
		return description;
	}

	public String getEnrolledDate() {
		return enrolledDate;
	}

	public List<Object> getIssuedCertificates() {
		return issuedCertificates;
	}

	public Object getLastReadContentId() {
		return lastReadContentId;
	}

	public Object getLastReadContentStatus() {
		return lastReadContentStatus;
	}

	public String getUserId() {
		return userId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setAddedBy(String addedBy) {
		this.addedBy = addedBy;
	}

	public void setBatch(SunbirdApiBatch batch) {
		this.batch = batch;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public void setCertstatus(Object certstatus) {
		this.certstatus = certstatus;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public void setCompletionPercentage(Object completionPercentage) {
		this.completionPercentage = completionPercentage;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public void setContentStatus(Object contentStatus) {
		this.contentStatus = contentStatus;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public void setCourseLogoUrl(String courseLogoUrl) {
		this.courseLogoUrl = courseLogoUrl;
	}

	public void setDateTime(long dateTime) {
		this.dateTime = dateTime;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEnrolledDate(String enrolledDate) {
		this.enrolledDate = enrolledDate;
	}

	public void setIssuedCertificates(List<Object> issuedCertificates) {
		this.issuedCertificates = issuedCertificates;
	}

	public void setLastReadContentId(Object lastReadContentId) {
		this.lastReadContentId = lastReadContentId;
	}

	public void setLastReadContentStatus(Object lastReadContentStatus) {
		this.lastReadContentStatus = lastReadContentStatus;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}

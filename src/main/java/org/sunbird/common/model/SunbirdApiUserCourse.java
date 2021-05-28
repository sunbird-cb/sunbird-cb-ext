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

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public Object getLastReadContentStatus() {
        return lastReadContentStatus;
    }

    public void setLastReadContentStatus(Object lastReadContentStatus) {
        this.lastReadContentStatus = lastReadContentStatus;
    }

    public String getEnrolledDate() {
        return enrolledDate;
    }

    public void setEnrolledDate(String enrolledDate) {
        this.enrolledDate = enrolledDate;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCourseLogoUrl() {
        return courseLogoUrl;
    }

    public void setCourseLogoUrl(String courseLogoUrl) {
        this.courseLogoUrl = courseLogoUrl;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public Object getContentStatus() {
        return contentStatus;
    }

    public void setContentStatus(Object contentStatus) {
        this.contentStatus = contentStatus;
    }

    public Object getLastReadContentId() {
        return lastReadContentId;
    }

    public void setLastReadContentId(Object lastReadContentId) {
        this.lastReadContentId = lastReadContentId;
    }

    public Object getCertstatus() {
        return certstatus;
    }

    public void setCertstatus(Object certstatus) {
        this.certstatus = certstatus;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public SunbirdApiBatch getBatch() {
        return batch;
    }

    public void setBatch(SunbirdApiBatch batch) {
        this.batch = batch;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Object> getIssuedCertificates() {
        return issuedCertificates;
    }

    public void setIssuedCertificates(List<Object> issuedCertificates) {
        this.issuedCertificates = issuedCertificates;
    }

    public Object getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(Object completionPercentage) {
        this.completionPercentage = completionPercentage;
    }
}

package org.sunbird.workallocation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void addUserId(String userId) {
        if (CollectionUtils.isEmpty(this.userIds))
            this.userIds = new ArrayList<>();
        if(!this.userIds.contains(userId))
                this.userIds.add(userId);
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public String getPublishedPdfLink() {
        return publishedPdfLink;
    }

    public void setPublishedPdfLink(String publishedPdfLink) {
        this.publishedPdfLink = publishedPdfLink;
    }

    public String getSignedPdfLink() {
        return signedPdfLink;
    }

    public void setSignedPdfLink(String signedPdfLink) {
        this.signedPdfLink = signedPdfLink;
    }

    public int getRolesCount() {
        return rolesCount;
    }

    public void setRolesCount(int rolesCount) {
        this.rolesCount = rolesCount;
    }

    public int getActivitiesCount() {
        return activitiesCount;
    }

    public void setActivitiesCount(int activitiesCount) {
        this.activitiesCount = activitiesCount;
    }

    public int getCompetenciesCount() {
        return competenciesCount;
    }

    public void setCompetenciesCount(int competenciesCount) {
        this.competenciesCount = competenciesCount;
    }
}

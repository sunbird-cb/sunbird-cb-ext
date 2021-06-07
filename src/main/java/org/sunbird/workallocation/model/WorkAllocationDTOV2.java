package org.sunbird.workallocation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<RoleCompetency> getRoleCompetencyList() {
        return roleCompetencyList;
    }

    public void setRoleCompetencyList(List<RoleCompetency> roleCompetencyList) {
        this.roleCompetencyList = roleCompetencyList;
    }

    public String getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(String userPosition) {
        this.userPosition = userPosition;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPositionDescription() {
        return positionDescription;
    }

    public void setPositionDescription(String positionDescription) {
        this.positionDescription = positionDescription;
    }

    public String getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(String workOrderId) {
        this.workOrderId = workOrderId;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public List<ChildNode> getUnmappedActivities() {
        return unmappedActivities;
    }

    public void setUnmappedActivities(List<ChildNode> unmappedActivities) {
        this.unmappedActivities = unmappedActivities;
    }

    public List<CompetencyDetails> getUnmappedCompetencies() {
        return unmappedCompetencies;
    }

    public void setUnmappedCompetencies(List<CompetencyDetails> unmappedCompetencies) {
        this.unmappedCompetencies = unmappedCompetencies;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}

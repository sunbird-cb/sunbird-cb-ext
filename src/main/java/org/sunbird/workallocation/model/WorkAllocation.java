package org.sunbird.workallocation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkAllocation {
    private String userId;
    private String deptId;
    private String deptName;
    private List<ActiveList> activeList;
    private List<Object> archivedList;
    private long updatedAt;
    private String updatedBy;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public List<ActiveList> getActiveList() {
        return activeList;
    }

    public void setActiveList(List<ActiveList> activeList) {
        this.activeList = activeList;
    }

    public List<Object> getArchivedList() {
        return archivedList;
    }

    public void setArchivedList(List<Object> archivedList) {
        this.archivedList = archivedList;
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
}

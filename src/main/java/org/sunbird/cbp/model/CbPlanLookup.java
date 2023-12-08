package org.sunbird.cbp.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CbPlanLookup {

    private String orgId;

    private String assignmentTypeInfoKey;

    private UUID cbPlanId;

    private String assignmentType;

    private String contentType;

    private List<String> contentList;

    private boolean isActive;

    private Date endDate;

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getAssignmentTypeInfoKey() {
        return assignmentTypeInfoKey;
    }

    public void setAssignmentTypeInfoKey(String assignmentTypeInfoKey) {
        this.assignmentTypeInfoKey = assignmentTypeInfoKey;
    }

    public UUID getCbPlanId() {
        return cbPlanId;
    }

    public void setCbPlanId(UUID cbPlanId) {
        this.cbPlanId = cbPlanId;
    }

    public String getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public List<String> getContentList() {
        return contentList;
    }

    public void setContentList(List<String> contentList) {
        this.contentList = contentList;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}

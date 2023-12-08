package org.sunbird.cbp.model.dto;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;

public class CbPlanDto {
    @Required
    @NotBlank
    private String name;
    @Required
    @NotBlank
    private String contentType;

    @Required
    @NotNull
    private List<String> contentList;

    @Required
    @NotBlank
    private String assignmentType;

    @Required
    @NotNull
    private List<String> assignmentTypeInfo;

    @Required
    @NotNull
    private Date endDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }

    public List<String> getAssignmentTypeInfo() {
        return assignmentTypeInfo;
    }

    public void setAssignmentTypeInfo(List<String> assignmentTypeInfo) {
        this.assignmentTypeInfo = assignmentTypeInfo;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}

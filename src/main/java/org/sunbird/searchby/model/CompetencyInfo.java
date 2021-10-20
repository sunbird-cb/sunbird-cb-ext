package org.sunbird.searchby.model;

public class CompetencyInfo {
    private String name;
    private String id;
    private String type;
    private String description;
    private String status;
    private String source;
    private String competencyType;
    private String competencyArea;
    private int contentCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getContentCount() {
        return contentCount;
    }

    public void setContentCount(int contentCount) {
        this.contentCount = contentCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCompetencyType() {
        return competencyType;
    }

    public void setCompetencyType(String competencyType) {
        this.competencyType = competencyType;
    }

    public String getCompetencyArea() {
        return competencyArea;
    }

    public void setCompetencyArea(String competencyArea) {
        this.competencyArea = competencyArea;
    }
}

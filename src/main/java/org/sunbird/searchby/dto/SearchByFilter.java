package org.sunbird.searchby.dto;

import java.util.List;

public class SearchByFilter {
    List<String> competencyName;
    List<String> competencyType;
    List<String> competencyArea;

    public List<String> getCompetencyName() {
        return competencyName;
    }

    public void setCompetencyName(List<String> competencyName) {
        this.competencyName = competencyName;
    }

    public List<String> getCompetencyType() {
        return competencyType;
    }

    public void setCompetencyType(List<String> competencyType) {
        this.competencyType = competencyType;
    }

    public List<String> getCompetencyArea() {
        return competencyArea;
    }

    public void setCompetencyArea(List<String> competencyArea) {
        this.competencyArea = competencyArea;
    }

    @Override
    public String toString() {
        return "SearchByFilter{" +
                "competencyName=" + competencyName +
                ", competencyType=" + competencyType +
                ", competencyArea=" + competencyArea +
                '}';
    }
}

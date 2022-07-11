package org.sunbird.searchby.model;

import java.io.Serializable;

public class CompetencyInfo extends FracCommonInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String type;
	private String status;
	private String source;
	private String competencyType;
	private String competencyArea;
	private Integer contentCount;

	public Integer getContentCount() {
		return contentCount;
	}

	public void setContentCount(Integer contentCount) {
		this.contentCount = contentCount;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

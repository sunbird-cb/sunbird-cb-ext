package org.sunbird.searchby.model;

import java.io.Serializable;

public class CompetencyInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String id;
	private String type;
	private String description;
	private String status;
	private String source;
	private String competencyType;
	private String competencyArea;
	private Integer contentCount;

	public String getCompetencyArea() {
		return competencyArea;
	}

	public String getCompetencyType() {
		return competencyType;
	}

	public Integer getContentCount() {
		return contentCount;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSource() {
		return source;
	}

	public String getStatus() {
		return status;
	}

	public String getType() {
		return type;
	}

	public void setCompetencyArea(String competencyArea) {
		this.competencyArea = competencyArea;
	}

	public void setCompetencyType(String competencyType) {
		this.competencyType = competencyType;
	}

	public void setContentCount(Integer contentCount) {
		this.contentCount = contentCount;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setType(String type) {
		this.type = type;
	}
}

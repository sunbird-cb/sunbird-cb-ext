package org.sunbird.searchby.model;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

public class SearchByFilter {
	List<String> competencyName;
	List<String> competencyType;
	List<String> competencyArea;

	public List<String> getCompetencyArea() {
		return competencyArea;
	}

	public List<String> getCompetencyName() {
		return competencyName;
	}

	public List<String> getCompetencyType() {
		return competencyType;
	}

	public boolean isEmptyFilter() {
		return CollectionUtils.isEmpty(competencyName) || CollectionUtils.isEmpty(competencyType)
				|| CollectionUtils.isEmpty(competencyArea);
	}

	public void setCompetencyArea(List<String> competencyArea) {
		this.competencyArea = competencyArea;
	}

	public void setCompetencyName(List<String> competencyName) {
		this.competencyName = competencyName;
	}

	public void setCompetencyType(List<String> competencyType) {
		this.competencyType = competencyType;
	}

	@Override
	public String toString() {
		return "SearchByFilter{" + "competencyName=" + competencyName + ", competencyType=" + competencyType
				+ ", competencyArea=" + competencyArea + '}';
	}
}

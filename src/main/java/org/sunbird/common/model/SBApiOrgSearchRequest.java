package org.sunbird.common.model;

import java.util.HashMap;
import java.util.Map;

public class SBApiOrgSearchRequest {
	private SBApiOrgFilterRequest filters = new SBApiOrgFilterRequest();

	private Map<String, String> sortBy = new HashMap<>();
	private String query;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public SBApiOrgFilterRequest getFilters() {
		return filters;
	}

	public void setFilters(SBApiOrgFilterRequest filters) {
		this.filters = filters;
	}

	public Map<String, String> getSortBy() {
		return sortBy;
	}

	public void setSortBy(Map<String, String> sortBy) {
		this.sortBy = sortBy;
	}

}

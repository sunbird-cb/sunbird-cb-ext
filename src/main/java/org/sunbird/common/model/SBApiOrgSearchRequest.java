package org.sunbird.common.model;

public class SBApiOrgSearchRequest {
	private SBApiOrgFilterRequest filters = new SBApiOrgFilterRequest();
	private SBApiSortByRequest sort_by = new SBApiSortByRequest();
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

	public SBApiSortByRequest getSort_by() {
		return sort_by;
	}

	public void setSort_by(SBApiSortByRequest sort_by) {
		this.sort_by = sort_by;
	}
}

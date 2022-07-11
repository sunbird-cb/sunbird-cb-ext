package org.sunbird.common.model;

public class SBApiOrgSearchRequest {
	private SBApiOrgFilterRequest filters = new SBApiOrgFilterRequest();
	private SBApiSortByRequest sort_by = new SBApiSortByRequest();

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

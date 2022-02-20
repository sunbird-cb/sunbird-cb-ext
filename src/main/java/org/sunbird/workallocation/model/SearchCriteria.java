package org.sunbird.workallocation.model;

public class SearchCriteria {
	private int pageSize;
	private int pageNo;
	private String status;
	private String userId;
	private String departmentName;
	private String query;

	public String getDepartmentName() {
		return departmentName;
	}

	public int getPageNo() {
		return pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public String getQuery() {
		return query;
	}

	public String getStatus() {
		return status;
	}

	public String getUserId() {
		return userId;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}

package org.sunbird.progress.model;

import java.util.List;

public class UserProgressRequest {

	private List<BatchEnrolment> batchList;
	private int limit;
	private int offset;

	public List<BatchEnrolment> getBatchList() {
		return batchList;
	}

	public void setBatchList(List<BatchEnrolment> batchList) {
		this.batchList = batchList;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

}

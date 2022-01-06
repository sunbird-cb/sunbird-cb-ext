package org.sunbird.progress.model;

import java.util.List;

public class BatchEnrolment {
	private String batchId;
	private List<String> userList;

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public List<String> getUserList() {
		return userList;
	}

	public void setUserList(List<String> userList) {
		this.userList = userList;
	}

}

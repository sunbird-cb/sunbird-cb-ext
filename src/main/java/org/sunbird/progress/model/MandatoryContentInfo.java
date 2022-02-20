package org.sunbird.progress.model;

public class MandatoryContentInfo {
	private String rootOrg;
	private String org;
	private String contentType;
	private String batchId;
	private Float minProgressForCompletion = 0.0f;

	private Float userProgress = 0.0f;

	public String getBatchId() {
		return batchId;
	}

	public String getContentType() {
		return contentType;
	}

	public Float getMinProgressForCompletion() {
		return minProgressForCompletion;
	}

	public String getOrg() {
		return org;
	}

	public String getRootOrg() {
		return rootOrg;
	}

	public Float getUserProgress() {
		return userProgress;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setMinProgressForCompletion(Float minProgressForCompletion) {
		this.minProgressForCompletion = minProgressForCompletion;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public void setRootOrg(String rootOrg) {
		this.rootOrg = rootOrg;
	}

	public void setUserProgress(Float userProgress) {
		this.userProgress = userProgress;
	}
}

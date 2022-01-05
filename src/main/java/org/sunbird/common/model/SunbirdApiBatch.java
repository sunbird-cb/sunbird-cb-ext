package org.sunbird.common.model;

import java.util.List;

public class SunbirdApiBatch {
	private List<String> createdFor;
	private String identifier;
	private String endDate;
	private String name;
	private String batchId;
	private String enrollmentType;
	private String enrollmentEndDate;
	private String startDate;
	private int status;

	public String getBatchId() {
		return batchId;
	}

	public List<String> getCreatedFor() {
		return createdFor;
	}

	public String getEndDate() {
		return endDate;
	}

	public String getEnrollmentEndDate() {
		return enrollmentEndDate;
	}

	public String getEnrollmentType() {
		return enrollmentType;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getName() {
		return name;
	}

	public String getStartDate() {
		return startDate;
	}

	public int getStatus() {
		return status;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public void setCreatedFor(List<String> createdFor) {
		this.createdFor = createdFor;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public void setEnrollmentEndDate(String enrollmentEndDate) {
		this.enrollmentEndDate = enrollmentEndDate;
	}

	public void setEnrollmentType(String enrollmentType) {
		this.enrollmentType = enrollmentType;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}

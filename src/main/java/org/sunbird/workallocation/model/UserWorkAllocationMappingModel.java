package org.sunbird.workallocation.model;

public class UserWorkAllocationMappingModel {

	private String userId;
	private String workAllocationId;
	private String workOrderId;
	private String status;

	public String getStatus() {
		return status;
	}

	public String getUserId() {
		return userId;
	}

	public String getWorkAllocationId() {
		return workAllocationId;
	}

	public String getWorkOrderId() {
		return workOrderId;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setWorkAllocationId(String workAllocationId) {
		this.workAllocationId = workAllocationId;
	}

	public void setWorkOrderId(String workOrderId) {
		this.workOrderId = workOrderId;
	}
}

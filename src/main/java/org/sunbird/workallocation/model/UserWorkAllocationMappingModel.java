package org.sunbird.workallocation.model;

public class UserWorkAllocationMappingModel {

	private String userId;
	private String workAllocationId;
	private String workOrderId;
	private String status;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getWorkAllocationId() {
		return workAllocationId;
	}

	public void setWorkAllocationId(String workAllocationId) {
		this.workAllocationId = workAllocationId;
	}

	public String getWorkOrderId() {
		return workOrderId;
	}

	public void setWorkOrderId(String workOrderId) {
		this.workOrderId = workOrderId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}

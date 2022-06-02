package org.sunbird.user.registration.util;

/**
 * This Enum provides various status values for User Registration flow.
 * 
 * @author karthik
 *
 */
public enum UserRegistrationStatus {

	CREATED(1, "Created"), WF_INITIATED(2, "Initiated"), WF_APPROVED(3, "Approved"), WF_DENIED(4, "Denied"), FAILED(5,
			"Failed");

	private int status = 0;
	private String name;

	UserRegistrationStatus(int status, String name) {
		this.status = status;
		this.name = name;
	}

	public int getStatus() {
		return status;
	}

	public String getName() {
		return name;
	}
}

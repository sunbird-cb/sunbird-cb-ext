package org.sunbird.user.registration.util;

/**
 * This Enum provides various status values for User Registration flow.
 * 
 * @author karthik
 *
 */
public enum UserRegistrationStatus {

	CREATED(1), WF_INITIATED(2), WF_APPROVED(3), WF_DENIED(4), FAILED(5);

	private int status = 0;

	UserRegistrationStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

}

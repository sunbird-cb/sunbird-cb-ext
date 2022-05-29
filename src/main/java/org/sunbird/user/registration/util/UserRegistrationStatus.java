package org.sunbird.user.registration.util;

/**
 * This Enum provides various status values for User Registration flow.
 * 
 * @author karthik
 *
 */
public enum UserRegistrationStatus {

	INITIATED(0), CREATED(1), APPROVED(2), DENIED(3), FAILED(4);

	private int status = 0;

	UserRegistrationStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
}

package org.sunbird.portal.department.model;

public class PortalUserInfo {
	private String userId;
	private String firstName;
	private String lastName;
	private String emailId;
	private boolean isActive;
	private boolean isBlocked;

	public String getEmailId() {
		return emailId;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getUserId() {
		return userId;
	}

	public boolean isActive() {
		return isActive;
	}

	public boolean isBlocked() {
		return isBlocked;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public void setBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
package org.sunbird.user.registration.model;

/**
 * Model to store user registration details in ES server
 * 
 * @author karthik
 *
 */
public class UserRegistration extends UserRegistrationInfo {
	private String registrationCode;
	private String priviousStatus;
	private String status;
	private String createdOn;
	private String updatedOn;
	private String createdBy;
	private String updatedBy;

	public String getRegistrationCode() {
		return registrationCode;
	}

	public void setRegistrationCode(String registrationCode) {
		this.registrationCode = registrationCode;
	}
	
	public String getPriviousStatus() {
		return priviousStatus;
	}

	public void setPriviousStatus(String priviousStatus) {
		this.priviousStatus = priviousStatus;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(String updatedOn) {
		this.updatedOn = updatedOn;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
}

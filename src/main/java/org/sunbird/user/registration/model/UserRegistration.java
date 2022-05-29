package org.sunbird.user.registration.model;

/**
 * Model to store user registration details in ES server
 * 
 * @author karthik
 *
 */
public class UserRegistration extends UserRegistrationInfo {
	private String id;
	private String registrationCode;
	private String priviousStatus;
	private String status;
	private long createdOn;
	private long updatedOn;
	private String createdBy;
	private String updatedBy;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}

	public long getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(long updatedOn) {
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

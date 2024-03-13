package org.sunbird.user.registration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model to store user registration details in ES server
 * 
 * @author karthik
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegistration extends UserRegistrationInfo {
	private String wfId;
	private String priviousStatus;
	private String status;
	private long createdOn;
	private long updatedOn;
	private String createdBy;
	private String updatedBy;
	private String userId;
	private String userName;
	private String proposedDeptName;
	private String userAuthToken;

	public String getWfId() {
		return wfId;
	}

	public void setWfId(String wfId) {
		this.wfId = wfId;
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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getProposedDeptName() {
		return proposedDeptName;
	}

	public void setProposedDeptName(String proposedDeptName) {
		this.proposedDeptName = proposedDeptName;
	}

	public String getUserAuthToken() {
		return userAuthToken;
	}

	public void setUserAuthToken(String userAuthToken) {
		this.userAuthToken = userAuthToken;
	}

	public String toMininumString() {
		StringBuilder strBuilder = new StringBuilder("[ UserRegistrationCode : ");
		strBuilder.append(this.getRegistrationCode()).append(", UserId : ").append(this.getUserId()).append("]");
		return strBuilder.toString();
	}
}

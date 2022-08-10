package org.sunbird.user.registration.model;

import java.util.HashMap;
import java.util.List;

public class WfRequest {

	private String state;

	private String action;

	private String applicationId;

	private String userId;

	private String actorUserId;

	private String wfId;

	private List<HashMap<String, Object>> updateFieldValues;

	private String comment;

	private String serviceName;

	private String deptName;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getActorUserId() {
		return actorUserId;
	}

	public void setActorUserId(String actorUserId) {
		this.actorUserId = actorUserId;
	}

	public String getWfId() {
		return wfId;
	}

	public void setWfId(String wfId) {
		this.wfId = wfId;
	}

	public List<HashMap<String, Object>> getUpdateFieldValues() {
		return updateFieldValues;
	}

	public void setUpdateFieldValues(List<HashMap<String, Object>> updateFieldValues) {
		this.updateFieldValues = updateFieldValues;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

}

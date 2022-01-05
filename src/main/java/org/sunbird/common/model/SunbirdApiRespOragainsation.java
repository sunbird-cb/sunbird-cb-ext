package org.sunbird.common.model;

import java.util.List;

public class SunbirdApiRespOragainsation {

	private String organisationId;
	private List<String> roles;
	private String userId;
	private String parentOrgId;
	private String id;

	public String getId() {
		return id;
	}

	public String getOrganisationId() {
		return organisationId;
	}

	public String getParentOrgId() {
		return parentOrgId;
	}

	public List<String> getRoles() {
		return roles;
	}

	public String getUserId() {
		return userId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setOrganisationId(String organisationId) {
		this.organisationId = organisationId;
	}

	public void setParentOrgId(String parentOrgId) {
		this.parentOrgId = parentOrgId;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}

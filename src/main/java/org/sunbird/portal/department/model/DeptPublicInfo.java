package org.sunbird.portal.department.model;

public class DeptPublicInfo {

	String id;
	String description;
	String rootOrgId;
	String orgName;

	public DeptPublicInfo() {
	}

	public DeptPublicInfo(String id, String description, String rootOrgId, String orgName) {
		this.id = id;
		this.description = description;
		this.rootOrgId = rootOrgId;
		this.orgName = orgName;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getOrgName() {
		return orgName;
	}

	public String getRootOrgId() {
		return rootOrgId;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public void setRootOrgId(String rootOrgId) {
		this.rootOrgId = rootOrgId;
	}

}

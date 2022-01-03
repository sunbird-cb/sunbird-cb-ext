package org.sunbird.portal.department.model;

public class DeptPublicInfo {

	String id;
	String description;
	String rootOrgId;
	String orgName;

	public DeptPublicInfo() {
		super();
	}

	public DeptPublicInfo(String id, String description, String rootOrgId, String orgName) {
		super();
		this.id = id;
		this.description = description;
		this.rootOrgId = rootOrgId;
		this.orgName = orgName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRootOrgId() {
		return rootOrgId;
	}

	public void setRootOrgId(String rootOrgId) {
		this.rootOrgId = rootOrgId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

}

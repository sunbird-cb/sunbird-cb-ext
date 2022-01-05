package org.sunbird.searchby.model;

import java.io.Serializable;

public class ProviderInfo implements Serializable {
	private String name;
	private String logoUrl;
	private String description;
	private String orgId;
	private Integer contentCount;

	public Integer getContentCount() {
		return contentCount;
	}

	public String getDescription() {
		return description;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public String getName() {
		return name;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setContentCount(Integer contentCount) {
		this.contentCount = contentCount;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
}

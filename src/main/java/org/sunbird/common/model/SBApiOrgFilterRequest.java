package org.sunbird.common.model;

import java.util.List;

public class SBApiOrgFilterRequest {
	private boolean isTenant = true;
	private List<String> id;
	public boolean isTenant() {
		return isTenant;
	}
	public void setTenant(boolean isTenant) {
		this.isTenant = isTenant;
	}
	public List<String> getId() {
		return id;
	}
	public void setId(List<String> id) {
		this.id = id;
	}
}

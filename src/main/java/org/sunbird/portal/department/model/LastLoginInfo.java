package org.sunbird.portal.department.model;

public class LastLoginInfo {

	String userId;
	String orgId;
	String loginTime;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(String loginTime) {
		this.loginTime = loginTime;
	}

	@Override
	public String toString() {
		return "LastLoginInfo [userId=" + userId + ", orgId=" + orgId + ", loginTime=" + loginTime + "]";
	}

	public LastLoginInfo(String userId, String orgId, String loginTime) {
		super();
		this.userId = userId;
		this.orgId = orgId;
		this.loginTime = loginTime;
	}

	public LastLoginInfo() {
		// TODO Auto-generated constructor stub
	}

}

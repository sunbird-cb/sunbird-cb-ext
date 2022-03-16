package org.sunbird.telemetry.model;

import java.util.Date;

public class LastLoginInfo {

	String userId;
	Long orgId;
	Date loginTime;
	String orgName;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	@Override
	public String toString() {
		return "LastLoginInfo [userId=" + userId + ", orgId=" + orgId + ", loginTime=" + loginTime + ", orgName="
				+ orgName + "]";
	}

	public LastLoginInfo(String userId, Long orgId, Date loginTime, String orgName) {
		super();
		this.userId = userId;
		this.orgId = orgId;
		this.loginTime = loginTime;
		this.orgName = orgName;
	}

}
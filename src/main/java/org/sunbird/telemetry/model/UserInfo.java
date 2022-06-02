package org.sunbird.telemetry.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;
import java.util.List;

public class UserInfo {
	private String userId;
	private Long orgId;
	private Date loginTime;
	private String orgName;
	private String identifier;
	private String email;
	private String emailLink;
	private String userName;
	private String firstName;
	private String lastName;
	private String channel;

	private String designation;
	private List<String> roles = null;

	public String getEmailLink() {
		return emailLink;
	}

	public void setEmailLink(String emailLink) {
		this.emailLink = emailLink;
	}
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

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
	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}
	public UserInfo() {
	}
	public UserInfo(String userId, Long orgId, Date loginTime, String orgName) {
		super();
		this.userId = userId;
		this.orgId = orgId;
		this.loginTime = loginTime;
		this.orgName = orgName;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("userId", userId)
				.append("orgId", orgId)
				.append("loginTime", loginTime)
				.append("orgName", orgName)
				.append("identifier", identifier)
				.append("email", email)
				.append("emailLink", emailLink)
				.append("userName", userName)
				.append("firstName", firstName)
				.append("lastName", lastName)
				.append("channel", channel)
				.append("designation", designation)
				.append("roles", roles)
				.toString();
	}

	public UserInfo(String userId, Long orgId, Date loginTime, String orgName, String identifier, String email, String emailLink, String userName, String firstName, String lastName, String channel, String designation, List<String> roles) {
		this.userId = userId;
		this.orgId = orgId;
		this.loginTime = loginTime;
		this.orgName = orgName;
		this.identifier = identifier;
		this.email = email;
		this.emailLink = emailLink;
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.channel = channel;
		this.designation = designation;
		this.roles = roles;
	}
}
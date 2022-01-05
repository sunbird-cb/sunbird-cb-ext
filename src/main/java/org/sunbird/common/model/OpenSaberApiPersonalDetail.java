package org.sunbird.common.model;

public class OpenSaberApiPersonalDetail {
	private String firstname;
	private String surname;
	private String primaryEmail;
	private String username;
	private String osid;

	public String getFirstname() {
		return firstname;
	}

	public String getOsid() {
		return osid;
	}

	public String getPrimaryEmail() {
		return primaryEmail;
	}

	public String getSurname() {
		return surname;
	}

	public String getUsername() {
		return username;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public void setOsid(String osid) {
		this.osid = osid;
	}

	public void setPrimaryEmail(String primaryEmail) {
		this.primaryEmail = primaryEmail;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}

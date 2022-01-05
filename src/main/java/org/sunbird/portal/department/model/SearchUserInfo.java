package org.sunbird.portal.department.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchUserInfo {
	@JsonProperty("department_name")
	private String departmentName;
	private String wid;
	private String email;
	@JsonProperty("first_name")
	private String firstName;
	@JsonProperty("last_name")
	private String lastName;

	public String getDepartmentName() {
		return departmentName;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getWid() {
		return wid;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setWid(String wid) {
		this.wid = wid;
	}

}

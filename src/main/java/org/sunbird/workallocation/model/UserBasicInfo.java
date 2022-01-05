package org.sunbird.workallocation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserBasicInfo {
	private String wid;
	@JsonProperty("department_name")
	private String departmentName;
	@JsonProperty("last_name")
	private String lastName;
	@JsonProperty("first_name")
	private String firstName;
	private String email;
	private String designation;

	public String getDepartmentName() {
		return departmentName;
	}

	public String getDesignation() {
		return designation;
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

	public void setDesignation(String designation) {
		this.designation = designation;
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

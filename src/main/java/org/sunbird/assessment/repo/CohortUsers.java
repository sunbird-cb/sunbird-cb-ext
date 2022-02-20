package org.sunbird.assessment.repo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CohortUsers {
	@JsonProperty("first_name")
	private String firstName;
	@JsonProperty("last_name")
	private String lastName;
	private String email;
	private String desc;
	@JsonProperty("user_id")
	private String userId;
	private String department;
	@JsonProperty("phone_No")
	private String phoneNo;
	private String designation;
	private String userLocation;
	private String city;

	public String getCity() {
		return city;
	}

	public String getDepartment() {
		return department;
	}

	public String getDesc() {
		return desc;
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

	public String getPhoneNo() {
		return phoneNo;
	}

	public String getUserId() {
		return userId;
	}

	public String getUserLocation() {
		return userLocation;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public void setDesc(String desc) {
		this.desc = desc;
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

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setUserLocation(String userLocation) {
		this.userLocation = userLocation;
	}

}

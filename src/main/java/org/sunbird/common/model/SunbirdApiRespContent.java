package org.sunbird.common.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SunbirdApiRespContent {

	private String rootOrgName;
	private String channel;
	private String id;
	private String orgName;
	private String description;
	private String identifier;
	private String rootOrgId;
	private String firstName;
	private String dob;
	private String userType;
	private String lastName;
	private String gender;
	private List<String> roles;
	private String countryCode;
	private String email;
	private String userName;
	private List<SunbirdApiRespOragainsation> organisations;
	private Boolean isMdo;
	private Boolean isCbp;

	public String getChannel() {
		return channel;
	}

	public String getRootOrgName() {
		return rootOrgName;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getDescription() {
		return description;
	}

	public String getDob() {
		return dob;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getGender() {
		return gender;
	}

	public String getId() {
		return id;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getLastName() {
		return lastName;
	}

	public List<SunbirdApiRespOragainsation> getOrganisations() {
		return organisations;
	}

	public String getOrgName() {
		return orgName;
	}

	public List<String> getRoles() {
		return roles;
	}

	public String getRootOrgId() {
		return rootOrgId;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserType() {
		return userType;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setOrganisations(List<SunbirdApiRespOragainsation> organisations) {
		this.organisations = organisations;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public void setRootOrgId(String rootOrgId) {
		this.rootOrgId = rootOrgId;
	}

	public void setRootOrgName(String rootOrgName) {
		this.rootOrgName = rootOrgName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public Boolean getIsMdo() {
		return isMdo;
	}

	public void setIsMdo(Boolean isMdo) {
		this.isMdo = isMdo;
	}

	public Boolean getIsCbp() {
		return isCbp;
	}

	public void setIsCbp(Boolean isCbp) {
		this.isCbp = isCbp;
	}

}
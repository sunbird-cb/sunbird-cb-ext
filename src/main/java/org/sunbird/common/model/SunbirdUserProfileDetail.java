package org.sunbird.common.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SunbirdUserProfileDetail {
	private List<SunbirdUserProfessionalDetail> professionalDetails;
	private Map<String, Object> personalDetails;

	public List<SunbirdUserProfessionalDetail> getProfessionalDetails() {
		return professionalDetails;
	}

	public void setProfessionalDetails(List<SunbirdUserProfessionalDetail> professionalDetails) {
		this.professionalDetails = professionalDetails;
	}

	public Map<String, Object> getPersonalDetails() {
		return personalDetails;
	}

	public void setPersonalDetails(Map<String, Object> personalDetails) {
		this.personalDetails = personalDetails;
	}
}

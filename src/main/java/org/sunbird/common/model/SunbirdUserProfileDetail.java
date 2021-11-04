package org.sunbird.common.model;

import java.util.List;

public class SunbirdUserProfileDetail {
	private List<SunbirdUserProfessionalDetail> professionalDetails;

	public List<SunbirdUserProfessionalDetail> getProfessionalDetails() {
		return professionalDetails;
	}

	public void setProfessionalDetails(List<SunbirdUserProfessionalDetail> professionalDetails) {
		this.professionalDetails = professionalDetails;
	}
}

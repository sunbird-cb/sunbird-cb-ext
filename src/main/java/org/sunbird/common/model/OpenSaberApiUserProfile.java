package org.sunbird.common.model;

public class OpenSaberApiUserProfile {
	private String id;
	private String userId;
	private String osid;
	private OpenSaberApiPersonalDetail personalDetails;

	public String getId() {
		return id;
	}

	public String getOsid() {
		return osid;
	}

	public OpenSaberApiPersonalDetail getPersonalDetails() {
		return personalDetails;
	}

	public String getUserId() {
		return userId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setOsid(String osid) {
		this.osid = osid;
	}

	public void setPersonalDetails(OpenSaberApiPersonalDetail personalDetails) {
		this.personalDetails = personalDetails;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}

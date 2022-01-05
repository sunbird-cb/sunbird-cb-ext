package org.sunbird.common.model;

public class SearchUserApiContent {

	private String id;
	private String userId;
	private String email;
	private String firstName;
	private String lastName;
	private String desc;
	private String channel;
	private SunbirdUserProfileDetail profileDetails;

	public String getChannel() {
		return channel;
	}

	public String getDesc() {
		return desc;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getId() {
		return id;
	}

	public String getLastName() {
		return lastName;
	}

	public SunbirdUserProfileDetail getProfileDetails() {
		return profileDetails;
	}

	public String getUserId() {
		return userId;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setProfileDetails(SunbirdUserProfileDetail profileDetails) {
		this.profileDetails = profileDetails;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "SearchUserApiContent{" + "id='" + id + '\'' + ", userId='" + userId + '\'' + ", email='" + email + '\''
				+ ", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + ", desc='" + desc + '\''
				+ '}';
	}
}

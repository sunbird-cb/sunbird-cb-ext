package org.sunbird.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchUserApiContent {

    private String id;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String desc;
    private String channel;
    private SunbirdUserProfileDetail profileDetails;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public SunbirdUserProfileDetail getProfileDetails() {
		return profileDetails;
	}

	public void setProfileDetails(SunbirdUserProfileDetail profileDetails) {
		this.profileDetails = profileDetails;
	}

	@Override
    public String toString() {
        return "SearchUserApiContent{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}

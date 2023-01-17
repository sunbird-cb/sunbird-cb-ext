package org.sunbird.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchUserApiContent {

    private String id;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String desc;
    private String channel;
    private String phone;

    private String courseId;
    private String rootOrgId;
    private SunbirdUserProfileDetail profileDetails;



    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    private List<Map<String, Object>> organisations = null;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Map<String, Object>> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(List<Map<String, Object>> organisations) {
        this.organisations = organisations;
    }

    public String getRootOrgId() {
        return rootOrgId;
    }

    public void setRootOrgId(String rootOrgId) {
        this.rootOrgId = rootOrgId;
    }
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
                ", channel='" + channel + '\'' +
                ", phone='" + phone + '\'' +
                ", rootOrgId='" + rootOrgId + '\'' +
                ", profileDetails=" + profileDetails +
                ", organisations=" + organisations +
                '}';
    }
}

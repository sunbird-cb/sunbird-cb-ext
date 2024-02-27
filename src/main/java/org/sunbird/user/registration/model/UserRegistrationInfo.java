package org.sunbird.user.registration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Model object to store user registration details.
 *
 * @author karthik
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegistrationInfo {
    private String registrationCode;
    private String firstName;
    private String lastName;
    private String email;
    private String position;
    private String source;
    private String orgName;
    private String channel;
    private String organisationType;
    private String organisationSubType;
    private String mapId;
    private String sbRootOrgId;
    private String sbOrgId;
    private String phone;
    private String group;
    private List<String> tag;
    private String externalSystemId;
    private String externalSystem;

    public String getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getOrganisationType() {
        return organisationType;
    }

    public void setOrganisationType(String organisationType) {
        this.organisationType = organisationType;
    }

    public String getOrganisationSubType() {
        return organisationSubType;
    }

    public void setOrganisationSubType(String organisationSubType) {
        this.organisationSubType = organisationSubType;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public String getSbRootOrgId() {
        return sbRootOrgId;
    }

    public void setSbRootOrgId(String sbRootOrgId) {
        this.sbRootOrgId = sbRootOrgId;
    }

    public String getSbOrgId() {
        return sbOrgId;
    }

    public void setSbOrgId(String sbOrgId) {
        this.sbOrgId = sbOrgId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getTag() {
        return tag;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public String getExternalSystemId() {
        return externalSystemId;
    }

    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    public String getExternalSystem() {
        return externalSystem;
    }

    public void setExternalSystem(String externalSystem) {
        this.externalSystem = externalSystem;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}

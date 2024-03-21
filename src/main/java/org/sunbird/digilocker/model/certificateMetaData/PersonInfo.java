package org.sunbird.digilocker.model.certificateMetaData;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class PersonInfo {
    private String uid;
    private String title;
    private String name;
    private String dob;
    private String age;
    private String swd;
    private String swdIndicator;
    private String motherName;
    private String gender;
    private String maritalStatus;
    private String relationWithHof;
    private String disabilityStatus;
    private String category;
    private String religion;
    private String phone;
    private String email;

    private Address address;
    private Photo photo;

    public PersonInfo() {
        this.name = "";
        this.uid = "";
        this.phone = "";
        this.age = "";
        this.dob = "";
        this.category = "";
        this.disabilityStatus = "";
        this.email = "";
        this.gender = "";
        this.maritalStatus = "";
        this.motherName = "";
        this.swd = "";
        this.swdIndicator = "";
        this.relationWithHof = "";
        this.title = "";
        this.religion = "";
        this.address = new Address();
        this.photo = new Photo();
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getSwd() {
        return swd;
    }

    public void setSwd(String swd) {
        this.swd = swd;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getSwdIndicator() {
        return swdIndicator;
    }

    public void setSwdIndicator(String swdIndicator) {
        this.swdIndicator = swdIndicator;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getRelationWithHof() {
        return relationWithHof;
    }

    public void setRelationWithHof(String relationWithHof) {
        this.relationWithHof = relationWithHof;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getDisabilityStatus() {
        return disabilityStatus;
    }

    public void setDisabilityStatus(String disabilityStatus) {
        this.disabilityStatus = disabilityStatus;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JacksonXmlProperty(localName = "Address")
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @JacksonXmlProperty(localName = "Photo")
    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }
}

package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Person {
    private String name;
    private String dob;
    private String gender;
    private String phone;

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
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
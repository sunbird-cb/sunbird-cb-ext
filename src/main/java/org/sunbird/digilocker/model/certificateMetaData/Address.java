package org.sunbird.digilocker.model.certificateMetaData;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Address {
    private String type;
    private String line1;
    private String line2;
    private String house;
    private String landmark;
    private String locality;
    private String vtc;
    private String district;
    private String pin;
    private String state;
    private String country;

    public Address() {
        this.type = "";
        this.line1 = "";
        this.line2 = "";
        this.house = "";
        this.landmark = "";
        this.locality = "";
        this.vtc = "";
        this.district = "";
        this.pin = "";
        this.state = "";
        this.country = "IN";
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getLine2() {
        return line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getVtc() {
        return vtc;
    }

    public void setVtc(String vtc) {
        this.vtc = vtc;
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}

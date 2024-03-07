package org.sunbird.digilocker.model.certificateMetaData;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Organization {
    private String name;
    private String code;
    private String tin;
    private String uid;
    private String type;
    private Address address;

    public Organization() {
        this.name = "iGOT";
        this.tin = "";
        this.uid = "";
        this.type = "CG";
        this.code = "";
        this.address = new Address();
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JacksonXmlProperty(localName = "Address", namespace = "http://tempuri.org/")
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class URIRequestDocDetails {

    @JacksonXmlProperty(localName = "DOB")
    private String DOB;

    @JacksonXmlProperty(localName = "DigiLockerId")
    private String DigiLockerId;

    @JacksonXmlProperty(localName = "DocType")
    private String DocType;

    @JacksonXmlProperty(localName = "mobile_no")
    private String Mobile;

    @JacksonXmlProperty(localName = "FullName")
    private String FullName;

    @JacksonXmlProperty(localName = "CertificateAccessCode")
    private String CertificateAccessCode;

    public String getDOB() {
        return DOB;
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public String getDigiLockerId() {
        return DigiLockerId;
    }

    public void setDigiLockerId(String digiLockerId) {
        DigiLockerId = digiLockerId;
    }

    public String getDocType() {
        return DocType;
    }

    public void setDocType(String docType) {
        DocType = docType;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getCertificateAccessCode() {
        return CertificateAccessCode;
    }

    public void setCertificateAccessCode(String certificateAccessCode) {
        CertificateAccessCode = certificateAccessCode;
    }
}

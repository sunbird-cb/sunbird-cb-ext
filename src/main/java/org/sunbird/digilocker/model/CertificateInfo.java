package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class CertificateInfo {

    private String certificateName;
    private String issuedOn;

    public CertificateInfo() {
    }

    @JacksonXmlProperty(localName = "certificateName", namespace = "http://tempuri.org/")
    public String getCertificateName() {
        return certificateName;
    }

    public void setCertificateName(String certificateName) {
        this.certificateName = certificateName;
    }

    @JacksonXmlProperty(localName = "issuedOn", namespace = "http://tempuri.org/")
    public String getIssuedOn() {
        return issuedOn;
    }

    public void setIssuedOn(String issuedOn) {
        this.issuedOn = issuedOn;
    }
}

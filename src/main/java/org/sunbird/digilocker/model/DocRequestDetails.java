package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class DocRequestDetails {
    @JacksonXmlProperty(localName = "DigiLockerId")
    private String DigiLockerId;

    @JacksonXmlProperty(localName = "URI")
    private String URI;

    public String getDigiLockerId() {
        return DigiLockerId;
    }

    public void setDigiLockerId(String digiLockerId) {
        DigiLockerId = digiLockerId;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }
}

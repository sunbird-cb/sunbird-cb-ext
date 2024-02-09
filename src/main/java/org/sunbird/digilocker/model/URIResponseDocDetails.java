package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class URIResponseDocDetails {
    private IssuedTo issuedTo;
    private String URI;

    public URIResponseDocDetails() {
        this.issuedTo = new IssuedTo();
    }

    @JacksonXmlProperty(localName = "IssuedTo", namespace = "http://tempuri.org/")
    public IssuedTo getIssuedTo() {
        return issuedTo;
    }

    public void setIssuedTo(IssuedTo issuedTo) {
        this.issuedTo = issuedTo;
    }

    @JacksonXmlProperty(localName = "URI", namespace = "http://tempuri.org/")
    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }
}


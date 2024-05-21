package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class URIResponseDocDetails {

    private IssuedTo issuedTo;
    private String URI;
    private String docContent;
    private String dataContent;
    private String docDescription;

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

    @JacksonXmlProperty(localName = "DocContent", namespace = "http://tempuri.org/")
    public String getDocContent() {
        return docContent;
    }

    public void setDocContent(String docContent) {
        this.docContent = docContent;
    }

    @JacksonXmlProperty(localName = "DataContent", namespace = "http://tempuri.org/")
    public String getDataContent() {
        return dataContent;
    }

    public void setDataContent(String dataContent) {
        this.dataContent = dataContent;
    }

    @JacksonXmlProperty(localName = "DocDescription", namespace = "http://tempuri.org/")
    public String getDocDescription() {
        return docDescription;
    }

    public void setDocDescription(String docDescription) {
        this.docDescription = docDescription;
    }
}


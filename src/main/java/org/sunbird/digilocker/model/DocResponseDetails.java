package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class DocResponseDetails {

    private String docContent;
    private CertificateInfo dataContent;

    public DocResponseDetails() {
        this.dataContent = new CertificateInfo();
    }

    @JacksonXmlProperty(localName = "DocContent", namespace = "http://tempuri.org/")
    public String getDocContent() {
        return docContent;
    }

    public void setDocContent(String docContent) {
        this.docContent = docContent;
    }

    @JacksonXmlProperty(localName = "DataContent", namespace = "http://tempuri.org/")
    public CertificateInfo getDataContent() {
        return dataContent;
    }

    public void setDataContent(CertificateInfo dataContent) {
        this.dataContent = dataContent;
    }

}

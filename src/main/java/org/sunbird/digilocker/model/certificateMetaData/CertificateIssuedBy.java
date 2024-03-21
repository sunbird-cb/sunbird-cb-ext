package org.sunbird.digilocker.model.certificateMetaData;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class CertificateIssuedBy {
    private Organization organization;

    public CertificateIssuedBy() {
        this.organization = new Organization();
    }
    @JacksonXmlProperty(localName = "Organization")
    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}

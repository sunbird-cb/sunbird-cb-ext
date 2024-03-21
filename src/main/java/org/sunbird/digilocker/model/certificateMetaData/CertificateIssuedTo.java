package org.sunbird.digilocker.model.certificateMetaData;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class CertificateIssuedTo {

    private PersonInfo personInfo;

    public CertificateIssuedTo() {
        this.personInfo = new PersonInfo();
    }
    @JacksonXmlProperty(localName = "Person")
    public PersonInfo getPersonInfo() {
        return personInfo;
    }

    public void setPersonInfo(PersonInfo personInfo) {
        this.personInfo = personInfo;
    }
}

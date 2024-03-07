package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.sunbird.digilocker.model.certificateMetaData.CertificateMetaData;

import java.io.Serializable;

public class CertificateInfo implements Serializable {

    private CertificateMetaData certificateMetaData;

    public CertificateInfo() {
        this.certificateMetaData = new CertificateMetaData();
    }

    @JacksonXmlProperty(localName = "Certificate", namespace = "http://tempuri.org/")
    public CertificateMetaData getCertificateMetaData() {
        return certificateMetaData;
    }

    public void setCertificateMetaData(CertificateMetaData certificateMetaData) {
        this.certificateMetaData = certificateMetaData;
    }
}

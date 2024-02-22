package org.sunbird.digilocker.model.certificateMetaData;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class CertificateData {

    private Certificate certificate;

    private ParticipationCertificate participationCertificate;

    public CertificateData() {
        this.certificate = new Certificate();
        this.participationCertificate = new ParticipationCertificate();
    }
    @JacksonXmlProperty(localName = "Certificate", namespace = "http://tempuri.org/")
    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    @JacksonXmlProperty(localName = "ParticipationCertificate", namespace = "http://tempuri.org/")
    public ParticipationCertificate getParticipationCertificate() {
        return participationCertificate;
    }

    public void setParticipationCertificate(ParticipationCertificate participationCertificate) {
        this.participationCertificate = participationCertificate;
    }
}

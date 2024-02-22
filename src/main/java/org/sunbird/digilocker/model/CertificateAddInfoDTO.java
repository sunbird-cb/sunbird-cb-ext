package org.sunbird.digilocker.model;

import java.util.Date;

public class CertificateAddInfoDTO {

    private String certificateName;
    private String documentName;
    private String documentInfo;
    private String certificateId;
    private Date certificateIssueOn;


    public String getCertificateName() {
        return certificateName;
    }

    public void setCertificateName(String certificateName) {
        this.certificateName = certificateName;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocumentInfo() {
        return documentInfo;
    }

    public void setDocumentInfo(String documentInfo) {
        this.documentInfo = documentInfo;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public Date getCertificateIssueOn() {
        return certificateIssueOn;
    }

    public void setCertificateIssueOn(Date certificateIssueOn) {
        this.certificateIssueOn = certificateIssueOn;
    }
}

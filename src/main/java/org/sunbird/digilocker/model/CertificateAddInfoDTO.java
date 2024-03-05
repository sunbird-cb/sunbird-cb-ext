package org.sunbird.digilocker.model;

import java.util.Date;

public class CertificateAddInfoDTO {

    private String certificateName;
    private String documentName;
    private String documentInfo;
    private String certificateId;
    private Date certificateIssueOn;

    private String userName;

    private String swd;

    private String swdIndicator;

    private String vtc;

    private String locality;

    private String district;

    private String state;



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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSwd() {
        return swd;
    }

    public void setSwd(String swd) {
        this.swd = swd;
    }

    public String getSwdIndicator() {
        return swdIndicator;
    }

    public void setSwdIndicator(String swdIndicator) {
        this.swdIndicator = swdIndicator;
    }

    public String getVtc() {
        return vtc;
    }

    public void setVtc(String vtc) {
        this.vtc = vtc;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}

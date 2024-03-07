package org.sunbird.digilocker.model.certificateMetaData;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Date;

@JacksonXmlRootElement(localName = "Certificate", namespace = "http://tempuri.org/")
public class CertificateMetaData {
    private String language;
    private String name;
    private String type;
    private String number;
    private String prevNumber;
    private String expiryDate;
    private String validFromDate;
    private String issuedAt;
    private Date issueDate;
    private String status;
    private CertificateIssuedBy issuedBy;
    private CertificateIssuedTo issuedTo;
    private CertificateData certificateData;
    private String signature;

    public CertificateMetaData() {
        this.language = "99";
        this.name = "";
        this.type = "";
        this.number = "";
        this.prevNumber = "";
        this.expiryDate = "";
        this.validFromDate = "";
        this.issuedAt = "";
        this.issueDate = null;
        this.status = "A";
        this.issuedBy = new CertificateIssuedBy();
        this.issuedTo = new CertificateIssuedTo();
        this.certificateData = new CertificateData();
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getPrevNumber() {
        return prevNumber;
    }

    public void setPrevNumber(String prevNumber) {
        this.prevNumber = prevNumber;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getValidFromDate() {
        return validFromDate;
    }

    public void setValidFromDate(String validFromDate) {
        this.validFromDate = validFromDate;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(String issuedAt) {
        this.issuedAt = issuedAt;
    }

    @JacksonXmlProperty(isAttribute = true)
    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JacksonXmlProperty(localName = "IssuedBy", namespace = "http://tempuri.org/")
    public CertificateIssuedBy getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(CertificateIssuedBy issuedBy) {
        this.issuedBy = issuedBy;
    }

    @JacksonXmlProperty(localName = "IssuedTo", namespace = "http://tempuri.org/")
    public CertificateIssuedTo getIssuedTo() {
        return issuedTo;
    }

    public void setIssuedTo(CertificateIssuedTo issuedTo) {
        this.issuedTo = issuedTo;
    }

    @JacksonXmlProperty(localName = "CertificateData", namespace = "http://tempuri.org/")
    public CertificateData getCertificateData() {
        return certificateData;
    }

    public void setCertificateData(CertificateData certificateData) {
        this.certificateData = certificateData;
    }

    @JacksonXmlProperty(localName = "Signature", namespace = "http://tempuri.org/")
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}

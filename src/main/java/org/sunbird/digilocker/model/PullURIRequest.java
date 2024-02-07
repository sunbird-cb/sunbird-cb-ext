package org.sunbird.digilocker.model;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "PullURIRequest")
public class PullURIRequest {

    @JacksonXmlProperty(localName = "format")
    private String format;

    @JacksonXmlProperty(localName = "orgId")
    private String orgId;

    @JacksonXmlProperty(localName = "ts")
    private String ts;

    @JacksonXmlProperty(localName = "txn")
    private String txn;

    @JacksonXmlProperty(localName = "ver")
    private String ver;

    @JacksonXmlProperty(localName = "DocDetails")
    private DocDetails docDetails;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getTxn() {
        return txn;
    }

    public void setTxn(String txn) {
        this.txn = txn;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public DocDetails getDocDetails() {
        return docDetails;
    }

    public void setDocDetails(DocDetails docDetails) {
        this.docDetails = docDetails;
    }

    public static class DocDetails {

        @JacksonXmlProperty(localName = "DOB")
        private String DOB;

        @JacksonXmlProperty(localName = "DigiLockerId")
        private String DigiLockerId;

        @JacksonXmlProperty(localName = "DocType")
        private String DocType;

        @JacksonXmlProperty(localName = "Mobile")
        private String Mobile;

        @JacksonXmlProperty(localName = "FullName")
        private String FullName;

        @JacksonXmlProperty(localName = "CertificateAccessCode")
        private String CertificateAccessCode;

        public String getDOB() {
            return DOB;
        }

        public void setDOB(String DOB) {
            this.DOB = DOB;
        }

        public String getDigiLockerId() {
            return DigiLockerId;
        }

        public void setDigiLockerId(String digiLockerId) {
            DigiLockerId = digiLockerId;
        }

        public String getDocType() {
            return DocType;
        }

        public void setDocType(String docType) {
            DocType = docType;
        }

        public String getMobile() {
            return Mobile;
        }

        public void setMobile(String mobile) {
            Mobile = mobile;
        }

        public String getFullName() {
            return FullName;
        }

        public void setFullName(String fullName) {
            FullName = fullName;
        }

        public String getCertificateAccessCode() {
            return CertificateAccessCode;
        }

        public void setCertificateAccessCode(String certificateAccessCode) {
            CertificateAccessCode = certificateAccessCode;
        }
    }
}

package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "PullDocResponse", namespace = "http://tempuri.org/")
public class PullDocResponse {
    private ResponseStatus responseStatus;
    private DocDetails docDetails;

    @JacksonXmlProperty(localName = "ResponseStatus", namespace = "http://tempuri.org/")
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    @JacksonXmlProperty(localName = "DocDetails", namespace = "http://tempuri.org/")
    public DocDetails getDocDetails() {
        return docDetails;
    }

    public void setDocDetails(DocDetails docDetails) {
        this.docDetails = docDetails;
    }

    public static class ResponseStatus {

        private int status;
        private String ts;
        private String txn;

        public ResponseStatus() {

        }
        public ResponseStatus(int status, String ts, String txn) {
            this.status = status;
            this.ts = ts;
            this.txn = txn;
        }
        @JacksonXmlProperty(localName = "Status",isAttribute = true)
        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getTs() {
            return ts;
        }

        public void setTs(String ts) {
            this.ts = ts;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getTxn() {
            return txn;
        }

        public void setTxn(String txn) {
            this.txn = txn;
        }
    }

    public static class DocDetails {

        private String docContent;
        private CertificateInfo dataContent;

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

    // Inner class for Person
    public static class CertificateInfo {

        private String certificateName;
        private String issuedOn;

        public CertificateInfo() {}

        @JacksonXmlProperty(localName = "certificateName", namespace = "http://tempuri.org/")
        public String getCertificateName() {
            return certificateName;
        }

        public void setCertificateName(String certificateName) {
            this.certificateName = certificateName;
        }

        @JacksonXmlProperty(localName = "issuedOn", namespace = "http://tempuri.org/")
        public String getIssuedOn() {
            return issuedOn;
        }

        public void setIssuedOn(String issuedOn) {
            this.issuedOn = issuedOn;
        }
    }
}

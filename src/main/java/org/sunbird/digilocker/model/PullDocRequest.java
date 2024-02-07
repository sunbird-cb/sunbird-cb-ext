package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "PullDocRequest")
public class PullDocRequest {

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
    private PullDocRequest.DocDetails docDetails;

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

    public PullDocRequest.DocDetails getDocDetails() {
        return docDetails;
    }

    public void setDocDetails(PullDocRequest.DocDetails docDetails) {
        this.docDetails = docDetails;
    }

    public static class DocDetails {
        @JacksonXmlProperty(localName = "DigiLockerId")
        private String DigiLockerId;

        @JacksonXmlProperty(localName = "URI")
        private String URI;

        public String getDigiLockerId() {
            return DigiLockerId;
        }

        public void setDigiLockerId(String digiLockerId) {
            DigiLockerId = digiLockerId;
        }

        public String getURI() {
            return URI;
        }

        public void setURI(String URI) {
            this.URI = URI;
        }
    }
}

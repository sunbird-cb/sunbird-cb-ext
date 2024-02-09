package org.sunbird.digilocker.model;

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
    private URIRequestDocDetails docDetails;

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

    public URIRequestDocDetails getDocDetails() {
        return docDetails;
    }

    public void setDocDetails(URIRequestDocDetails docDetails) {
        this.docDetails = docDetails;
    }

}

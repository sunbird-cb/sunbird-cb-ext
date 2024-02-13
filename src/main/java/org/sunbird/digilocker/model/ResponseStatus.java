package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(namespace = "http://tempuri.org/")
public class ResponseStatus {
    private String status;
    private String ts;
    private String txn;

    public ResponseStatus() {
    }

    public ResponseStatus(String status, String ts, String txn) {
        this.status = status;
        this.ts = ts;
        this.txn = txn;
    }

    @JacksonXmlProperty(localName = "Status", isAttribute = true)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    @JacksonXmlText
    public String getStatusElement() {
        return status;
    }

    public void setStatusElement(String status) {
        this.status = status;
    }

}


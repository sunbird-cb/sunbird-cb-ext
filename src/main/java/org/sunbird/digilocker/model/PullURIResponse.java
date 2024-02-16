package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "PullURIResponse", namespace = "http://tempuri.org/")
public class PullURIResponse {

    private ResponseStatus responseStatus;
    private URIResponseDocDetails docDetails;

    public PullURIResponse() {
        this.responseStatus = new ResponseStatus();
        this.docDetails = new URIResponseDocDetails();
    }

    @JacksonXmlProperty(localName = "ResponseStatus", namespace = "http://tempuri.org/")
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    @JacksonXmlProperty(localName = "DocDetails", namespace = "http://tempuri.org/")
    public URIResponseDocDetails getDocDetails() {
        return docDetails;
    }

    public void setDocDetails(URIResponseDocDetails docDetails) {
        this.docDetails = docDetails;
    }

}


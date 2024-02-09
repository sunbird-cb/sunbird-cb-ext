package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "PullDocResponse", namespace = "http://tempuri.org/")
public class PullDocResponse {
    private ResponseStatus responseStatus;
    private DocResponseDetails docDetails;

    public PullDocResponse() {
        this.responseStatus = new ResponseStatus();
        this.docDetails = new DocResponseDetails();
    }

    @JacksonXmlProperty(localName = "ResponseStatus", namespace = "http://tempuri.org/")
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    @JacksonXmlProperty(localName = "DocDetails", namespace = "http://tempuri.org/")
    public DocResponseDetails getDocDetails() {
        return docDetails;
    }

    public void setDocDetails(DocResponseDetails docDetails) {
        this.docDetails = docDetails;
    }

}

package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import javax.xml.bind.annotation.XmlElement;

@JacksonXmlRootElement(localName = "PullURIResponse", namespace = "http://tempuri.org/")
public class PullURIResponse {

    private ResponseStatus responseStatus;
    private DocDetails docDetails;

    public PullURIResponse() {
        this.responseStatus = new ResponseStatus();
        this.docDetails = new DocDetails();
    }

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

    // Inner class for ResponseStatus
    public static class ResponseStatus {
        private String status;
        private String ts;
        private String txn;

        public ResponseStatus() {}

        public ResponseStatus(String status, String ts, String txn) {
            this.status = status;
            this.ts = ts;
            this.txn = txn;
        }
        @JacksonXmlProperty(localName = "Status",isAttribute = true)
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
    }

    public static class DocDetails {
        private IssuedTo issuedTo;
        private String URI;

        public DocDetails() {
            this.issuedTo = new IssuedTo();
        }

        @JacksonXmlProperty(localName ="IssuedTo", namespace = "http://tempuri.org/")
        public IssuedTo getIssuedTo() {
            return issuedTo;
        }

        public void setIssuedTo(IssuedTo issuedTo) {
            this.issuedTo = issuedTo;
        }

        @JacksonXmlProperty(localName = "URI", namespace = "http://tempuri.org/")
        public String getURI() {
            return URI;
        }

        public void setURI(String URI) {
            this.URI = URI;
        }
    }

    // Inner class for IssuedTo
    public static class IssuedTo {
        private Persons persons;

        public IssuedTo() {
            this.persons = new Persons();
        }

        @JacksonXmlProperty(localName = "Persons", namespace = "http://tempuri.org/")
        public Persons getPersons() {
            return persons;
        }

        public void setPersons(Persons persons) {
            this.persons = persons;
        }
    }

    // Inner class for Persons
    public static class Persons {
        private Person person;

        public Persons() {
            this.person = new Person();
        }

        @JacksonXmlProperty(localName = "Person", namespace = "http://tempuri.org/")
        public Person getPerson() {
            return person;
        }

        public void setPerson(Person person) {
            this.person = person;
        }
    }

    // Inner class for Person
    public static class Person {
        private String name;
        private String dob;
        private String gender;
        private String phone;

        public Person() {}

        @JacksonXmlProperty(isAttribute = true)
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        @JacksonXmlProperty(isAttribute = true)
        public String getDob() {
            return dob;
        }

        public void setDob(String dob) {
            this.dob = dob;
        }
        @JacksonXmlProperty(isAttribute = true)
        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }
        @JacksonXmlProperty(isAttribute = true)
        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}


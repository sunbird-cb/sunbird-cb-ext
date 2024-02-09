package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class IssuedTo {
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

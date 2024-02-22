package org.sunbird.digilocker.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Persons {
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

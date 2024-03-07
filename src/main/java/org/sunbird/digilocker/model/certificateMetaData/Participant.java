package org.sunbird.digilocker.model.certificateMetaData;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Participant {
    private String name;
    private String age;
    private String gender;

    public Participant() {
        this.name = "";
        this.age = "";
        this.gender = "";
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}

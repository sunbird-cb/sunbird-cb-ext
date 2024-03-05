package org.sunbird.digilocker.model.certificateMetaData;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Tournament {
    private String name;
    private String place;
    private String date;

    public Tournament() {
        this.name = "";
        this.place = "";
        this.date = "";
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

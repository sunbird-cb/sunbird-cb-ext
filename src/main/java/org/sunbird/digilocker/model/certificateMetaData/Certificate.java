package org.sunbird.digilocker.model.certificateMetaData;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Certificate {
    private String number;
    private String place;
    private String date;

    public Certificate() {
        this.number = "";
        this.place = "";
        this.date = "";
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
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

package org.sunbird.digilocker.model.certificateMetaData;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Photo {
    private String format;

    public Photo() {
        this.format = "RAW/PNG/JPG/JPEG";
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}

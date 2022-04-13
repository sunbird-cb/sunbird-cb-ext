package org.sunbird.common.model;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailConfig {
    private String sender;
    private final String subject;

    public EmailConfig(String sender, String subject) {
        this.sender = sender;
        this.subject = subject;
    }

}

package org.sunbird.common.model;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailConfig {
    private String sender;
    private String subject;

    @Override
    public String toString() {
        return "EmailConfig{" +
                "sender='" + sender + '\'' +
                ", subject='" + subject + '\'' +
                '}';
    }

    public EmailConfig(String sender, String subject) {
        this.sender = sender;
        this.subject = subject;
    }

}

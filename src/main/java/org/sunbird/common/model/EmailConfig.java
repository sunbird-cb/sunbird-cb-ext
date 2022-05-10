package org.sunbird.common.model;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailConfig {
    private String sender;
    private String subject;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

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

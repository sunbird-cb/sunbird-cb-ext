package org.sunbird.common.model;

public class Config {
    private String sender;
    private Object topic;
    private Object otp;
    private String subject;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Object getTopic() {
        return topic;
    }

    public void setTopic(Object topic) {
        this.topic = topic;
    }

    public Object getOtp() {
        return otp;
    }

    public void setOtp(Object otp) {
        this.otp = otp;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
package org.sunbird.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Notification {
    private String mode;
    private String deliveryType;
    private EmailConfig config;
    private List<String> ids;
    private Template template;

    public Notification(String mode, String deliveryType, EmailConfig config, List<String> ids, Template template) {
        this.mode = mode;
        this.deliveryType = deliveryType;
        this.config = config;
        this.ids = ids;
        this.template = template;
    }

    public EmailConfig getConfig() {
        return config;
    }

    public void setConfig(EmailConfig config) {
        this.config = config;
    }
}


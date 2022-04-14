package org.sunbird.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Notification {
    private String mode;
    private String deliveryType;
    private EmailConfig config;
    private List<String> ids;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    private Template template;

    public Notification() {
    }

    public Notification(String mode, String deliveryType, EmailConfig config, List<String> ids, Template template) {
        this.mode = mode;
        this.deliveryType = deliveryType;
        this.config = config;
        this.ids = ids;
        this.template = template;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "mode='" + mode + '\'' +
                ", deliveryType='" + deliveryType + '\'' +
                ", config=" + config +
                ", ids=" + ids +
                ", template=" + template +
                '}';
    }

    public EmailConfig getConfig() {
        return config;
    }

    public void setConfig(EmailConfig config) {
        this.config = config;
    }
}


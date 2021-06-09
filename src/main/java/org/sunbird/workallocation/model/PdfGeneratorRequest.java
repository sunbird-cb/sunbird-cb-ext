package org.sunbird.workallocation.model;

import java.util.Map;

public class PdfGeneratorRequest {

    private String templateId;

    private Map<String, Object> tagValuePair;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Map<String, Object> getTagValuePair() {
        return tagValuePair;
    }

    public void setTagValuePair(Map<String, Object> tagValuePair) {
        this.tagValuePair = tagValuePair;
    }
}

package org.sunbird.workallocation.model;

import java.util.List;
public class ContentCreateRequest {
    private String name;
    private String creator;
    private String createdBy;
    private String code;
    private String mimeType;
    private String contentType;
    private String primaryCategory;
    private List<String> organisation;

    public ContentCreateRequest(String name, String creator, String createdBy, String code, String mimeType, String contentType, String primaryCategory, List<String> organisation, List<String> createdFor) {
        this.name = name;
        this.creator = creator;
        this.createdBy = createdBy;
        this.code = code;
        this.mimeType = mimeType;
        this.contentType = contentType;
        this.primaryCategory = primaryCategory;
        this.organisation = organisation;
        this.createdFor = createdFor;
    }
    public ContentCreateRequest () {

    }

    private List<String> createdFor;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getPrimaryCategory() {
        return primaryCategory;
    }

    public void setPrimaryCategory(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public List<String> getOrganisation() {
        return organisation;
    }

    public void setOrganisation(List<String> organisation) {
        this.organisation = organisation;
    }

    public List<String> getCreatedFor() {
        return createdFor;
    }

    public void setCreatedFor(List<String> createdFor) {
        this.createdFor = createdFor;
    }
}

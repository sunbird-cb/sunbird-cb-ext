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

	private List<String> createdFor;

	public ContentCreateRequest() {

	}

	public ContentCreateRequest(String name, String creator, String createdBy, String code, String mimeType,
			String contentType, String primaryCategory, List<String> organisation, List<String> createdFor) {
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

	public String getCode() {
		return code;
	}

	public String getContentType() {
		return contentType;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public List<String> getCreatedFor() {
		return createdFor;
	}

	public String getCreator() {
		return creator;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getName() {
		return name;
	}

	public List<String> getOrganisation() {
		return organisation;
	}

	public String getPrimaryCategory() {
		return primaryCategory;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public void setCreatedFor(List<String> createdFor) {
		this.createdFor = createdFor;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOrganisation(List<String> organisation) {
		this.organisation = organisation;
	}

	public void setPrimaryCategory(String primaryCategory) {
		this.primaryCategory = primaryCategory;
	}
}

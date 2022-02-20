package org.sunbird.workallocation.model;

import java.util.List;

public class ResponseData {

	private String type;
	private String id;
	private String name;
	private String description;
	private String status;
	private String source;
	private List<ChildNode> children;
	private AdditionalProperties additionalProperties;

	public AdditionalProperties getAdditionalProperties() {
		return additionalProperties;
	}

	public List<ChildNode> getChildren() {
		return children;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSource() {
		return source;
	}

	public String getStatus() {
		return status;
	}

	public String getType() {
		return type;
	}

	public void setAdditionalProperties(AdditionalProperties additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

	public void setChildren(List<ChildNode> children) {
		this.children = children;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setType(String type) {
		this.type = type;
	}
}

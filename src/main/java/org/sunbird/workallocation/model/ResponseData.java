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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public List<ChildNode> getChildren() {
		return children;
	}

	public void setChildren(List<ChildNode> children) {
		this.children = children;
	}

	public AdditionalProperties getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(AdditionalProperties additionalProperties) {
		this.additionalProperties = additionalProperties;
	}
}

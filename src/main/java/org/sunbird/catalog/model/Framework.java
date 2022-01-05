package org.sunbird.catalog.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Framework {
	private String identifier;
	private String code;
	private String name;
	private String description;
	private String type;
	private String objectType;
	private List<Category> categories;

	public List<Category> getCategories() {
		return categories;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getName() {
		return name;
	}

	public String getObjectType() {
		return objectType;
	}

	public String getType() {
		return type;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public void setType(String type) {
		this.type = type;
	}

}

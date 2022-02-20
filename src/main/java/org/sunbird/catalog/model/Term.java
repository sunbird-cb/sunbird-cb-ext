package org.sunbird.catalog.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Term {
	private String identifier;
	private String code;
	private String name;
	private String description;
	private int index;
	private String status;
	private List<Term> children;
	private int noOfHoursConsumed;

	public List<Term> getChildren() {
		return children;
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

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public int getNoOfHoursConsumed() {
		return noOfHoursConsumed;
	}

	public String getStatus() {
		return status;
	}

	public void setChildren(List<Term> children) {
		this.children = children;
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

	public void setIndex(int index) {
		this.index = index;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNoOfHoursConsumed(int noOfHoursConsumed) {
		this.noOfHoursConsumed = noOfHoursConsumed;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}

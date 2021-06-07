package org.sunbird.workallocation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildNode {
	private String type;
	private String id;
	private String name;
	private String description;
	private String status;
	private String source;
	private String parentRole;
	private String submittedFromId;
	private String submittedFromName;
	private String submittedFromEmail;
	private String submittedToId;
	private String submittedToName;
	private String submittedToEmail;
	private String level;

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

	public String getParentRole() {
		return parentRole;
	}

	public void setParentRole(String parentRole) {
		this.parentRole = parentRole;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public ChildNode getFracRequest(String source) {
		ChildNode newCN = new ChildNode();
		newCN.setName(name);
		newCN.setSource(source);
		newCN.setType("ACTIVITY");
		return newCN;
	}

	public String getSubmittedFromId() {
		return submittedFromId;
	}

	public void setSubmittedFromId(String submittedFromId) {
		this.submittedFromId = submittedFromId;
	}

	public String getSubmittedFromName() {
		return submittedFromName;
	}

	public void setSubmittedFromName(String submittedFromName) {
		this.submittedFromName = submittedFromName;
	}

	public String getSubmittedFromEmail() {
		return submittedFromEmail;
	}

	public void setSubmittedFromEmail(String submittedFromEmail) {
		this.submittedFromEmail = submittedFromEmail;
	}

	public String getSubmittedToId() {
		return submittedToId;
	}

	public void setSubmittedToId(String submittedToId) {
		this.submittedToId = submittedToId;
	}

	public String getSubmittedToName() {
		return submittedToName;
	}

	public void setSubmittedToName(String submittedToName) {
		this.submittedToName = submittedToName;
	}

	public String getSubmittedToEmail() {
		return submittedToEmail;
	}

	public void setSubmittedToEmail(String submittedToEmail) {
		this.submittedToEmail = submittedToEmail;
	}
}

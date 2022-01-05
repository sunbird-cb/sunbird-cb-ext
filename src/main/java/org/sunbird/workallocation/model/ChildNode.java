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

	public String getDescription() {
		return description;
	}

	public ChildNode getFracRequest(String source) {
		ChildNode newCN = new ChildNode();
		newCN.setName(name);
		newCN.setSource(source);
		newCN.setType("ACTIVITY");
		return newCN;
	}

	public String getId() {
		return id;
	}

	public String getLevel() {
		return level;
	}

	public String getName() {
		return name;
	}

	public String getParentRole() {
		return parentRole;
	}

	public String getSource() {
		return source;
	}

	public String getStatus() {
		return status;
	}

	public String getSubmittedFromEmail() {
		return submittedFromEmail;
	}

	public String getSubmittedFromId() {
		return submittedFromId;
	}

	public String getSubmittedFromName() {
		return submittedFromName;
	}

	public String getSubmittedToEmail() {
		return submittedToEmail;
	}

	public String getSubmittedToId() {
		return submittedToId;
	}

	public String getSubmittedToName() {
		return submittedToName;
	}

	public String getType() {
		return type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParentRole(String parentRole) {
		this.parentRole = parentRole;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setSubmittedFromEmail(String submittedFromEmail) {
		this.submittedFromEmail = submittedFromEmail;
	}

	public void setSubmittedFromId(String submittedFromId) {
		this.submittedFromId = submittedFromId;
	}

	public void setSubmittedFromName(String submittedFromName) {
		this.submittedFromName = submittedFromName;
	}

	public void setSubmittedToEmail(String submittedToEmail) {
		this.submittedToEmail = submittedToEmail;
	}

	public void setSubmittedToId(String submittedToId) {
		this.submittedToId = submittedToId;
	}

	public void setSubmittedToName(String submittedToName) {
		this.submittedToName = submittedToName;
	}

	public void setType(String type) {
		this.type = type;
	}
}

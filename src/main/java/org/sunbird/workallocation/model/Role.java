package org.sunbird.workallocation.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Role {
	private String type;
	private String id;
	private String name;
	private String description;
	private String status;
	private String source;
	private List<ChildNode> childNodes;
	private long addedAt;
	private long updatedAt;
	private String updatedBy;
	private long archivedAt;
	private boolean isArchived;

	public long getAddedAt() {
		return addedAt;
	}

	public long getArchivedAt() {
		return archivedAt;
	}

	public List<ChildNode> getChildNodes() {
		return childNodes;
	}

	public String getDescription() {
		return description;
	}

	public FracRequest getFracRequest(String source, ChildNode child) {
		FracRequest req = new FracRequest();
		req.setName(source);
		req.setName(name);
		req.setDescription(description);
		req.setType("ROLE");
		if ("".equals(id)) {
			req.setId(null);
		} else {
			req.setId(id);
		}

		List<ChildNode> children = new ArrayList<>();
		if (child != null) {
			ChildNode newCN = new ChildNode();
			newCN.setName(child.getName());
			newCN.setType(child.getType());
			newCN.setSource(source);
			children.add(newCN);
			req.setChildren(children);
		} else if (!CollectionUtils.isEmpty(childNodes)) {
			for (ChildNode cn : childNodes) {
				ChildNode newCN = new ChildNode();
				if (!StringUtils.isEmpty(cn.getId())) {
					newCN.setId(cn.getId());
				}

				newCN.setName(cn.getName());
				newCN.setType(cn.getType());
				newCN.setSource(source);
				newCN.setDescription(cn.getDescription());
				newCN.setSubmittedFromEmail(cn.getSubmittedFromEmail());
				newCN.setSubmittedFromId(cn.getSubmittedFromId());
				newCN.setSubmittedFromName(cn.getSubmittedFromName());
				newCN.setSubmittedToEmail(cn.getSubmittedToEmail());
				newCN.setSubmittedToId(cn.getSubmittedToId());
				newCN.setSubmittedToName(cn.getSubmittedToName());
				children.add(newCN);
			}
			req.setChildren(children);
		}
		return req;
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

	public long getUpdatedAt() {
		return updatedAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public boolean isArchived() {
		return isArchived;
	}

	public void setAddedAt(long addedAt) {
		this.addedAt = addedAt;
	}

	public void setArchived(boolean archived) {
		isArchived = archived;
	}

	public void setArchivedAt(long archivedAt) {
		this.archivedAt = archivedAt;
	}

	public void setChildNodes(List<ChildNode> childNodes) {
		this.childNodes = childNodes;
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

	public void setUpdatedAt(long updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
}

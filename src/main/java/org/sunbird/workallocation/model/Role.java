package org.sunbird.workallocation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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

	public List<ChildNode> getChildNodes() {
		return childNodes;
	}

	public void setChildNodes(List<ChildNode> childNodes) {
		this.childNodes = childNodes;
	}

	public long getAddedAt() {
		return addedAt;
	}

	public void setAddedAt(long addedAt) {
		this.addedAt = addedAt;
	}

	public long getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(long updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public long getArchivedAt() {
		return archivedAt;
	}

	public void setArchivedAt(long archivedAt) {
		this.archivedAt = archivedAt;
	}

	public boolean isArchived() {
		return isArchived;
	}

	public void setArchived(boolean archived) {
		isArchived = archived;
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
		} else {
			if (!CollectionUtils.isEmpty(childNodes)) {
				for (ChildNode cn : childNodes) {
					ChildNode newCN = new ChildNode();
					if (!StringUtils.isEmpty(cn.getId()))
						newCN.setId(cn.getId());

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
		}
		return req;
	}
}

package org.sunbird.workallocation.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public Map<String, Object> getFracRequest(String source) {
		Map<String, Object> frac = new HashMap<String, Object>();
		frac.put("name", name);
		frac.put("type", "ROLE");
		frac.put("source", source);
		List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
		for (ChildNode cn : childNodes) {
			Map<String, Object> child = new HashMap<String, Object>();
			child.put("name", cn.getName());
			child.put("Type", "ACTIVITY");
			child.put("source", source);
			children.add(child);
		}
		frac.put("children", children);
		return frac;
	}
}

package org.sunbird.workallocation.model;

public class Child {
	private String id;
	private String type;
	private String name;
	private String description;
	private Object status;
	private String source;
	private String level;

	public String getDescription() {
		return description;
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

	public String getSource() {
		return source;
	}

	public Object getStatus() {
		return status;
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

	public void setSource(String source) {
		this.source = source;
	}

	public void setStatus(Object status) {
		this.status = status;
	}

	public void setType(String type) {
		this.type = type;
	}
}

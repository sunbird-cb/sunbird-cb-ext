package org.sunbird.workallocation.model;

import java.util.List;

public class FracRequest {
	private String type;
	private String name;
	private String source;
	private List<ChildNode> children;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

}

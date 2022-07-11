package org.sunbird.portal.department.model;

import java.io.Serializable;

public class DeptPublicInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String identifier;
	private String channel;

	public DeptPublicInfo(String identifier, String channel) {
		this.identifier = identifier;
		this.channel = channel;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
}

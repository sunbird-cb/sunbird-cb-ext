package org.sunbird.common.model;

import org.sunbird.common.util.Constants;

public class SBApiSortByRequest {
	private String channel = Constants.ASC_ORDER;

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
}

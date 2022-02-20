package org.sunbird.common.model;

import java.util.List;

public class SunbirdApiResultResponse {

	private int count;
	private List<SunbirdApiRespContent> content;

	public List<SunbirdApiRespContent> getContent() {
		return content;
	}

	public int getCount() {
		return count;
	}

	public void setContent(List<SunbirdApiRespContent> content) {
		this.content = content;
	}

	public void setCount(int count) {
		this.count = count;
	}

}

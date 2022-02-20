package org.sunbird.common.model;

import java.util.List;

public class SearchUserAPIResponse {
	private int count;
	private List<SearchUserApiContent> content;

	public List<SearchUserApiContent> getContent() {
		return content;
	}

	public int getCount() {
		return count;
	}

	public void setContent(List<SearchUserApiContent> content) {
		this.content = content;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "SearchUserAPIResponse{" + "count=" + count + ", content=" + content + '}';
	}
}

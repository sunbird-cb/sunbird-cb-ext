package org.sunbird.common.model;

public class SunbirdApiRespResult {
	private SunbirdApiResultResponse response;
	private SunbirdApiHierarchyResultContent content;

	public SunbirdApiResultResponse getResponse() {
		return response;
	}

	public void setResponse(SunbirdApiResultResponse response) {
		this.response = response;
	}

	public SunbirdApiHierarchyResultContent getContent() {
		return content;
	}

	public void setContent(SunbirdApiHierarchyResultContent content) {
		this.content = content;
	}
}

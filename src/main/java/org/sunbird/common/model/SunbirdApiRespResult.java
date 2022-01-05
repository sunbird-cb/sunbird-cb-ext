package org.sunbird.common.model;

public class SunbirdApiRespResult {
	private SunbirdApiResultResponse response;
	private SunbirdApiHierarchyResultContent content;
	private SunbirdApiHierarchyResultBatch batch;

	public SunbirdApiHierarchyResultBatch getBatch() {
		return batch;
	}

	public SunbirdApiHierarchyResultContent getContent() {
		return content;
	}

	public SunbirdApiResultResponse getResponse() {
		return response;
	}

	public void setBatch(SunbirdApiHierarchyResultBatch batch) {
		this.batch = batch;
	}

	public void setContent(SunbirdApiHierarchyResultContent content) {
		this.content = content;
	}

	public void setResponse(SunbirdApiResultResponse response) {
		this.response = response;
	}
}

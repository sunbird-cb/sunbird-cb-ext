package org.sunbird.common.model;

import java.util.List;
import java.util.Map;

public class SunbirdApiRespResult {
	private SunbirdApiResultResponse response;
	private SunbirdApiHierarchyResultContent content;
	private SunbirdApiHierarchyResultBatch batch;
	private Map<String, Object> questionSet;
	private List<Map<String, Object>> questions;

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
	public SunbirdApiHierarchyResultBatch getBatch() {
		return batch;
	}

	public void setBatch(SunbirdApiHierarchyResultBatch batch) {
		this.batch = batch;
	}

	public Map<String, Object> getQuestionSet() {
		return questionSet;
	}

	public void setQuestionSet(Map<String, Object> questionSet) {
		this.questionSet = questionSet;
	}

	public List<Map<String, Object>> getQuestions() {
		return questions;
	}

	public void setQuestions(List<Map<String, Object>> questions) {
		this.questions = questions;
	}
}


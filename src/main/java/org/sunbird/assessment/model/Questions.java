package org.sunbird.assessment.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Questions implements Serializable {

	private static final long serialVersionUID = 1L;
	private String questionId;
	private String question;
	private String questionType;
	private List<Map<String, Object>> options;
	private Boolean multiSelection;

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getQuestionType() {
		return questionType;
	}

	public void setQuestionType(String questionType) {
		this.questionType = questionType;
	}

	public List<Map<String, Object>> getOptions() {
		return options;
	}

	public void setOptions(List<Map<String, Object>> options) {
		this.options = options;
	}

	public Boolean getMultiSelection() {
		return multiSelection;
	}

	public void setMultiSelection(Boolean multiSelection) {
		this.multiSelection = multiSelection;
	}

}

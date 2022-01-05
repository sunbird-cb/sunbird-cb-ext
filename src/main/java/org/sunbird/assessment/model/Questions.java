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

	public Boolean getMultiSelection() {
		return multiSelection;
	}

	public List<Map<String, Object>> getOptions() {
		return options;
	}

	public String getQuestion() {
		return question;
	}

	public String getQuestionId() {
		return questionId;
	}

	public String getQuestionType() {
		return questionType;
	}

	public void setMultiSelection(Boolean multiSelection) {
		this.multiSelection = multiSelection;
	}

	public void setOptions(List<Map<String, Object>> options) {
		this.options = options;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public void setQuestionType(String questionType) {
		this.questionType = questionType;
	}

}

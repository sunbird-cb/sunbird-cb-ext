package org.sunbird.assessment.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionSet implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer timeLimit;
	private Boolean isAssessment;
	private List<Questions> questions;

	public Integer getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(Integer timeLimit) {
		this.timeLimit = timeLimit;
	}

	public Boolean getIsAssessment() {
		return isAssessment;
	}

	public void setIsAssessment(Boolean isAssessment) {
		this.isAssessment = isAssessment;
	}

	public List<Questions> getQuestions() {
		return questions;
	}

	public void setQuestions(List<Questions> questions) {
		this.questions = questions;
	}

}

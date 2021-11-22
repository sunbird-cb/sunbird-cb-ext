package org.sunbird.assessment.model;

import java.util.List;

public class QuestionSet {

	private int timeLimit;
	private Boolean isAssessment;
	private List<Questions> questions;

	public int getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(int timeLimit) {
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

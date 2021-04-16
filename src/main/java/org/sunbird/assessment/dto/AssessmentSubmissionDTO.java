package org.sunbird.assessment.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class AssessmentSubmissionDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotNull(message = "{submission.timeLimit.mandatory}")
	private Long timeLimit;

	@NotNull(message = "{submission.isAssessment.mandatory}")
	private Boolean isAssessment;

	@NotNull(message = "{submission.questions.mandatory}")
	private List<Map<String, Object>> questions;

	@NotNull(message = "{submission.identifier.mandatory}")
	private String identifier;

	@NotNull(message = "{submission.title.mandatory}")
	private String title;

	public Long getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(Long timeLimit) {
		this.timeLimit = timeLimit;
	}

	public Boolean isAssessment() {
		return isAssessment;
	}

	public void setIsAssessment(Boolean isAssessment) {
		this.isAssessment = isAssessment;
	}

	public List<Map<String, Object>> getQuestions() {
		return questions;
	}

	public void setQuestions(List<Map<String, Object>> questions) {
		this.questions = questions;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return "AssessmentSubmissionDTO [timeLimit=" + timeLimit + ", isAssessment=" + isAssessment + ", questions="
				+ questions + ", identifier=" + identifier + ", title=" + title + "]";
	}
}

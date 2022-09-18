package org.sunbird.assessment.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sunbird.common.util.Constants;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaSubmitResult implements Serializable {

	private static final long serialVersionUID = 1L;
	private String courseId;
	private String batchId;
	private String userId;
	private String contentId;
	private Timestamp createdOn;
	private Timestamp lastAttemptOn;
	private List<String> questionList;
	private Double totalMaxScore;
	private Double totalScore;
	private Timestamp updatedOn;

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public Timestamp getLastAttemptOn() {
		return lastAttemptOn;
	}

	public void setLastAttemptOn(Timestamp lastAttemptOn) {
		this.lastAttemptOn = lastAttemptOn;
	}

	public List<String> getQuestionList() {
		return questionList;
	}

	public void setQuestionList(List<String> questionList) {
		this.questionList = questionList;
	}

	public Double getTotalMaxScore() {
		return totalMaxScore;
	}

	public void setTotalMaxScore(Double totalMaxScore) {
		this.totalMaxScore = totalMaxScore;
	}

	public Double getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(Double totalScore) {
		this.totalScore = totalScore;
	}

	public Timestamp getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Timestamp updatedOn) {
		this.updatedOn = updatedOn;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("courseId", courseId)
				.append("batchId", batchId)
				.append("userId", userId)
				.append("contentId", contentId)
				.append("createdOn", createdOn)
				.append("lastAttemptOn", lastAttemptOn)
				.append("questionList", questionList)
				.append("totalMaxScore", totalMaxScore)
				.append("totalScore", totalScore)
				.append("updatedOn", updatedOn)
				.toString();
	}
}

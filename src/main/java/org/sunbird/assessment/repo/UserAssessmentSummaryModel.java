package org.sunbird.assessment.repo;

import java.util.Date;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("user_assessment_summary")
public class UserAssessmentSummaryModel {

	@PrimaryKey
	private UserAssessmentSummaryPrimaryKeyModel primaryKey;

	@Column("max_score")
	private Float firstMaxScore;

	@Column("max_score_date")
	private Date firstMaxScoreDate;

	@Column("first_passed_score")
	private Float firstPassesScore;

	@Column("first_passed_score_date")
	private Date firstPassesScoreDate;

	public UserAssessmentSummaryModel() {
	}

	public UserAssessmentSummaryModel(UserAssessmentSummaryPrimaryKeyModel primaryKey, Float firstMaxScore,
			Date firstMaxScoreDate, Float firstPassesScore, Date firstPassesScoreDate) {
		this.primaryKey = primaryKey;
		this.firstMaxScore = firstMaxScore;
		this.firstMaxScoreDate = firstMaxScoreDate;
		this.firstPassesScore = firstPassesScore;
		this.firstPassesScoreDate = firstPassesScoreDate;
	}

	public Float getFirstMaxScore() {
		return firstMaxScore;
	}

	public Date getFirstMaxScoreDate() {
		return firstMaxScoreDate;
	}

	public Float getFirstPassesScore() {
		return firstPassesScore;
	}

	public Date getFirstPassesScoreDate() {
		return firstPassesScoreDate;
	}

	public UserAssessmentSummaryPrimaryKeyModel getPrimaryKey() {
		return primaryKey;
	}

	public void setFirstMaxScore(Float firstMaxScore) {
		this.firstMaxScore = firstMaxScore;
	}

	public void setFirstMaxScoreDate(Date firstMaxScoreDate) {
		this.firstMaxScoreDate = firstMaxScoreDate;
	}

	public void setFirstPassesScore(Float firstPassesScore) {
		this.firstPassesScore = firstPassesScore;
	}

	public void setFirstPassesScoreDate(Date firstPassesScoreDate) {
		this.firstPassesScoreDate = firstPassesScoreDate;
	}

	public void setPrimaryKey(UserAssessmentSummaryPrimaryKeyModel primaryKey) {
		this.primaryKey = primaryKey;
	}

	@Override
	public String toString() {
		return "UserAssessmentSummaryModel [primaryKey=" + primaryKey + ", firstMaxScore=" + firstMaxScore
				+ ", firstMaxScoreDate=" + firstMaxScoreDate + ", firstPassesScore=" + firstPassesScore
				+ ", firstPassesScoreDate=" + firstPassesScoreDate + "]";
	}

}

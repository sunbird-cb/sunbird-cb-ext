package org.sunbird.assessment.repo;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("user_quiz_master")
public class UserQuizMasterModel {

	@PrimaryKey
	private UserQuizMasterPrimaryKeyModel primaryKey;

	@Column("correct_count")
	private Integer correctCount;
	@Column("date_created")
	private Date dateCreated;
	@Column("incorrect_count")
	private Integer incorrectCount;
	@Column("not_answered_count")
	private Integer notAnsweredCount;
	@Column("pass_percent")
	private BigDecimal passPercent;
	@Column("source_id")
	private String sourceId;
	@Column("source_title")
	private String sourceTitle;
	@Column("user_id")
	private String userId;

	public UserQuizMasterModel() {
	}

	public UserQuizMasterModel(UserQuizMasterPrimaryKeyModel primaryKey, Integer correctCount, Date dateCreated,
			Integer incorrectCount, Integer notAnsweredCount, BigDecimal passPercent, String sourceId,
			String sourceTitle, String userId) {
		this.primaryKey = primaryKey;
		this.correctCount = correctCount;
		this.dateCreated = dateCreated;
		this.incorrectCount = incorrectCount;
		this.notAnsweredCount = notAnsweredCount;
		this.passPercent = passPercent;
		this.sourceId = sourceId;
		this.sourceTitle = sourceTitle;
		this.userId = userId;
	}

	public Integer getCorrectCount() {
		return correctCount;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public Integer getIncorrectCount() {
		return incorrectCount;
	}

	public Integer getNotAnsweredCount() {
		return notAnsweredCount;
	}

	public BigDecimal getPassPercent() {
		return passPercent;
	}

	public UserQuizMasterPrimaryKeyModel getPrimaryKey() {
		return primaryKey;
	}

	public String getSourceId() {
		return sourceId;
	}

	public String getSourceTitle() {
		return sourceTitle;
	}

	public String getUserId() {
		return userId;
	}

	public void setCorrectCount(Integer correctCount) {
		this.correctCount = correctCount;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public void setIncorrectCount(Integer incorrectCount) {
		this.incorrectCount = incorrectCount;
	}

	public void setNotAnsweredCount(Integer notAnsweredCount) {
		this.notAnsweredCount = notAnsweredCount;
	}

	public void setPassPercent(BigDecimal passPercent) {
		this.passPercent = passPercent;
	}

	public void setPrimaryKey(UserQuizMasterPrimaryKeyModel primaryKey) {
		this.primaryKey = primaryKey;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public void setSourceTitle(String sourceTitle) {
		this.sourceTitle = sourceTitle;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "UserQuizMasterModel [primaryKey=" + primaryKey + ", correctCount=" + correctCount + ", dateCreated="
				+ dateCreated + ", incorrectCount=" + incorrectCount + ", notAnsweredCount=" + notAnsweredCount
				+ ", passPercent=" + passPercent + ", sourceId=" + sourceId + ", sourceTitle=" + sourceTitle
				+ ", userId=" + userId + "]";
	}
}

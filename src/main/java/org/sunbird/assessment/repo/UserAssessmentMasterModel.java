package org.sunbird.assessment.repo;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("user_assessment_master")
public class UserAssessmentMasterModel {

	@PrimaryKey
	private UserAssessmentMasterPrimaryKeyModel primaryKey;

	@Column("correct_count")
	private Integer correctCount;
	@Column("date_created")
	private Date dateCreated;
	@Column("incorrect_count")
	private Integer incorrectCount;
	@Column("not_answered_count")
	private Integer notAnsweredCount;
	@Column("parent_content_type")
	private String parentContentType;
	@Column("pass_percent")
	private BigDecimal passPercent;
	@Column("source_id")
	private String sourceId;
	@Column("source_title")
	private String sourceTitle;
	@Column("user_id")
	private String userId;

	public UserAssessmentMasterModel() {
	}

	public UserAssessmentMasterModel(UserAssessmentMasterPrimaryKeyModel primaryKey, Integer correctCount,
			Date dateCreated, Integer incorrectCount, Integer notAnsweredCount, String parentContentType,
			BigDecimal passPercent, String sourceId, String sourceTitle, String userId) {
		this.primaryKey = primaryKey;
		this.correctCount = correctCount;
		this.dateCreated = dateCreated;
		this.incorrectCount = incorrectCount;
		this.notAnsweredCount = notAnsweredCount;
		this.parentContentType = parentContentType;
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

	public String getParentContentType() {
		return parentContentType;
	}

	public BigDecimal getPassPercent() {
		return passPercent;
	}

	public UserAssessmentMasterPrimaryKeyModel getPrimaryKey() {
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

	public void setParentContentType(String parentContentType) {
		this.parentContentType = parentContentType;
	}

	public void setPassPercent(BigDecimal passPercent) {
		this.passPercent = passPercent;
	}

	public void setPrimaryKey(UserAssessmentMasterPrimaryKeyModel primaryKey) {
		this.primaryKey = primaryKey;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public void setSourceTitle(String sourceTitle) {
		this.sourceTitle = sourceTitle;
	}

	public void setUser_id(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "UserAssessmentMasterModel [primaryKey=" + primaryKey + ", correctCount=" + correctCount
				+ ", dateCreated=" + dateCreated + ", incorrectCount=" + incorrectCount + ", notAnsweredCount="
				+ notAnsweredCount + ", parentContentType=" + parentContentType + ", passPercent=" + passPercent
				+ ", sourceId=" + sourceId + ", sourceTitle=" + sourceTitle + ", userId=" + userId + "]";
	}

}

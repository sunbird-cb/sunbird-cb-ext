package org.sunbird.assessment.repo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.sunbird.common.util.Constants;

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

	public UserAssessmentMasterPrimaryKeyModel getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(UserAssessmentMasterPrimaryKeyModel primaryKey) {
		this.primaryKey = primaryKey;
	}

	public Integer getCorrectCount() {
		return correctCount;
	}

	public void setCorrectCount(Integer correctCount) {
		this.correctCount = correctCount;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Integer getIncorrectCount() {
		return incorrectCount;
	}

	public void setIncorrectCount(Integer incorrectCount) {
		this.incorrectCount = incorrectCount;
	}

	public Integer getNotAnsweredCount() {
		return notAnsweredCount;
	}

	public void setNotAnsweredCount(Integer notAnsweredCount) {
		this.notAnsweredCount = notAnsweredCount;
	}

	public String getParentContentType() {
		return parentContentType;
	}

	public void setParentContentType(String parentContentType) {
		this.parentContentType = parentContentType;
	}

	public BigDecimal getPassPercent() {
		return passPercent;
	}

	public void setPassPercent(BigDecimal passPercent) {
		this.passPercent = passPercent;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getSourceTitle() {
		return sourceTitle;
	}

	public void setSourceTitle(String sourceTitle) {
		this.sourceTitle = sourceTitle;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public UserAssessmentMasterModel(Map<String, Object> userAssessmentMasterModelData) {
		super();
		this.primaryKey = (UserAssessmentMasterPrimaryKeyModel) userAssessmentMasterModelData.get("primaryKey");
		this.correctCount = (java.lang.Integer) userAssessmentMasterModelData.get("correctCount");
		this.dateCreated = (Date) userAssessmentMasterModelData.get(Constants.DATE_CREATED_ON);
		this.incorrectCount = (java.lang.Integer) userAssessmentMasterModelData.get(Constants.INCORRECT_COUNT);
		this.notAnsweredCount = (java.lang.Integer) userAssessmentMasterModelData.get(Constants.NOT_ANSWERED_COUNT);
		this.parentContentType = (String) userAssessmentMasterModelData.get(Constants.PARENT_CONTENT_TYPE);
		this.passPercent = (BigDecimal) userAssessmentMasterModelData.get(Constants.PASS_PERCENTAGE);
		this.sourceId = (String) userAssessmentMasterModelData.get(Constants.SOURCE_ID);
		this.sourceTitle = (String) userAssessmentMasterModelData.get("sourceTitle");
		this.userId = (String) userAssessmentMasterModelData.get("userId");
	}

	public UserAssessmentMasterModel() {
		super();
	}

	@Override
	public String toString() {
		return "UserAssessmentMasterModel [primaryKey=" + primaryKey + ", correctCount=" + correctCount
				+ ", dateCreated=" + dateCreated + ", incorrectCount=" + incorrectCount + ", notAnsweredCount="
				+ notAnsweredCount + ", parentContentType=" + parentContentType + ", passPercent=" + passPercent
				+ ", sourceId=" + sourceId + ", sourceTitle=" + sourceTitle + ", userId=" + userId + "]";
	}

}

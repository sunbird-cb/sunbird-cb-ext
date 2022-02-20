package org.sunbird.assessment.repo;

import java.math.BigDecimal;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("user_assessment_top_performer")
public class UserAssessmentTopPerformerModel {
	@PrimaryKey
	private UserAssessmentTopPerformerPrimaryKeyModel primaryKey;
	@Column("pass_percent")
	private BigDecimal passPercent;
	@Column("source_id")
	private String sourceId;
	@Column("source_title")
	private String sourceTitle;
	@Column("user_id")
	private String userId;

	public UserAssessmentTopPerformerModel() {
	}

	public UserAssessmentTopPerformerModel(UserAssessmentTopPerformerPrimaryKeyModel primaryKey, BigDecimal passPercent,
			String sourceId, String sourceTitle, String userId) {
		this.primaryKey = primaryKey;
		this.passPercent = passPercent;
		this.sourceId = sourceId;
		this.sourceTitle = sourceTitle;
		this.userId = userId;
	}

	public BigDecimal getPassPercent() {
		return passPercent;
	}

	public UserAssessmentTopPerformerPrimaryKeyModel getPrimaryKey() {
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

	public void setPassPercent(BigDecimal passPercent) {
		this.passPercent = passPercent;
	}

	public void setPrimaryKey(UserAssessmentTopPerformerPrimaryKeyModel primaryKey) {
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
		return "UserAssessmentTopPerformerModel [primaryKey=" + primaryKey + ", passPercent=" + passPercent
				+ ", sourceId=" + sourceId + ", sourceTitle=" + sourceTitle + ", userId=" + userId + "]";
	}
}

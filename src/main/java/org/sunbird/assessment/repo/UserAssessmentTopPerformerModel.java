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

	public UserAssessmentTopPerformerPrimaryKeyModel getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(UserAssessmentTopPerformerPrimaryKeyModel primaryKey) {
		this.primaryKey = primaryKey;
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

	public UserAssessmentTopPerformerModel(UserAssessmentTopPerformerPrimaryKeyModel primaryKey, BigDecimal passPercent,
			String sourceId, String sourceTitle, String userId) {
		super();
		this.primaryKey = primaryKey;
		this.passPercent = passPercent;
		this.sourceId = sourceId;
		this.sourceTitle = sourceTitle;
		this.userId = userId;
	}

	public UserAssessmentTopPerformerModel() {
		super();
	}

	@Override
	public String toString() {
		return "UserAssessmentTopPerformerModel [primaryKey=" + primaryKey + ", passPercent=" + passPercent
				+ ", sourceId=" + sourceId + ", sourceTitle=" + sourceTitle + ", userId=" + userId + "]";
	}
}

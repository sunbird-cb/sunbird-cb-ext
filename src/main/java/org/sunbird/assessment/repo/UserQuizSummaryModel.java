package org.sunbird.assessment.repo;

import java.util.Date;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("user_quiz_summary")
public class UserQuizSummaryModel {
	@PrimaryKey
	private UserQuizSummaryPrimaryKeyModel primaryKey;

	@Column("date_updated")
	private Date dateUpdated;

	public UserQuizSummaryModel() {
		super();
	}

	public UserQuizSummaryModel(UserQuizSummaryPrimaryKeyModel primaryKey, Date dateUpdated) {
		super();
		this.primaryKey = primaryKey;
		this.dateUpdated = dateUpdated;
	}

	public UserQuizSummaryPrimaryKeyModel getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(UserQuizSummaryPrimaryKeyModel primaryKey) {
		this.primaryKey = primaryKey;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	@Override
	public String toString() {
		return "UserQuizSummaryModel [primaryKey=" + primaryKey + ", dateUpdated=" + dateUpdated + "]";
	}
}

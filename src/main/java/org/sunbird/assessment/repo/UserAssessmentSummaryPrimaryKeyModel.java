package org.sunbird.assessment.repo;

import java.io.Serializable;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class UserAssessmentSummaryPrimaryKeyModel implements Serializable {
	private static final long serialVersionUID = 1L;

	@PrimaryKeyColumn(name = "root_org", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String rootOrg;

	@PrimaryKeyColumn(name = "user_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	private String userId;

	@PrimaryKeyColumn(name = "content_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
	private String contentId;

	public UserAssessmentSummaryPrimaryKeyModel() {
	}

	public UserAssessmentSummaryPrimaryKeyModel(String rootOrg, String userId, String contentId) {
		this.rootOrg = rootOrg;
		this.userId = userId;
		this.contentId = contentId;
	}

	public String getContentId() {
		return contentId;
	}

	public String getRootOrg() {
		return rootOrg;
	}

	public String getUserId() {
		return userId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public void setRootOrg(String rootOrg) {
		this.rootOrg = rootOrg;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "UserAssessmentSummaryPrimaryKeyModel [rootOrg=" + rootOrg + ", userId=" + userId + ", contentId="
				+ contentId + "]";
	}
}

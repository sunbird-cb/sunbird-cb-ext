package org.sunbird.assessment.repo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class UserAssessmentTopPerformerPrimaryKeyModel implements Serializable {
	private static final long serialVersionUID = 1L;

	@PrimaryKeyColumn(name = "root_org", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String rootOrg;

	@PrimaryKeyColumn(name = "parent_source_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	private String parentSourceId;

	@PrimaryKeyColumn(name = "ts_created", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
	private Date tsCreated;

	@PrimaryKeyColumn(name = "result_percent", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
	private BigDecimal resultPercent;

	@PrimaryKeyColumn(name = "id", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
	private UUID id;

	public String getRootOrg() {
		return rootOrg;
	}

	public void setRootOrg(String rootOrg) {
		this.rootOrg = rootOrg;
	}

	public String getParentSourceId() {
		return parentSourceId;
	}

	public void setParentSourceId(String parentSourceId) {
		this.parentSourceId = parentSourceId;
	}

	public Date getTsCreated() {
		return tsCreated;
	}

	public void setTsCreated(Date tsCreated) {
		this.tsCreated = tsCreated;
	}

	public BigDecimal getResultPercent() {
		return resultPercent;
	}

	public void setResultPercent(BigDecimal resultPercent) {
		this.resultPercent = resultPercent;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UserAssessmentTopPerformerPrimaryKeyModel() {
		super();
	}

	public UserAssessmentTopPerformerPrimaryKeyModel(String rootOrg, String parentSourceId, Date tsCreated,
			BigDecimal resultPercent, UUID id) {
		this.rootOrg = rootOrg;
		this.parentSourceId = parentSourceId;
		this.tsCreated = tsCreated;
		this.resultPercent = resultPercent;
		this.id = id;
		this.rootOrg = rootOrg;
	}

	@Override
	public String toString() {
		return "UserAssessmentTopPerformerPrimaryKeyModel [rootOrg=" + rootOrg + ", parentSourceId=" + parentSourceId
				+ ", tsCreated=" + tsCreated + ", resultPercent=" + resultPercent + ", id=" + id + "]";
	}
}

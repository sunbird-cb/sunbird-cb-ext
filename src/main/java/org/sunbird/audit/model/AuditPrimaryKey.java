package org.sunbird.audit.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class AuditPrimaryKey {

	@PrimaryKeyColumn(name = "orgId", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String orgId;

	@PrimaryKeyColumn(name = "auditType", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
	private String auditType;

	@PrimaryKeyColumn(name = "createdDate", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
	private String createdDate;

	@PrimaryKeyColumn(name = "updatedDate", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
	private String updatedDate;

	public String getAuditType() {
		return auditType;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public String getOrgId() {
		return orgId;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setAuditType(String auditType) {
		this.auditType = auditType;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

}

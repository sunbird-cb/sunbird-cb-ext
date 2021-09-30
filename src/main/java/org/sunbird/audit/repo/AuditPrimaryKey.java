package org.sunbird.audit.repo;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class AuditPrimaryKey {
	
	private static final long serialVersionUID = 1L;
	
    @PrimaryKeyColumn(name = "orgId", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String orgId;
    
    @PrimaryKeyColumn(name = "auditType", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private String auditType;
    
    @PrimaryKeyColumn(name = "createdDate", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private String createdDate;
    
    @PrimaryKeyColumn(name = "updatedDate", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private String updatedDate;

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getAuditType() {
		return auditType;
	}

	public void setAuditType(String auditType) {
		this.auditType = auditType;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

}


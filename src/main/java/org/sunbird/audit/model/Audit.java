package org.sunbird.audit.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("org_audit")
public class Audit {

	@PrimaryKey
	private AuditPrimaryKey primaryKey;

	@Column("createdBy")
	private String createdBy;

	@Column("updatedBy")
	private String updatedBy;

	@Column("transactionDetails")
	private String transactionDetails;

	public Audit() {
	}

	public Audit(String orgId, String auditType, String createdDate, String createdBy, String updatedDate,
			String updatedBy, String transactionDetails) {
		this.primaryKey = new AuditPrimaryKey();
		this.primaryKey.setOrgId(orgId);
		this.primaryKey.setAuditType(auditType);
		this.primaryKey.setCreatedDate(createdDate);
		this.primaryKey.setUpdatedDate(updatedDate);
		this.createdBy = createdBy;
		this.updatedBy = updatedBy;
		this.transactionDetails = transactionDetails;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public AuditPrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	public String getTransactionDetails() {
		return transactionDetails;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public void setPrimaryKey(AuditPrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
	}

	public void setTransactionDetails(String transactionDetails) {
		this.transactionDetails = transactionDetails;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

}

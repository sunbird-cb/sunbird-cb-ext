package org.sunbird.staff.budget.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("org_audit")
public class Audit {
	
	public Audit() {
        super();
    }

    public Audit(String orgId,String auditType, String createdDate, String createdBy, String updatedDate, String updatedBy, String transactionDetails) {
        this.primaryKey = new AuditPrimaryKey();
        this.primaryKey.setOrgId(orgId);
        this.primaryKey.setAuditType(auditType);
        this.primaryKey.setCreatedDate(createdDate);
        this.primaryKey.setUpdatedDate(updatedDate);
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.transactionDetails = transactionDetails;
    }

    @PrimaryKey
    private AuditPrimaryKey primaryKey;

    @Column("createdBy")
    private String createdBy;
    
    @Column("updatedBy")
    private String updatedBy;
    
    @Column("transactionDetails")
    private String transactionDetails;

	public AuditPrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(AuditPrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getTransactionDetails() {
		return transactionDetails;
	}

	public void setTransactionDetails(String transactionDetails) {
		this.transactionDetails = transactionDetails;
	}

}

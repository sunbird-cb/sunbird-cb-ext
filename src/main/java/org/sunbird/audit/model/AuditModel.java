package org.sunbird.audit.model;

import java.util.List;
import java.util.Map;

public class AuditModel {

	private String orgId;
	private String auditType;
	private String createdDate;
	private String createdBy;
	private String updatedDate;
	private String updatedBy;
	private Object transactionDetails;
	private String position;
	private int totalPositionsFilled;
	private int totalPositionsVacant;
	private String budgetYear;
	private List<Map<String, String>> proofDocs;
	private long salaryBudgetAllocated;
	private String schemeName;
	private long trainingBudgetAllocated;
	private long trainingBudgetUtilization;

	public String getBudgetYear() {
		return budgetYear;
	}

	public void setBudgetYear(String budgetYear) {
		this.budgetYear = budgetYear;
	}

	public List<Map<String, String>> getProofDocs() {
		return proofDocs;
	}

	public void setProofDocs(List<Map<String, String>> proofDocs) {
		this.proofDocs = proofDocs;
	}

	public long getSalaryBudgetAllocated() {
		return salaryBudgetAllocated;
	}

	public void setSalaryBudgetAllocated(long salaryBudgetAllocated) {
		this.salaryBudgetAllocated = salaryBudgetAllocated;
	}

	public String getSchemeName() {
		return schemeName;
	}

	public void setSchemeName(String schemeName) {
		this.schemeName = schemeName;
	}

	public long getTrainingBudgetAllocated() {
		return trainingBudgetAllocated;
	}

	public void setTrainingBudgetAllocated(long trainingBudgetAllocated) {
		this.trainingBudgetAllocated = trainingBudgetAllocated;
	}

	public long getTrainingBudgetUtilization() {
		return trainingBudgetUtilization;
	}

	public void setTrainingBudgetUtilization(long trainingBudgetUtilization) {
		this.trainingBudgetUtilization = trainingBudgetUtilization;
	}

//	public void setTransactionDetails(StaffInfo transactionDetails) {
//		this.transactionDetails = transactionDetails;
//	}
	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public int getTotalPositionsFilled() {
		return totalPositionsFilled;
	}

	public void setTotalPositionsFilled(int totalPositionsFilled) {
		this.totalPositionsFilled = totalPositionsFilled;
	}

	public int getTotalPositionsVacant() {
		return totalPositionsVacant;
	}

	public void setTotalPositionsVacant(int totalPositionsVacant) {
		this.totalPositionsVacant = totalPositionsVacant;
	}

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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Object getTransactionDetails() {
		return transactionDetails;
	}

	public void setTransactionDetails(Object transactionDetails) {
		this.transactionDetails = transactionDetails;
	}

}

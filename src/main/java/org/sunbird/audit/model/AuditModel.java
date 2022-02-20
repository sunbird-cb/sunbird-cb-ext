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

	public String getAuditType() {
		return auditType;
	}

	public String getBudgetYear() {
		return budgetYear;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public String getOrgId() {
		return orgId;
	}

	public String getPosition() {
		return position;
	}

	public List<Map<String, String>> getProofDocs() {
		return proofDocs;
	}

	public long getSalaryBudgetAllocated() {
		return salaryBudgetAllocated;
	}

	public String getSchemeName() {
		return schemeName;
	}

	public int getTotalPositionsFilled() {
		return totalPositionsFilled;
	}

	public int getTotalPositionsVacant() {
		return totalPositionsVacant;
	}

	public long getTrainingBudgetAllocated() {
		return trainingBudgetAllocated;
	}

	public long getTrainingBudgetUtilization() {
		return trainingBudgetUtilization;
	}

	public Object getTransactionDetails() {
		return transactionDetails;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setAuditType(String auditType) {
		this.auditType = auditType;
	}

	public void setBudgetYear(String budgetYear) {
		this.budgetYear = budgetYear;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public void setProofDocs(List<Map<String, String>> proofDocs) {
		this.proofDocs = proofDocs;
	}

	public void setSalaryBudgetAllocated(long salaryBudgetAllocated) {
		this.salaryBudgetAllocated = salaryBudgetAllocated;
	}

	public void setSchemeName(String schemeName) {
		this.schemeName = schemeName;
	}

	public void setTotalPositionsFilled(int totalPositionsFilled) {
		this.totalPositionsFilled = totalPositionsFilled;
	}

	public void setTotalPositionsVacant(int totalPositionsVacant) {
		this.totalPositionsVacant = totalPositionsVacant;
	}

	public void setTrainingBudgetAllocated(long trainingBudgetAllocated) {
		this.trainingBudgetAllocated = trainingBudgetAllocated;
	}

	public void setTrainingBudgetUtilization(long trainingBudgetUtilization) {
		this.trainingBudgetUtilization = trainingBudgetUtilization;
	}

	public void setTransactionDetails(Object transactionDetails) {
		this.transactionDetails = transactionDetails;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

}

package org.sunbird.budget.model;

import java.util.List;
import java.util.Map;

public class BudgetInfo {

	private String id;
	private String orgId;
	private String budgetYear;
	private String schemeName;
	private Long salaryBudgetAllocated;
	private Long trainingBudgetAllocated;
	private Long trainingBudgetUtilization;
	private List<Map<String, String>> proofDocs;

	public String getBudgetYear() {
		return budgetYear;
	}

	public String getId() {
		return id;
	}

	public String getOrgId() {
		return orgId;
	}

	public List<Map<String, String>> getProofDocs() {
		return proofDocs;
	}

	public Long getSalaryBudgetAllocated() {
		return salaryBudgetAllocated;
	}

	public String getSchemeName() {
		return schemeName;
	}

	public Long getTrainingBudgetAllocated() {
		return trainingBudgetAllocated;
	}

	public Long getTrainingBudgetUtilization() {
		return trainingBudgetUtilization;
	}

	public void setBudgetYear(String budgetYear) {
		this.budgetYear = budgetYear;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public void setProofDocs(List<Map<String, String>> proofDocs) {
		this.proofDocs = proofDocs;
	}

	public void setSalaryBudgetAllocated(Long salaryBudgetAllocated) {
		this.salaryBudgetAllocated = salaryBudgetAllocated;
	}

	public void setSchemeName(String schemeName) {
		this.schemeName = schemeName;
	}

	public void setTrainingBudgetAllocated(Long trainingBudgetAllocated) {
		this.trainingBudgetAllocated = trainingBudgetAllocated;
	}

	public void setTrainingBudgetUtilization(Long trainingBudgetUtilization) {
		this.trainingBudgetUtilization = trainingBudgetUtilization;
	}
}
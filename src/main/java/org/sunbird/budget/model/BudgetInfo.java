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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

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

	public Long getSalaryBudgetAllocated() {
		return salaryBudgetAllocated;
	}

	public void setSalaryBudgetAllocated(Long salaryBudgetAllocated) {
		this.salaryBudgetAllocated = salaryBudgetAllocated;
	}

	public String getSchemeName() {
		return schemeName;
	}

	public void setSchemeName(String schemeName) {
		this.schemeName = schemeName;
	}

	public Long getTrainingBudgetAllocated() {
		return trainingBudgetAllocated;
	}

	public void setTrainingBudgetAllocated(Long trainingBudgetAllocated) {
		this.trainingBudgetAllocated = trainingBudgetAllocated;
	}

	public Long getTrainingBudgetUtilization() {
		return trainingBudgetUtilization;
	}

	public void setTrainingBudgetUtilization(Long trainingBudgetUtilization) {
		this.trainingBudgetUtilization = trainingBudgetUtilization;
	}
}
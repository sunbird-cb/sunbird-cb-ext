package org.sunbird.budget.model;

import java.util.List;
import java.util.Map;

public class BudgetInfo {
	
	private String id;
	private String orgId;
	private String budgetYear;
	private List<Map<String, String>> proofdocs;
	private long salaryBudgetAllocated;
	private String schemeName;
	private long trainingBudgetAllocated;
	private long trainingBudgetUtilization;
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
	public List<Map<String, String>> getProofdocs() {
		return proofdocs;
	}
	public void setProofdocs(List<Map<String, String>> proofdocs) {
		this.proofdocs = proofdocs;
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
	
}

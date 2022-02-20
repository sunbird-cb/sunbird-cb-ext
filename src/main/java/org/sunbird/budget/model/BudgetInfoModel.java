package org.sunbird.budget.model;

import java.util.List;
import java.util.Map;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("org_budget_scheme")
public class BudgetInfoModel {

	@PrimaryKey
	private BudgetInfoPrimaryKeyModel primaryKey;

	@Column("proofDocs")
	private List<Map<String, String>> proofDocs;

	@Column("salaryBudgetAllocated")
	private long salaryBudgetAllocated;

	@Column("schemeName")
	private String schemeName;

	@Column("trainingBudgetAllocated")
	private long trainingBudgetAllocated;

	@Column("trainingBudgetUtilization")
	private long trainingBudgetUtilization;

	public BudgetInfoModel() {
	}

	public BudgetInfoModel(BudgetInfoPrimaryKeyModel primaryKey, String schemeName, long salaryBudgetAllocated,
			long trainingBudgetAllocated, long trainingBudgetUtilization) {
		this.primaryKey = primaryKey;
		this.schemeName = schemeName;
		this.salaryBudgetAllocated = salaryBudgetAllocated;
		this.trainingBudgetAllocated = trainingBudgetAllocated;
		this.trainingBudgetUtilization = trainingBudgetUtilization;
	}

	public BudgetInfo getBudgetInfo() {
		BudgetInfo bInfo = new BudgetInfo();
		bInfo.setOrgId(primaryKey.getOrgId());
		bInfo.setBudgetYear(primaryKey.getBudgetYear());
		bInfo.setId(primaryKey.getId());
		bInfo.setSalaryBudgetAllocated(salaryBudgetAllocated);
		bInfo.setSchemeName(schemeName);
		bInfo.setTrainingBudgetAllocated(trainingBudgetAllocated);
		bInfo.setTrainingBudgetUtilization(trainingBudgetUtilization);
		return bInfo;
	}

	public BudgetInfoPrimaryKeyModel getPrimaryKey() {
		return primaryKey;
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

	public long getTrainingBudgetAllocated() {
		return trainingBudgetAllocated;
	}

	public long getTrainingBudgetUtilization() {
		return trainingBudgetUtilization;
	}

	public void setPrimaryKey(BudgetInfoPrimaryKeyModel primaryKey) {
		this.primaryKey = primaryKey;
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

	public void setTrainingBudgetAllocated(long trainingBudgetAllocated) {
		this.trainingBudgetAllocated = trainingBudgetAllocated;
	}

	public void setTrainingBudgetUtilization(long trainingBudgetUtilization) {
		this.trainingBudgetUtilization = trainingBudgetUtilization;
	}
}

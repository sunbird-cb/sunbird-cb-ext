package org.sunbird.budget.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class BudgetInfoPrimaryKeyModel {

	@PrimaryKeyColumn(name = "orgId", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String orgId;

	@PrimaryKeyColumn(name = "budgetYear", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
	private String budgetYear;

	@PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
	private String id;

	public BudgetInfoPrimaryKeyModel() {
	}

	public BudgetInfoPrimaryKeyModel(String orgId, String id, String budgetYear) {
		this.orgId = orgId;
		this.id = id;
		this.budgetYear = budgetYear;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}

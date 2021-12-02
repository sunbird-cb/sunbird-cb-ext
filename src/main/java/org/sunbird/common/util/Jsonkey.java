package org.sunbird.common.util;

/**
 * This class contains all the key related to the cassandra table columns
 * 
 * @author fathima
 *
 */
public final class Jsonkey {

	public static final String ORG_ID = "orgId";
	public static final String AUDIT_TYPE = "auditType";
	public static final String CREATED_DATE = "createdDate";
	public static final String CREATED_BY = "createdBy";
	public static final String UPDATED_DATE = "updatedDate";
	public static final String UPDATED_BY = "updatedBy";
	public static final String TRANSACTION_DETAILS = "transactionDetails";
	public static final String BUDGET_YEAR= "budgetYear";
	public static final String ID = "id";
	public static final String SALARY_BUDGET_ALLOCATED = "salaryBudgetAllocated";
	public static final String TRAINING_BUDGET_ALLOCATED = "trainingBudgetAllocated";
	public static final String TRAINING_BUDGET_UTILIZATION = "trainingBudgetUtilization";
	public static final String SCHEME_NAME = "schemeName";
	public static final String PROOF_DOCS = "proofDocs";
	
	
	// Database and Tables
	public static final String DATABASE = "sunbird";
	public static final String BUDGET_TABLE = "org_budget_scheme";
	public static final String AUDIT_TABLE = "org_audit";
}

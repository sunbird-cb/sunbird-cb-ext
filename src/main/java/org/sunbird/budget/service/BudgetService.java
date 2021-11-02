package org.sunbird.budget.service;

import org.sunbird.budget.model.BudgetDocInfo;
import org.sunbird.budget.model.BudgetInfo;
import org.sunbird.common.model.SBApiResponse;

public interface BudgetService {

	public SBApiResponse submitBudgetDetails(BudgetInfo data, String userId) throws Exception;

	public SBApiResponse getBudgetDetails(String orgId, String budgetYear) throws Exception;

	public SBApiResponse updateBudgetDetails(BudgetInfo data, String userId) throws Exception;

	public SBApiResponse deleteBudgetDetails(String orgId, String id, String budgetYear) throws Exception;

	public SBApiResponse getBudgetAudit(String orgId) throws Exception;

	public SBApiResponse submitBudgetDocDetails(BudgetDocInfo data, String userId) throws Exception;

	public SBApiResponse getBudgetDocDetails(String orgId, String budgetYear) throws Exception;

	public SBApiResponse updateBudgetDocDetails(BudgetDocInfo data, String userId) throws Exception;

}

package org.sunbird.budget.service;

import org.sunbird.budget.model.BudgetDocInfo;
import org.sunbird.budget.model.BudgetInfo;
import org.sunbird.common.model.SBApiResponse;

public interface BudgetService {

	public SBApiResponse submitBudgetDetails(BudgetInfo data, String userId) throws Exception;

	public SBApiResponse submitBudgetDocDetails(BudgetDocInfo docData, String userId) throws Exception;

	public SBApiResponse getBudgetDetails(String orgId, String budgetYear) throws Exception;

	public SBApiResponse updateBudgetDetails(BudgetInfo data, String userId) throws Exception;

	public SBApiResponse deleteBudgetDetails(String orgId, String id, String budgetYear) throws Exception;

	public SBApiResponse deleteDocBudgetDetails(String orgId, String budgetDetailsId, String budgetYear,
			String proofDocId) throws Exception;

	public SBApiResponse getBudgetAudit(String orgId) throws Exception;

}

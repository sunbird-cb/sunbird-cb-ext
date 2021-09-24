package org.sunbird.budget.service;

import org.sunbird.budget.model.BudgetInfo;
import org.sunbird.common.model.Response;


public interface BudgetService {
	
	public Response submitBudgetDetails(BudgetInfo data, String userId) throws Exception;
	
	public Response getBudgetDetails(String orgId) throws Exception;
	
	public Response updateBudgetDetails(BudgetInfo data, String userId) throws Exception;

	public Response deleteBudgetDetails(String orgId, String id, String budgetYear) throws Exception;

}

package org.sunbird.budget.service;

import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.sunbird.budget.model.BudgetDocInfo;
import org.sunbird.budget.model.BudgetInfo;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.exception.MyOwnRuntimeException;

public interface BudgetService {

	public SBApiResponse deleteBudgetDetails(String orgId, String id, String budgetYear);

	public SBApiResponse deleteDocBudgetDetails(String orgId, String budgetDetailsId, String budgetYear,
			String proofDocId);

	public SBApiResponse getBudgetAudit(String orgId) throws ParseException, IOException;

	public SBApiResponse getBudgetDetails(String orgId, String budgetYear);

	public SBApiResponse submitBudgetDetails(BudgetInfo data, String userId);

	public SBApiResponse submitBudgetDocDetails(BudgetDocInfo docData, String userId);

	public SBApiResponse updateBudgetDetails(BudgetInfo data, String userId) throws MyOwnRuntimeException;

}

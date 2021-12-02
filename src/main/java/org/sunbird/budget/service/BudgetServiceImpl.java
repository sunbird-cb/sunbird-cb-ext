package org.sunbird.budget.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbird.audit.model.Audit;
import org.sunbird.audit.repo.AuditRepository;
import org.sunbird.budget.model.BudgetAuditInfo;
import org.sunbird.budget.model.BudgetInfo;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.Jsonkey;
import org.sunbird.cassandra.utils.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BudgetServiceImpl implements BudgetService {
	private ObjectMapper mapper = new ObjectMapper();
	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	
	@Autowired
	private CassandraOperation cassandraOperation;

	@Autowired
	private AuditRepository auditRepository;

	@Override
	public SBApiResponse submitBudgetDetails(BudgetInfo data, String userId) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_BUDGET_SCHEME_ADD);
		try {
			validateAddBudgetInfo(data);
			Map<String, Object> request = new HashMap<>();
			request.put(Jsonkey.ORG_ID, data.getOrgId());
			request.put(Jsonkey.BUDGET_YEAR, data.getBudgetYear());
			request.put(Jsonkey.SCHEME_NAME, data.getSchemeName());

			List<Map<String, Object>> existingDataList = 
					cassandraOperation.getRecordsByProperties(Jsonkey.DATABASE, Jsonkey.BUDGET_TABLE, request, null);

			if (!existingDataList.isEmpty()) {
				String errMsg = "Budget Scheme exist for given name. Failed to create BudgetInfo for OrgId: "
						+ data.getOrgId() + ", BudgetYear: " + data.getBudgetYear() + ", SchemeName: "
						+ data.getSchemeName();
				logger.error(errMsg);
				response.getParams().setErr(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			} 

				request.put(Jsonkey.ID, UUID.randomUUID().toString());
				request.put(Jsonkey.PROOF_DOCS, data.getProofdocs());
				request.put(Jsonkey.SALARY_BUDGET_ALLOCATED, data.getSalaryBudgetAllocated());
				request.put(Jsonkey.TRAINING_BUDGET_ALLOCATED, data.getTrainingBudgetAllocated());
				request.put(Jsonkey.TRAINING_BUDGET_UTILIZATION, data.getTrainingBudgetUtilization());
				
				cassandraOperation.insertRecord(Jsonkey.DATABASE, Jsonkey.BUDGET_TABLE , request);	
				cassandraOperation.insertRecord(Jsonkey.DATABASE, Jsonkey.AUDIT_TABLE, getAuditMap(userId, request, Constants.CREATE));
				
				response.getParams().setStatus(Constants.SUCCESSFUL);
				response.put(Constants.DATA, request);
				response.setResponseCode(HttpStatus.CREATED);
		} catch (Exception ex) {
			String errMsg = "Exception occurred while saving the Budget details. Exception: " + ex.getMessage();
			logger.error(errMsg, ex);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return response;
		}
		return response;
	}

	@Override
	public SBApiResponse getBudgetDetails(String orgId, String budgetYear) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_BUDGET_SCHEME_READ);
		List<Map<String, Object>> budgetResponseList = null;
		String errMsg = null;
		if ("all".equalsIgnoreCase(budgetYear)) {
			errMsg = "No Budget Year Collection found for Org: " + orgId;
		} else {
			budgetResponseList = getSpecificBudgetYearDetails(orgId, budgetYear);
			errMsg = "No Budget Scheme found for Org: " + orgId + ", BudgetYear: " + budgetYear;
		}

		if (CollectionUtils.isEmpty(budgetResponseList)) {
			logger.info(errMsg);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			return response;
		}
		response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
		response.put(Constants.RESPONSE, budgetResponseList);
		response.setResponseCode(HttpStatus.OK);
		return response;
	}

	@Override
	public SBApiResponse updateBudgetDetails(BudgetInfo data, String userId) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_BUDGET_SCHEME_UPDATE);
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put(Jsonkey.ORG_ID, data.getOrgId());
		keyMap.put(Jsonkey.ID, data.getId());
		keyMap.put(Jsonkey.BUDGET_YEAR, data.getBudgetYear());
		try {
			validateUpdateBudgetInfo(data);
			
			List<Map<String, Object>> existingBudgetInfo = 
					cassandraOperation.getRecordsByProperties(Jsonkey.DATABASE, Jsonkey.BUDGET_TABLE, keyMap, null);
			if (existingBudgetInfo.isEmpty()) {
				String errMsg = "Failed to find BudgetScheme for OrgId: " + data.getOrgId() + ", Id: " + data.getId()
						+ ", BudgetYear: " + data.getBudgetYear();
				logger.error(errMsg);
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			}
			if (data.getSchemeName() != null) {
//				 Validate for duplicate schemeNames
				keyMap.put(Jsonkey.SCHEME_NAME, data.getSchemeName());
				List<Map<String, Object>> existingList = 
						cassandraOperation.getRecordsByProperties(Jsonkey.DATABASE, Jsonkey.BUDGET_TABLE, keyMap, null);

				if (!CollectionUtils.isEmpty(existingList)) {
					boolean isOtherRecordExist = false;
					for (Map<String, Object> map : existingList) {
						if (!((String) map.get(Jsonkey.ID)).equalsIgnoreCase(data.getId())) {
							if (((String) map.get(Jsonkey.SCHEME_NAME)).equalsIgnoreCase(data.getSchemeName())) {
								isOtherRecordExist = true;
							}
						}
					}
					if (isOtherRecordExist) {
						String errMsg = "Budget Scheme exist for given name. Failed to update BudgetInfo for OrgId: "
								+ data.getOrgId() + ", BudgetYear: " + data.getBudgetYear() + ", SchemeName: "
								+ data.getSchemeName();
						logger.error(errMsg);
						response.getParams().setErr(errMsg);
						response.setResponseCode(HttpStatus.BAD_REQUEST);
						return response;
					}
				}
				request.put(Jsonkey.SCHEME_NAME, data.getSchemeName());
			}
			
			if (data.getSalaryBudgetAllocated() != null) {
				request.put(Jsonkey.SALARY_BUDGET_ALLOCATED, data.getSalaryBudgetAllocated());
			}
			if (data.getTrainingBudgetAllocated() != null) {
				request.put(Jsonkey.TRAINING_BUDGET_ALLOCATED, data.getTrainingBudgetAllocated());
			}
			if (data.getTrainingBudgetUtilization() != null) {
				request.put(Jsonkey.TRAINING_BUDGET_UTILIZATION, data.getTrainingBudgetUtilization());
			}
			
			cassandraOperation.updateRecord(Jsonkey.DATABASE, Jsonkey.BUDGET_TABLE, request, keyMap);
			
			request.put(Jsonkey.ID, data.getId());
			request.put(Jsonkey.ORG_ID, data.getOrgId());
			request.put(Jsonkey.BUDGET_YEAR, data.getBudgetYear());
			
			cassandraOperation.insertRecord(Jsonkey.DATABASE, Jsonkey.AUDIT_TABLE, getAuditMap(userId, request, Constants.UPDATE));

			response.put(Constants.DATA, request);
			response.getParams().setStatus(Constants.SUCCESSFUL);
			response.setResponseCode(HttpStatus.OK);
		} catch (Exception ex) {
			String errMsg = "Exception occurred while updating the Budget details. Exception: " + ex.getMessage();
			logger.error(errMsg, ex);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public SBApiResponse deleteBudgetDetails(String orgId, String id, String budgetYear) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_BUDGET_SCHEME_DELETE);
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put(Jsonkey.ORG_ID, orgId);
		keyMap.put(Jsonkey.ID, id);
		keyMap.put(Jsonkey.BUDGET_YEAR, budgetYear);
		try {
			List<Map<String, Object>> existingDetails = 
					cassandraOperation.getRecordsByProperties(Jsonkey.DATABASE, Jsonkey.BUDGET_TABLE, keyMap, null);
			if (!existingDetails.isEmpty()) {
				cassandraOperation.deleteRecord(Jsonkey.DATABASE, Jsonkey.BUDGET_TABLE, keyMap);
				response.getParams().setStatus(Constants.SUCCESSFUL);
				response.setResponseCode(HttpStatus.OK);
			} else {
				String errMsg = "Failed to find BudgetScheme for OrgId: " + orgId + ", Id: " + id + ", BudgetYear: "
						+ budgetYear;
				logger.error(errMsg);
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			String errMsg = "Exception occurred while deleting the Budget details. Exception: " + ex.getMessage();
			logger.error(errMsg, ex);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public SBApiResponse getBudgetAudit(String orgId) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_BUDGET_SCHEME_HISTORY_READ);
		List<Audit> auditDetails = auditRepository.getAudit(orgId, Constants.BUDGET);
		if (CollectionUtils.isEmpty(auditDetails)) {
			String errMsg = "Budget Scheme History details not found for Org: " + orgId;
			logger.info(errMsg);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			return response;
		}

		List<BudgetAuditInfo> auditResponse = new ArrayList<>();
		for (Audit audit : auditDetails) {
			BudgetAuditInfo bAuditInfo = new BudgetAuditInfo();
			bAuditInfo.setCreatedBy(audit.getCreatedBy());
			bAuditInfo.setCreatedDate(audit.getPrimaryKey().getCreatedDate());
			bAuditInfo.setUpdatedBy(audit.getUpdatedBy());
			bAuditInfo.setUpdatedDate(audit.getPrimaryKey().getUpdatedDate());
			BudgetInfo bInfo = mapper.readValue(audit.getTransactionDetails(), BudgetInfo.class);
			bAuditInfo.setOrgId(bInfo.getOrgId());
			bAuditInfo.setId(bInfo.getId());
			bAuditInfo.setBudgetYear(bInfo.getBudgetYear());
			bAuditInfo.setSchemeName(bInfo.getSchemeName());
			bAuditInfo.setSalaryBudgetAllocated(bInfo.getSalaryBudgetAllocated());
			bAuditInfo.setTrainingBudgetAllocated(bInfo.getTrainingBudgetAllocated());
			bAuditInfo.setTrainingBudgetUtilization(bInfo.getTrainingBudgetUtilization());
			auditResponse.add(bAuditInfo);
		}
		response.getParams().setStatus(Constants.SUCCESSFUL);
		response.setResponseCode(HttpStatus.OK);
		response.put(Constants.DATA, auditResponse);
		return response;
	}

	private void validateAddBudgetInfo(BudgetInfo budgetInfo) throws Exception {
		List<String> errObjList = new ArrayList<String>();
		if (StringUtils.isEmpty(budgetInfo.getOrgId())) {
			errObjList.add(Constants.ORG_ID);
		}
		if (StringUtils.isEmpty(budgetInfo.getBudgetYear())) {
			errObjList.add(Constants.BUDGET_YEAR);
		}
		if (StringUtils.isEmpty(budgetInfo.getSchemeName())) {
			errObjList.add(Constants.SCHEME_NAME);
		} else {
			if ("all".equalsIgnoreCase(budgetInfo.getSchemeName())) {
				if (budgetInfo.getSalaryBudgetAllocated() == null || budgetInfo.getSalaryBudgetAllocated() < 0) {
					errObjList.add(Constants.SALARY_BUDGET_ALLOCATED);
				}
			}
		}

		if (budgetInfo.getTrainingBudgetAllocated() == null || budgetInfo.getTrainingBudgetAllocated() < 0) {
			errObjList.add(Constants.TRAINING_BUDGET_ALLOCATED);
		}
		if (budgetInfo.getTrainingBudgetUtilization() == null || budgetInfo.getTrainingBudgetUtilization() < 0) {
			errObjList.add(Constants.TRAINING_BUDGET_UTILIZATION);
		}

		if (!CollectionUtils.isEmpty(errObjList)) {
			throw new Exception("One or more required fields are empty. Empty fields " + errObjList.toString());
		}
	}

	private void validateUpdateBudgetInfo(BudgetInfo budgetInfo) throws Exception {
		List<String> errObjList = new ArrayList<String>();
		if (StringUtils.isEmpty(budgetInfo.getOrgId())) {
			errObjList.add(Constants.ORG_ID);
		}
		if (StringUtils.isEmpty(budgetInfo.getId())) {
			errObjList.add(Constants.ID);
		}
		if (StringUtils.isEmpty(budgetInfo.getBudgetYear())) {
			errObjList.add(Constants.BUDGET_YEAR);
		}

		boolean schemeName = false, sBudgetAllocated = false, tBudgetAllocated = false, tBudgetUtilization = false;
		if (budgetInfo.getSchemeName() != null) {
			if (StringUtils.isEmpty(budgetInfo.getSchemeName())) {
				errObjList.add(Constants.SCHEME_NAME);
			}
		} else {
			errObjList.add(Constants.SCHEME_NAME);
			schemeName = true;
		}

		if (budgetInfo.getSalaryBudgetAllocated() != null) {
			if (budgetInfo.getSalaryBudgetAllocated() < 0) {
				errObjList.add(Constants.SALARY_BUDGET_ALLOCATED);
			}
		} else {
			errObjList.add(Constants.SALARY_BUDGET_ALLOCATED);
			sBudgetAllocated = true;
		}
		if (budgetInfo.getTrainingBudgetAllocated() != null) {
			if (budgetInfo.getTrainingBudgetAllocated() < 0) {
				errObjList.add(Constants.TRAINING_BUDGET_ALLOCATED);
			}
		} else {
			errObjList.add(Constants.TRAINING_BUDGET_ALLOCATED);
			tBudgetAllocated = true;
		}

		if (budgetInfo.getTrainingBudgetUtilization() != null) {
			if (budgetInfo.getTrainingBudgetUtilization() < 0) {
				errObjList.add(Constants.TRAINING_BUDGET_UTILIZATION);
			}
		} else {
			errObjList.add(Constants.TRAINING_BUDGET_UTILIZATION);
			tBudgetUtilization = true;
		}

		if (schemeName && sBudgetAllocated && tBudgetAllocated && tBudgetUtilization) {
			throw new Exception("One or more required fields are empty. Empty fields " + errObjList.toString());
		} else {
			if (schemeName)
				errObjList.remove(Constants.SCHEME_NAME);
			if (sBudgetAllocated)
				errObjList.remove(Constants.SALARY_BUDGET_ALLOCATED);
			if (tBudgetAllocated)
				errObjList.remove(Constants.TRAINING_BUDGET_ALLOCATED);
			if (tBudgetUtilization)
				errObjList.remove(Constants.TRAINING_BUDGET_UTILIZATION);
		}

		if (!CollectionUtils.isEmpty(errObjList)) {
			throw new Exception("One or more required fields are empty. Empty fields " + errObjList.toString());
		}
	}
	
//	private List<Object> getAllBudgetYearDetails(String orgId) {
//		List<BudgetInfoModel> budgetDetails = budgetRepository.getDistinctBudgetYear();
//		if (CollectionUtils.isEmpty(budgetDetails)) {
//			return Collections.emptyList();
//		}
//		List<Object> budgetResponse = new ArrayList<>();
//		for (BudgetInfoModel budget : budgetDetails) {
//			if (budget.getPrimaryKey().getOrgId().equals(orgId)) {
//				budgetResponse.add(budget.getPrimaryKey().getBudgetYear());
//			}
//		}
//		return budgetResponse;
//	}
	
	private List<Map<String, Object>> getSpecificBudgetYearDetails(String orgId, String budgetYear) {
		Map<String, Object> propertyMap = new HashMap<>();
		propertyMap.put(Jsonkey.ORG_ID, orgId);
		propertyMap.put(Jsonkey.BUDGET_YEAR, budgetYear);
		List<Map<String, Object>> details = cassandraOperation.getRecordsByProperties(Jsonkey.DATABASE, Jsonkey.BUDGET_TABLE, propertyMap , null);
		if (CollectionUtils.isEmpty(details)) {
			return Collections.emptyList();
		}
		List<Map<String, Object>> response = new ArrayList<>();
		for (Map<String, Object> budget : details) {
			response.add(budget);
		}
		return response;
	}
	
	private Map<String, Object> getAuditMap(String userId, Map<String, Object> data, String operation) throws JsonProcessingException {
		DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");
		Map<String, Object> auditMap = new HashMap<>();
		auditMap.put(Jsonkey.ORG_ID, data.get(Constants.ORG_ID));
		auditMap.put(Jsonkey.AUDIT_TYPE, Constants.BUDGET);
		if (operation.equalsIgnoreCase("Create")) {
			auditMap.put(Jsonkey.CREATED_DATE, dateFormat.format(new Date()));
			auditMap.put(Jsonkey.CREATED_BY, userId);
			auditMap.put(Jsonkey.UPDATED_DATE, StringUtils.EMPTY);
			auditMap.put(Jsonkey.UPDATED_BY, StringUtils.EMPTY);
		}
		if (operation.equalsIgnoreCase("Update")){
			auditMap.put(Jsonkey.CREATED_DATE, StringUtils.EMPTY);
			auditMap.put(Jsonkey.CREATED_BY, StringUtils.EMPTY);
			auditMap.put(Jsonkey.UPDATED_DATE, dateFormat.format(new Date()));
			auditMap.put(Jsonkey.UPDATED_BY, userId);
		}
		auditMap.put(Jsonkey.TRANSACTION_DETAILS, mapper.writeValueAsString(data));
		return auditMap;
	}
}

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
import org.sunbird.budget.model.BudgetAuditInfo;
import org.sunbird.budget.model.BudgetDocInfo;
import org.sunbird.budget.model.BudgetInfo;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BudgetServiceImpl implements BudgetService {
	private ObjectMapper mapper = new ObjectMapper();
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(Constants.DATE_FORMAT);
	private Logger logger = LoggerFactory.getLogger(getClass().getName());

	@Autowired
	private CassandraOperation cassandraOperation;

	@Override
	public SBApiResponse submitBudgetDetails(BudgetInfo data, String userId) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_BUDGET_SCHEME_ADD);
		try {
			validateAddBudgetInfo(data);
			Map<String, Object> request = new HashMap<>();
			request.put(Constants.ORG_ID, data.getOrgId());
			request.put(Constants.BUDGET_YEAR, data.getBudgetYear());
			request.put(Constants.SCHEME_NAME, data.getSchemeName());

			List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
					Constants.TABLE_ORG_BUDGET_SCHEME, request, null);

			if (!existingDataList.isEmpty()) {
				String errMsg = "Budget Scheme exist for given name. Failed to create BudgetInfo for OrgId: "
						+ data.getOrgId() + ", BudgetYear: " + data.getBudgetYear() + ", SchemeName: "
						+ data.getSchemeName();
				logger.error(errMsg);
				response.getParams().setErr(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			}

			request.put(Constants.ID, UUID.randomUUID().toString());
			request.put(Constants.PROOF_DOCS, data.getProofDocs());
			request.put(Constants.SALARY_BUDGET_ALLOCATED, data.getSalaryBudgetAllocated());
			request.put(Constants.TRAINING_BUDGET_ALLOCATED, data.getTrainingBudgetAllocated());
			request.put(Constants.TRAINING_BUDGET_UTILIZATION, data.getTrainingBudgetUtilization());

			cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_ORG_BUDGET_SCHEME, request);
			cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_ORG_AUDIT,
					getAuditMap(userId, request, Constants.CREATE));

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

	public SBApiResponse submitBudgetDocDetails(BudgetDocInfo docInfo, String userId) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_BUDGET_SCHEME_DOC_ADD);
		try {
			validateAddBudgetDocInfo(docInfo);

			Map<String, Object> propertyMap = new HashMap<>();
			propertyMap.put(Constants.ORG_ID, docInfo.getOrgId());
			propertyMap.put(Constants.ID, docInfo.getId());
			propertyMap.put(Constants.BUDGET_YEAR, docInfo.getBudgetYear());
			List<Map<String, Object>> existingBudgetInfo = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
					Constants.TABLE_ORG_BUDGET_SCHEME, propertyMap, new ArrayList<>());

			if (existingBudgetInfo.isEmpty()) {
				String errMsg = "Failed to find BudgetScheme for OrgId: " + docInfo.getOrgId() + ", Id: "
						+ docInfo.getId() + ", BudgetYear: " + docInfo.getBudgetYear();
				logger.error(errMsg);
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			}

			// Verify we already have this Doc.
			BudgetInfo budgetInfo = mapper.convertValue(existingBudgetInfo.get(0), BudgetInfo.class);
			if (budgetInfo.getProofDocs() != null) {
				for (Map<String, String> proofDoc : budgetInfo.getProofDocs()) {
					String existingFileUrl = proofDoc.get(Constants.BUDGET_DOC_FILE_URL);
					if (docInfo.getFileUrl().equalsIgnoreCase(existingFileUrl)) {
						String errMsg = "File already added in to ProofDocs for Id: " + docInfo.getId() + ", FileUrl: "
								+ docInfo.getFileUrl();
						logger.error(errMsg);
						response.getParams().setErrmsg(errMsg);
						response.setResponseCode(HttpStatus.BAD_REQUEST);
						return response;
					}
				}
			} else {
				budgetInfo.setProofDocs(new ArrayList<>());
			}
			Map<String, String> proofDoc = docInfo.getProofDoc();
			proofDoc.put(Constants.BUDGET_DOC_UPLOADED_BY, userId);
			proofDoc.put(Constants.BUDGET_DOC_UPLOADED_DATE, dateFormatter.format(new Date()));
			proofDoc.put(Constants.ID, UUID.randomUUID().toString());
			budgetInfo.getProofDocs().add(proofDoc);

			Map<String, Object> budgetMap = mapper.convertValue(budgetInfo, HashMap.class);
			cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_ORG_BUDGET_SCHEME, budgetMap);

			budgetMap.remove(Constants.PROOF_DOCS);

			Map<String, Object> data = new HashMap<>();
			data.put(Constants.ORG_ID, docInfo.getOrgId());
			data.put(Constants.AUDIT_TYPE, Constants.BUDGET);
			data.put(Constants.TRANSACTION_DETAILS, budgetMap);
			Map<String, Object> auditMap = getAuditMap(userId, data, Constants.UPDATE);
			cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_ORG_AUDIT, auditMap);

			response.put(Constants.DATA, budgetMap);
			response.getParams().setStatus(Constants.SUCCESSFUL);
			response.setResponseCode(HttpStatus.OK);
		} catch (Exception ex) {
			String errMsg = "Exception occurred while saving the Budget Doc details. Exception: " + ex.getMessage();
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
		keyMap.put(Constants.ORG_ID, data.getOrgId());
		keyMap.put(Constants.ID, data.getId());
		keyMap.put(Constants.BUDGET_YEAR, data.getBudgetYear());
		try {
			validateUpdateBudgetInfo(data);

			List<Map<String, Object>> existingBudgetInfo = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
					Constants.TABLE_ORG_BUDGET_SCHEME, keyMap, null);
			if (existingBudgetInfo.isEmpty()) {
				String errMsg = "Failed to find BudgetScheme for OrgId: " + data.getOrgId() + ", Id: " + data.getId()
						+ ", BudgetYear: " + data.getBudgetYear();
				logger.error(errMsg);
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			}
			if (data.getSchemeName() != null) {
				// Validate for duplicate schemeNames
				keyMap.put(Constants.SCHEME_NAME, data.getSchemeName());
				List<Map<String, Object>> existingList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
						Constants.TABLE_ORG_BUDGET_SCHEME, keyMap, null);

				if (!CollectionUtils.isEmpty(existingList)) {
					boolean isOtherRecordExist = false;
					for (Map<String, Object> map : existingList) {
						if (!((String) map.get(Constants.ID)).equalsIgnoreCase(data.getId())) {
							if (((String) map.get(Constants.SCHEME_NAME)).equalsIgnoreCase(data.getSchemeName())) {
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
				request.put(Constants.SCHEME_NAME, data.getSchemeName());
			}

			if (data.getSalaryBudgetAllocated() != null) {
				request.put(Constants.SALARY_BUDGET_ALLOCATED, data.getSalaryBudgetAllocated());
			}
			if (data.getTrainingBudgetAllocated() != null) {
				request.put(Constants.TRAINING_BUDGET_ALLOCATED, data.getTrainingBudgetAllocated());
			}
			if (data.getTrainingBudgetUtilization() != null) {
				request.put(Constants.TRAINING_BUDGET_UTILIZATION, data.getTrainingBudgetUtilization());
			}

			cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_ORG_BUDGET_SCHEME, request, keyMap);

			request.put(Constants.ID, data.getId());
			request.put(Constants.ORG_ID, data.getOrgId());
			request.put(Constants.BUDGET_YEAR, data.getBudgetYear());

			cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_ORG_AUDIT,
					getAuditMap(userId, request, Constants.UPDATE));

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
		keyMap.put(Constants.ORG_ID, orgId);
		keyMap.put(Constants.ID, id);
		keyMap.put(Constants.BUDGET_YEAR, budgetYear);
		try {
			List<Map<String, Object>> existingDetails = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
					Constants.TABLE_ORG_BUDGET_SCHEME, keyMap, null);
			if (!existingDetails.isEmpty()) {
				cassandraOperation.deleteRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_ORG_BUDGET_SCHEME, keyMap);
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

	public SBApiResponse deleteDocBudgetDetails(String orgId, String budgetDetailsId, String budgetYear,
			String proofDocId) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_BUDGET_SCHEME_DELETE);
		try {
			Map<String, Object> propertyMap = new HashMap<>();
			propertyMap.put(Constants.ORG_ID, orgId);
			propertyMap.put(Constants.ID, budgetDetailsId);
			propertyMap.put(Constants.BUDGET_YEAR, budgetYear);
			List<Map<String, Object>> budgetInfo = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
					Constants.TABLE_ORG_BUDGET_SCHEME, propertyMap, new ArrayList<>());

			if (!budgetInfo.isEmpty()) {
				BudgetInfo budgetInfoModel = mapper.convertValue(budgetInfo.get(0), BudgetInfo.class);
				if (CollectionUtils.isEmpty(budgetInfoModel.getProofDocs())) {
					String errMsg = "Failed to find BudgetScheme for OrgId: " + orgId + ", Id: " + budgetDetailsId
							+ ", BudgetYear: " + budgetYear;
					logger.error(errMsg);
					response.getParams().setErrmsg(errMsg);
					response.setResponseCode(HttpStatus.BAD_REQUEST);
				} else {
					boolean docRemoved = false;
					if (budgetInfoModel.getProofDocs() != null) {
						for (Map<String, String> proofDoc : budgetInfoModel.getProofDocs()) {
							if (proofDoc != null && proofDoc.get(Constants.ID).equalsIgnoreCase(proofDocId)) {
								budgetInfoModel.getProofDocs().remove(proofDoc);
								docRemoved = true;
								break;
							}
						}
					}

					if (!docRemoved) {
						String errMsg = "Budget Proof Doc doesn't exist for ProofDocId: " + proofDocId;
						logger.error(errMsg);
						response.getParams().setErrmsg(errMsg);
						response.setResponseCode(HttpStatus.BAD_REQUEST);
						return response;
					}

					// Update the removed list
					Map<String, Object> budgetMap = mapper.convertValue(budgetInfoModel, HashMap.class);
					cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_ORG_BUDGET_SCHEME, budgetMap);

					response.getParams().setStatus(Constants.SUCCESSFUL);
					response.setResponseCode(HttpStatus.OK);
				}
			} else {
				String errMsg = "Failed to find BudgetScheme for OrgId: " + orgId + ", Id: " + budgetDetailsId
						+ ", BudgetYear: " + budgetYear;
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

		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put(Constants.ORG_ID, orgId);
		keyMap.put(Constants.AUDIT_TYPE, Constants.BUDGET);
		List<Map<String, Object>> auditData = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
				Constants.TABLE_ORG_AUDIT, keyMap, null);
		if (CollectionUtils.isEmpty(auditData)) {
			String errMsg = "Budget Scheme History details not found for Org: " + orgId;
			logger.info(errMsg);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			return response;
		}

		List<BudgetAuditInfo> auditResponse = new ArrayList<>();
		for (Map<String, Object> audit : auditData) {
			if (audit.get(Constants.TRANSACTION_DETAILS) != null) {
				Map<String, Object> transactionDetails = mapper.readValue(
						(String) audit.get(Constants.TRANSACTION_DETAILS),
						new TypeReference<HashMap<String, Object>>() {
						});
				audit.put(Constants.TRAINING_BUDGET_ALLOCATED,
						transactionDetails.get(Constants.TRAINING_BUDGET_ALLOCATED));
				audit.put(Constants.TRAINING_BUDGET_UTILIZATION,
						transactionDetails.get(Constants.TRAINING_BUDGET_UTILIZATION));
				audit.put(Constants.SALARY_BUDGET_ALLOCATED, transactionDetails.get(Constants.SALARY_BUDGET_ALLOCATED));
				audit.put(Constants.ID, transactionDetails.get(Constants.ID));
				audit.put(Constants.PROOF_DOCS, transactionDetails.get(Constants.PROOF_DOCS));
				audit.put(Constants.BUDGET_YEAR, transactionDetails.get(Constants.BUDGET_YEAR));
				audit.put(Constants.SCHEME_NAME, transactionDetails.get(Constants.SCHEME_NAME));
				audit.remove(Constants.TRANSACTION_DETAILS);
			}

			BudgetAuditInfo bAuditInfo = new BudgetAuditInfo();
			bAuditInfo.setCreatedBy((String) audit.get(Constants.CREATED_BY));
			bAuditInfo.setCreatedDate((String) audit.get(Constants.CREATED_DATE));
			bAuditInfo.setUpdatedBy((String) audit.get(Constants.UPDATED_BY));
			bAuditInfo.setUpdatedDate((String) audit.get(Constants.UPDATED_DATE));
			auditResponse.add(bAuditInfo);
		}
		response.getParams().setStatus(Constants.SUCCESSFUL);
		response.setResponseCode(HttpStatus.OK);
		response.put(Constants.DATA, auditData);
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

	// private List<Object> getAllBudgetYearDetails(String orgId) {
	// List<BudgetInfoModel> budgetDetails =
	// budgetRepository.getDistinctBudgetYear();
	// if (CollectionUtils.isEmpty(budgetDetails)) {
	// return Collections.emptyList();
	// }
	// List<Object> budgetResponse = new ArrayList<>();
	// for (BudgetInfoModel budget : budgetDetails) {
	// if (budget.getPrimaryKey().getOrgId().equals(orgId)) {
	// budgetResponse.add(budget.getPrimaryKey().getBudgetYear());
	// }
	// }
	// return budgetResponse;
	// }

	private List<Map<String, Object>> getSpecificBudgetYearDetails(String orgId, String budgetYear) {
		Map<String, Object> propertyMap = new HashMap<>();
		propertyMap.put(Constants.ORG_ID, orgId);
		propertyMap.put(Constants.BUDGET_YEAR, budgetYear);
		List<Map<String, Object>> details = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
				Constants.TABLE_ORG_BUDGET_SCHEME, propertyMap, null);
		if (CollectionUtils.isEmpty(details)) {
			return Collections.emptyList();
		}
		List<Map<String, Object>> response = new ArrayList<>();
		for (Map<String, Object> budget : details) {
			response.add(budget);
		}
		return response;
	}

	private Map<String, Object> getAuditMap(String userId, Map<String, Object> data, String operation)
			throws JsonProcessingException {
		DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");
		List<Map<String, Object>> transactionDetails = new ArrayList<>();
		transactionDetails.add(data);
		Map<String, Object> auditMap = new HashMap<>();
		auditMap.put(Constants.ORG_ID, data.get(Constants.ORG_ID));
		auditMap.put(Constants.AUDIT_TYPE, Constants.BUDGET);
		if (operation.equalsIgnoreCase("Create")) {
			auditMap.put(Constants.CREATED_DATE, dateFormat.format(new Date()));
			auditMap.put(Constants.CREATED_BY, userId);
			auditMap.put(Constants.UPDATED_DATE, StringUtils.EMPTY);
			auditMap.put(Constants.UPDATED_BY, StringUtils.EMPTY);
		}
		if (operation.equalsIgnoreCase("Update")) {
			auditMap.put(Constants.CREATED_DATE, StringUtils.EMPTY);
			auditMap.put(Constants.CREATED_BY, StringUtils.EMPTY);
			auditMap.put(Constants.UPDATED_DATE, dateFormat.format(new Date()));
			auditMap.put(Constants.UPDATED_BY, userId);
		}
		auditMap.put(Constants.TRANSACTION_DETAILS, mapper.writeValueAsString(transactionDetails));
		return auditMap;
	}

	private void validateAddBudgetDocInfo(BudgetDocInfo budgetInfo) throws Exception {
		List<String> errObjList = new ArrayList<String>();
		if (StringUtils.isEmpty(budgetInfo.getOrgId())) {
			errObjList.add(Constants.ORG_ID);
		}
		if (StringUtils.isEmpty(budgetInfo.getBudgetYear())) {
			errObjList.add(Constants.BUDGET_YEAR);
		}
		if (StringUtils.isEmpty(budgetInfo.getId())) {
			errObjList.add(Constants.SCHEME_ID);
		}
		if (StringUtils.isEmpty(budgetInfo.getFileName())) {
			errObjList.add(Constants.BUDGET_DOC_FILE_NAME);
		}
		if (StringUtils.isEmpty(budgetInfo.getFileType())) {
			errObjList.add(Constants.BUDGET_DOC_FILE_TYPE);
		}
		if (StringUtils.isEmpty(budgetInfo.getFileSize())) {
			errObjList.add(Constants.BUDGET_DOC_FILE_SIZE);
		}
		if (StringUtils.isEmpty(budgetInfo.getFileUrl())) {
			errObjList.add(Constants.BUDGET_DOC_FILE_URL);
		}

		if (!CollectionUtils.isEmpty(errObjList)) {
			throw new Exception("One or more required fields are empty. Empty fields " + errObjList.toString());
		}
	}
}

package org.sunbird.budget.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
import org.sunbird.budget.model.BudgetInfoModel;
import org.sunbird.budget.model.BudgetInfoPrimaryKeyModel;
import org.sunbird.budget.repo.BudgetRepository;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BudgetServiceImpl implements BudgetService {
	private ObjectMapper mapper = new ObjectMapper();
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(Constants.DATE_FORMAT);
	private Logger logger = LoggerFactory.getLogger(getClass().getName());

	@Autowired
	private BudgetRepository budgetRepository;

	@Autowired
	private AuditRepository auditRepository;

	@Override
	public SBApiResponse submitBudgetDetails(BudgetInfo data, String userId) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_BUDGET_SCHEME_ADD);
		try {
			validateAddBudgetInfo(data);
			List<BudgetInfoModel> existingList = budgetRepository.getAllByOrgIdAndBudgetYearAndSchemeName(
					data.getOrgId(), data.getBudgetYear(), data.getSchemeName());
			if (!CollectionUtils.isEmpty(existingList)) {
				String errMsg = "Budget Scheme exist for given name. Failed to create BudgetInfo for OrgId: "
						+ data.getOrgId() + ", BudgetYear: " + data.getBudgetYear() + ", SchemeName: "
						+ data.getSchemeName();
				logger.error(errMsg);
				response.getParams().setErr(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			}

			BudgetInfoModel budgetInfoModel = new BudgetInfoModel(
					new BudgetInfoPrimaryKeyModel(data.getOrgId(), UUID.randomUUID().toString(), data.getBudgetYear()),
					data.getSchemeName(), data.getSalaryBudgetAllocated(), data.getTrainingBudgetAllocated(),
					data.getTrainingBudgetUtilization());
			budgetInfoModel = budgetRepository.save(budgetInfoModel);

			data.setId(budgetInfoModel.getPrimaryKey().getId());

			Audit audit = new Audit(data.getOrgId(), Constants.BUDGET, dateFormatter.format(new Date()), userId,
					StringUtils.EMPTY, StringUtils.EMPTY, mapper.writeValueAsString(data));
			audit = auditRepository.save(audit);

			response.getParams().setStatus(Constants.SUCCESSFUL);
			response.put(Constants.DATA, budgetInfoModel.getBudgetInfo());
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
		List<Object> budgetResponseList = null;
		String errMsg = null;
		if ("all".equalsIgnoreCase(budgetYear)) {
			budgetResponseList = getAllBudgetYearDetails(orgId);
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
		try {
			validateUpdateBudgetInfo(data);
			Optional<BudgetInfoModel> existingBudgetInfo = budgetRepository
					.findById(new BudgetInfoPrimaryKeyModel(data.getOrgId(), data.getId(), data.getBudgetYear()));

			if (!existingBudgetInfo.isPresent()) {
				String errMsg = "Failed to find BudgetScheme for OrgId: " + data.getOrgId() + ", Id: " + data.getId()
						+ ", BudgetYear: " + data.getBudgetYear();
				logger.error(errMsg);
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			}

			if (data.getSchemeName() != null) {
				// Validate for duplicate schemeNames
				List<BudgetInfoModel> existingList = budgetRepository.getAllByOrgIdAndBudgetYearAndSchemeName(
						data.getOrgId(), data.getBudgetYear(), data.getSchemeName());
				if (!CollectionUtils.isEmpty(existingList)) {
					boolean isOtherRecordExist = false;
					for (BudgetInfoModel bModel : existingList) {
						if (!bModel.getPrimaryKey().getId().equalsIgnoreCase(data.getId())) {
							if (bModel.getSchemeName().equalsIgnoreCase(data.getSchemeName())) {
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
				existingBudgetInfo.get().setSchemeName(data.getSchemeName());
			}

			if (data.getSalaryBudgetAllocated() != null) {
				existingBudgetInfo.get().setSalaryBudgetAllocated(data.getSalaryBudgetAllocated());
			}
			if (data.getTrainingBudgetAllocated() != null) {
				existingBudgetInfo.get().setTrainingBudgetAllocated(data.getTrainingBudgetAllocated());
			}
			if (data.getTrainingBudgetUtilization() != null) {
				existingBudgetInfo.get().setTrainingBudgetUtilization(data.getTrainingBudgetUtilization());
			}
			BudgetInfoModel updatedInfo = budgetRepository.save(existingBudgetInfo.get());

			Audit audit = new Audit(data.getOrgId(), Constants.BUDGET, "", "", dateFormatter.format(new Date()), userId,
					mapper.writeValueAsString(updatedInfo.getBudgetInfo()));
			audit = auditRepository.save(audit);

			response.put(Constants.DATA, updatedInfo.getBudgetInfo());
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
		try {
			Optional<BudgetInfoModel> budgetInfo = budgetRepository
					.findById(new BudgetInfoPrimaryKeyModel(orgId, id, budgetYear));
			if (budgetInfo.isPresent()) {
				budgetRepository.delete(budgetInfo.get());
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
	
	private List<Object> getAllBudgetYearDetails(String orgId) {
		List<BudgetInfoModel> budgetDetails = budgetRepository.getDistinctBudgetYear();
		if (CollectionUtils.isEmpty(budgetDetails)) {
			return Collections.emptyList();
		}
		List<Object> budgetResponse = new ArrayList<>();
		for (BudgetInfoModel budget : budgetDetails) {
			if (budget.getPrimaryKey().getOrgId().equals(orgId)) {
				budgetResponse.add(budget.getPrimaryKey().getBudgetYear());
			}
		}
		return budgetResponse;
	}
	
	private List<Object> getSpecificBudgetYearDetails(String orgId, String budgetYear) {
		List<BudgetInfoModel> budgetDetails = budgetRepository.getAllByOrgIdAndBudgetYear(orgId, budgetYear);
		if (CollectionUtils.isEmpty(budgetDetails)) {
			return Collections.emptyList();
		}
		List<Object> budgetResponse = new ArrayList<>();
		for (BudgetInfoModel budget : budgetDetails) {
			budgetResponse.add(budget.getBudgetInfo());
		}
		return budgetResponse;
	}
}

package org.sunbird.budget.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbird.budget.model.BudgetInfo;
import org.sunbird.budget.model.BudgetInfoModel;
import org.sunbird.budget.repo.BudgetRepository;
import org.sunbird.common.model.Response;
import org.sunbird.common.util.Constants;
import org.sunbird.core.exception.ApplicationLogicError;
import org.sunbird.staff.budget.model.Audit;
import org.sunbird.staff.budget.repo.AuditRepository;
import org.sunbird.staff.model.StaffInfo;
import org.sunbird.staff.model.StaffInfoModel;
import org.sunbird.staff.service.StaffServiceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BudgetServiceImpl implements BudgetService{

public static final String BUDGET = "budget";
	
	ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
    private BudgetRepository budgetRepository;
	
	@Autowired
    private AuditRepository AuditRepository;
	
	private Logger logger = LoggerFactory.getLogger(StaffServiceImpl.class);
	
	@Override
	public Response submitBudgetDetails(BudgetInfo data, String userId) throws Exception {
		Response response = new Response();
		try {  
	        String id = generateUniqueId();
			BudgetInfoModel budgetInfoModel = new BudgetInfoModel(data.getOrgId(), id, data.getBudgetYear(), data.getSchemeName(), 
					data.getSalaryBudgetAllocated(), data.getTrainingBudgetAllocated(), data.getTrainingBudgetUtilization());
			budgetRepository.save(budgetInfoModel);
			
			data.setId(id);

			String date = generateDate();
			
			String jsonData = mapper.writeValueAsString(data);
			
			Audit audit = new Audit(data.getOrgId(), BUDGET, date, userId, "", "", jsonData);
			AuditRepository.save(audit);
			
			response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
		}catch (Exception ex){
            logger.error("Exception occurred while saving the staff details!!", ex);
			response.put(Constants.MESSAGE, Constants.FAILED);
            throw new ApplicationLogicError("Exception occurred while saving the staff details!!", ex);
        }
		
		response.put(Constants.STATUS, HttpStatus.OK);
		return response;
	}

	@Override
	public Response getBudgetDetails(String orgId) throws Exception {
		Response response = new Response();
		List<BudgetInfoModel> budgetDetails = budgetRepository.getBudgetDetails(orgId);
		
		if (CollectionUtils.isEmpty(budgetDetails)) {
          logger.info("There are no data in DB.");
          return response;
		}
		List<BudgetInfo> budgetResponse = new ArrayList<>();
		for(BudgetInfoModel budget : budgetDetails) {
			BudgetInfo info = new BudgetInfo();
			info.setId(budget.getPrimaryKey().getId());
			info.setOrgId(budget.getPrimaryKey().getOrgId());
			info.setBudgetYear(budget.getPrimaryKey().getBudgetYear());
			info.setProofdocs(budget.getProofDocs());
			info.setSalaryBudgetAllocated(budget.getSalaryBudgetAllocated());
			info.setSchemeName(budget.getSchemeName());
			info.setTrainingBudgetAllocated(budget.getTrainingBudgetAllocated());
			info.setTrainingBudgetUtilization(budget.getTrainingBudgetUtilization());
			
			budgetResponse.add(info);
		}
		
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.RESPONSE, budgetResponse);
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
	}

	@Override
	public Response updateBudgetDetails(BudgetInfo data, String userId) throws Exception {
		Response response = new Response();
		try {
			budgetRepository.updateBudgetDetails(data.getTrainingBudgetUtilization(), data.getOrgId(), data.getId(), data.getBudgetYear());
			
			String date = generateDate();
			String jsonData = mapper.writeValueAsString(data);
			
			Audit audit = new Audit(data.getOrgId(), BUDGET, "" , "", date, userId, jsonData);
			AuditRepository.save(audit);
		}catch(Exception ex) {
			logger.error("Exception occurred while updating the budget details!!", ex);
			response.put(Constants.MESSAGE, Constants.FAILED);
            throw new ApplicationLogicError("Exception occurred while updating the budget details!!", ex);
		}
		response.put(Constants.STATUS, HttpStatus.OK);
		return response;
	}

	@Override
	public Response deleteBudgetDetails(String orgId, String id, String budgetYear) throws Exception {
		Response response = new Response();
		
		try {
			budgetRepository.deleteBudgetDetails(orgId, id, budgetYear);
			response.put(Constants.RESPONSE, Constants.SUCCESSFUL);
	        response.put(Constants.STATUS, HttpStatus.OK);
			
		} catch (Exception ex) {
			logger.error("Exception occurred while deleting the budget details!!", ex);
			ex.printStackTrace();
			response.put(Constants.MESSAGE, Constants.FAILED);
            throw new ApplicationLogicError("Exception occurred while deleting the budget details!!", ex);
		}
		return response;
	}
	
	public String generateUniqueId() {
		UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        return uuidAsString;
	}
	
	public String generateDate() {
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss"); 
		String dateStr = dateFormat.format(date);
		return dateStr;
	}

}

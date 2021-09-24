package org.sunbird.staff.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hibernate.mapping.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbird.common.model.Response;
import org.sunbird.common.util.Constants;
import org.sunbird.core.exception.ApplicationLogicError;
import org.sunbird.staff.budget.model.Audit;
import org.sunbird.staff.budget.model.AuditModel;
import org.sunbird.staff.model.StaffInfo;
import org.sunbird.staff.model.StaffInfoModel;
import org.sunbird.staff.budget.repo.AuditRepository;
import org.sunbird.staff.repo.StaffRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StaffServiceImpl implements StaffService{
	
	public static final String STAFF = "staff";
	
	ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
    private StaffRepository staffRepository;
	
	@Autowired
    private AuditRepository auditRepository;
	
	private Logger logger = LoggerFactory.getLogger(StaffServiceImpl.class);
	
	@Override
	public Response submitStaffDetails(StaffInfo data, String userId) throws Exception {
		//validation for position - no multiple position name
		Response response = new Response();
		try {  
	        String id = generateUniqueId();
			StaffInfoModel staffInfoModel = new StaffInfoModel(data.getOrgId(), id, data.getPosition(),
					data.getTotalPositionsFilled(), data.getTotalPositionsVacant());
			staffRepository.save(staffInfoModel);
			
			data.setId(id);
			
//			StaffInfo info = new StaffInfo();
//			info.setId();

			String date = generateDate();
			
//			String json = mapper.writeValueAsString(staffInfoModel);
			String jsonData = mapper.writeValueAsString(data);
			
			Audit audit = new Audit(data.getOrgId(), STAFF, date, userId, "", "", jsonData);
			auditRepository.save(audit);
			
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
	public Response updateStaffDetails(StaffInfo data, String userId) throws Exception {
		Response response = new Response();
		try {
			staffRepository.updateStaffDetails(data.getTotalPositionsFilled(), data.getTotalPositionsVacant(), data.getOrgId(), data.getId());
			
			String date = generateDate();
			String jsonData = mapper.writeValueAsString(data);
			
			Audit audit = new Audit(data.getOrgId(), STAFF, "" , "", date, userId, jsonData);
			auditRepository.save(audit);
		}catch(Exception ex) {
			logger.error("Exception occurred while updating the staff details!!", ex);
			response.put(Constants.MESSAGE, Constants.FAILED);
            throw new ApplicationLogicError("Exception occurred while updating the staff details!!", ex);
		}
		response.put(Constants.STATUS, HttpStatus.OK);
		return response;
	}

	@Override
	public Response getStaffDetails(String orgId) throws Exception {
		Response response = new Response();
		List<StaffInfoModel> staffDetails = staffRepository.getStaffDetails(orgId);
		
		if (CollectionUtils.isEmpty(staffDetails)) {
          logger.info("There are no data in DB.");
          return response;
		}
		List<StaffInfo> staffResponse = new ArrayList<>();
		for(StaffInfoModel staff : staffDetails) {
			StaffInfo info = new StaffInfo();
			info.setId(staff.getPrimaryKey().getId());
			info.setOrgId(staff.getPrimaryKey().getOrgId());
			info.setPosition(staff.getPosition());
			info.setTotalPositionsFilled(staff.getTotalPositionsFilled());
			info.setTotalPositionsVacant(staff.getTotalPositionsVacant());
			staffResponse.add(info);
		}
		
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.RESPONSE, staffResponse);
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
	}

	@Override
	public Response deleteStaffDetails(String orgId, String staffInfoId) throws Exception {
		Response response = new Response();
		
		try {
			staffRepository.deleteStaffDetails(orgId, staffInfoId);
			response.put(Constants.RESPONSE, Constants.SUCCESSFUL);
	        response.put(Constants.STATUS, HttpStatus.OK);
			
		} catch (Exception ex) {
			logger.error("Exception occurred while deleting the staff details!!", ex);
			ex.printStackTrace();
			response.put(Constants.MESSAGE, Constants.FAILED);
            throw new ApplicationLogicError("Exception occurred while deleting the staff details!!", ex);
		}
		return response;
	}
	
	@Override
	public Response getStaffAudit(String orgId, String auditType) throws Exception {
		Response response = new Response();
		List<Audit> auditDetails = auditRepository.getAudit(orgId, auditType);
		
		List<AuditModel> auditResponse = new ArrayList<>();
		JSONParser parser = new JSONParser();
		for(Audit audit : auditDetails) {
			AuditModel info = new AuditModel();
			info.setAuditType(audit.getPrimaryKey().getAuditType());
			info.setOrgId(audit.getPrimaryKey().getOrgId());
			info.setCreatedDate(audit.getPrimaryKey().getCreatedDate());
			info.setCreatedBy(audit.getCreatedBy());
			info.setUpdatedDate(audit.getPrimaryKey().getUpdatedDate());
			info.setUpdatedBy(audit.getUpdatedBy());
			
			  
			JSONObject json = (JSONObject) parser.parse(audit.getTransactionDetails()); 
			info.setTransactionDetails(json);
			
			auditResponse.add(info);
		}
		 
		response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.RESPONSE, auditResponse);
//        response.put(Constants.RESPONSE, auditDetails);
        response.put(Constants.STATUS, HttpStatus.OK);
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

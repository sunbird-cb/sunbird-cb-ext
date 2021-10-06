package org.sunbird.staff.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.staff.model.StaffAuditInfo;
import org.sunbird.staff.model.StaffInfo;
import org.sunbird.staff.model.StaffInfoModel;
import org.sunbird.staff.model.StaffInfoPrimaryKeyModel;
import org.sunbird.staff.repo.StaffRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StaffServiceImpl implements StaffService {
	private ObjectMapper mapper = new ObjectMapper();
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(Constants.DATE_FORMAT);
	private Logger logger = LoggerFactory.getLogger(getClass().getName());

	@Autowired
	private StaffRepository staffRepository;

	@Autowired
	private AuditRepository auditRepository;

	@Override
	public SBApiResponse submitStaffDetails(StaffInfo data, String userId) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_STAFF_POSITION_ADD);
		try {
			List<StaffInfoModel> existingList = staffRepository.getAllByOrgIdAndPosition(data.getOrgId(),
					data.getPosition());

			if (!CollectionUtils.isEmpty(existingList)) {
				String errMsg = "Position exist for given name. Failed to create StaffInfo for OrgId: "
						+ data.getOrgId() + ", Position: " + data.getPosition();
				logger.error(errMsg);
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			}

			StaffInfoModel staffInfoModel = new StaffInfoModel(
					new StaffInfoPrimaryKeyModel(data.getOrgId(), UUID.randomUUID().toString()), data.getPosition(),
					data.getTotalPositionsFilled(), data.getTotalPositionsVacant());
			staffInfoModel = staffRepository.save(staffInfoModel);

			data.setId(staffInfoModel.getPrimaryKey().getId());

			Audit audit = new Audit(data.getOrgId(), Constants.STAFF, dateFormatter.format(new Date()), userId,
					StringUtils.EMPTY, StringUtils.EMPTY, mapper.writeValueAsString(data));
			audit = auditRepository.save(audit);

			response.getParams().setStatus(Constants.SUCCESSFUL);
			response.put(Constants.DATA, staffInfoModel.getStaffInfo());
			response.setResponseCode(HttpStatus.CREATED);
		} catch (Exception ex) {
			String errMsg = "Exception occurred while saving the staff details. Exception: " + ex.getMessage();
			logger.error(errMsg, ex);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return response;
		}

		return response;
	}

	@Override
	public SBApiResponse updateStaffDetails(StaffInfo data, String userId) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_STAFF_POSITION_UPDATE);
		try {
			Optional<StaffInfoModel> existingStaffInfo = staffRepository
					.findById(new StaffInfoPrimaryKeyModel(data.getOrgId(), data.getId()));
			if (!existingStaffInfo.isPresent()) {
				String errMsg = "Failed to find StaffInfo for OrgId: " + data.getOrgId() + ", Id: " + data.getId();
				logger.error(errMsg);
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			}

			List<StaffInfoModel> existingList = staffRepository.getAllByOrgIdAndPosition(data.getOrgId(),
					data.getPosition());

			if (CollectionUtils.isEmpty(existingList)) {
				boolean isAnyOtherRecordExist = false;
				for (StaffInfoModel sModel : existingList) {
					if (!sModel.getPrimaryKey().getId().equalsIgnoreCase(data.getId())) {
						if (sModel.getPosition().equalsIgnoreCase(data.getPosition())) {
							isAnyOtherRecordExist = true;
						}
					}
				}
				if (isAnyOtherRecordExist) {
					// Return error
					String errMsg = "Position exist for given name. Failed to create StaffInfo for OrgId: "
							+ data.getOrgId() + ", Position: " + data.getPosition();
					logger.error(errMsg);
					response.getParams().setErrmsg(errMsg);
					response.setResponseCode(HttpStatus.BAD_REQUEST);
					return response;
				}
			}

			if (data.getPosition() != null) {
				existingStaffInfo.get().setPosition(data.getPosition());
			}
			if (data.getTotalPositionsFilled() != null) {
				existingStaffInfo.get().setTotalPositionsFilled(data.getTotalPositionsFilled());
			}
			if (data.getTotalPositionsVacant() != null) {
				existingStaffInfo.get().setTotalPositionsVacant(data.getTotalPositionsVacant());
			}
			StaffInfoModel updatedInfo = staffRepository.save(existingStaffInfo.get());

			Audit audit = new Audit(data.getOrgId(), Constants.STAFF, "", "", dateFormatter.format(new Date()), userId,
					mapper.writeValueAsString(updatedInfo.getStaffInfo()));
			audit = auditRepository.save(audit);

			response.put(Constants.DATA, updatedInfo.getStaffInfo());
			response.getParams().setStatus(Constants.SUCCESSFUL);
			response.setResponseCode(HttpStatus.OK);
		} catch (Exception ex) {
			String errMsg = "Exception occurred while updating the staff details. Exception: " + ex.getMessage();
			logger.error(errMsg, ex);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public SBApiResponse getStaffDetails(String orgId) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_STAFF_POSITION_READ);
		List<StaffInfoModel> staffDetails = staffRepository.getStaffDetails(orgId);

		if (CollectionUtils.isEmpty(staffDetails)) {
			String errMsg = "No Staff Position found for Org: " + orgId;
			logger.info(errMsg);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.NOT_FOUND);
			return response;
		}
		List<StaffInfo> staffResponse = new ArrayList<>();
		for (StaffInfoModel staff : staffDetails) {
			StaffInfo info = new StaffInfo();
			info.setId(staff.getPrimaryKey().getId());
			info.setOrgId(staff.getPrimaryKey().getOrgId());
			info.setPosition(staff.getPosition());
			info.setTotalPositionsFilled(staff.getTotalPositionsFilled());
			info.setTotalPositionsVacant(staff.getTotalPositionsVacant());
			staffResponse.add(info);
		}

		response.getParams().setStatus(Constants.SUCCESSFUL);
		response.put(Constants.RESPONSE, staffResponse);
		response.setResponseCode(HttpStatus.OK);
		return response;
	}

	@Override
	public SBApiResponse deleteStaffDetails(String orgId, String staffInfoId) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_STAFF_POSITION_DELETE);
		try {
			Optional<StaffInfoModel> staffInfo = staffRepository
					.findById(new StaffInfoPrimaryKeyModel(orgId, staffInfoId));

			if (staffInfo.isPresent()) {
				staffRepository.delete(staffInfo.get());
				response.getParams().setStatus(Constants.SUCCESSFUL);
				response.setResponseCode(HttpStatus.OK);
			} else {
				String errMsg = "Staff Position doesn't exist for OrgId: " + orgId + ", Position: " + staffInfoId;
				logger.error(errMsg);
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			String errMsg = "Exception occurred while deleting the staff details. Exception: " + ex.getMessage();
			logger.error(errMsg, ex);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public SBApiResponse getStaffAudit(String orgId, String auditType) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_STAFF_POSITION_HISTORY_READ);
		List<Audit> auditDetails = auditRepository.getAudit(orgId, auditType);
		if(CollectionUtils.isEmpty(auditDetails)) {
			String errMsg = "Staff Position History details not found for Org: " + orgId;
			logger.info(errMsg);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.NOT_FOUND);
			return response;
		}
		List<StaffAuditInfo> auditResponse = new ArrayList<>();
		for (Audit audit : auditDetails) {
			StaffAuditInfo sAuditInfo = new StaffAuditInfo();
			sAuditInfo.setCreatedBy(audit.getCreatedBy());
			sAuditInfo.setCreatedDate(audit.getPrimaryKey().getCreatedDate());
			sAuditInfo.setUpdatedBy(audit.getUpdatedBy());
			sAuditInfo.setUpdatedDate(audit.getPrimaryKey().getUpdatedDate());
			sAuditInfo.setOrgId(audit.getPrimaryKey().getOrgId());
			StaffInfo sInfo = mapper.readValue(audit.getTransactionDetails(), StaffInfo.class);
			sAuditInfo.setPosition(sInfo.getPosition());
			sAuditInfo.setTotalPositionsFilled(sInfo.getTotalPositionsFilled());
			sAuditInfo.setTotalPositionsVacant(sInfo.getTotalPositionsVacant());
			sAuditInfo.setId(sInfo.getId());
			auditResponse.add(sAuditInfo);
		}
		response.getParams().setStatus(Constants.SUCCESSFUL);
		response.setResponseCode(HttpStatus.OK);
		response.put(Constants.DATA, auditResponse);
		return response;
	}
}

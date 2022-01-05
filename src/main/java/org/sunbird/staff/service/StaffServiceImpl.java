package org.sunbird.staff.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.exception.MyOwnRuntimeException;
import org.sunbird.staff.model.StaffAuditInfo;
import org.sunbird.staff.model.StaffInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StaffServiceImpl implements StaffService {
	private static final String ONE_OR_MORE_REQUIRED_FIELDS_ARE_EMPTY_EMPTY_FIELDS = "One or more required fields are empty. Empty fields ";
	private static final String POSITION = ", Position: ";
	private ObjectMapper mapper = new ObjectMapper();
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(Constants.DATE_FORMAT);
	private Logger logger = LoggerFactory.getLogger(getClass().getName());

	@Autowired
	private CassandraOperation cassandraOperation;

	@Override
	public SBApiResponse deleteStaffDetails(String orgId, String staffInfoId) {
		SBApiResponse response = new SBApiResponse(Constants.API_STAFF_POSITION_DELETE);
		try {
			List<StaffInfo> existingStaffList = getStaffListByProperty(orgId, staffInfoId, StringUtils.EMPTY);

			if (!existingStaffList.isEmpty()) {
				Map<String, Object> primaryKeyMap = new HashMap<>();
				primaryKeyMap.put(Constants.ORG_ID, orgId);
				primaryKeyMap.put(Constants.ID, staffInfoId);
				cassandraOperation.deleteRecord(Constants.DATABASE, Constants.STAFF_TABLE, primaryKeyMap);
				response.getParams().setStatus(Constants.SUCCESSFUL);
				response.setResponseCode(HttpStatus.OK);
			} else {
				String errMsg = "Staff Position doesn't exist for OrgId: " + orgId + POSITION + staffInfoId;
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
	public SBApiResponse getStaffAudit(String orgId) throws IOException {
		SBApiResponse response = new SBApiResponse(Constants.API_STAFF_POSITION_HISTORY_READ);

		Map<String, Object> propertyMap = new HashMap<>();
		propertyMap.put(Constants.ORG_ID, orgId);
		propertyMap.put(Constants.AUDIT_TYPE, Constants.STAFF);
		List<Map<String, Object>> auditDetails = cassandraOperation.getRecordsByProperties(Constants.DATABASE,
				Constants.AUDIT_TABLE, propertyMap, new ArrayList<>());

		if (CollectionUtils.isEmpty(auditDetails)) {
			String errMsg = "Staff Position History details not found for Org: " + orgId;
			logger.info(errMsg);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			return response;
		}
		List<StaffAuditInfo> auditResponse = new ArrayList<>();
		for (Map<String, Object> audit : auditDetails) {
			StaffAuditInfo sAuditInfo = mapper.readValue((String) audit.get(Constants.TRANSACTION_DETAILS),
					StaffAuditInfo.class);
			sAuditInfo.setCreatedBy((String) audit.get(Constants.CREATED_BY));
			sAuditInfo.setCreatedDate((String) audit.get(Constants.CREATED_DATE));
			sAuditInfo.setUpdatedBy((String) audit.get(Constants.UPDATED_BY));
			sAuditInfo.setUpdatedDate((String) audit.get(Constants.UPDATED_DATE));
			auditResponse.add(sAuditInfo);
		}
		response.getParams().setStatus(Constants.SUCCESSFUL);
		response.setResponseCode(HttpStatus.OK);
		response.put(Constants.DATA, auditResponse);
		return response;
	}

	@Override
	public SBApiResponse getStaffDetails(String orgId) {
		SBApiResponse response = new SBApiResponse(Constants.API_STAFF_POSITION_READ);
		List<StaffInfo> staffDetails = getStaffListByProperty(orgId, StringUtils.EMPTY, StringUtils.EMPTY);

		if (CollectionUtils.isEmpty(staffDetails)) {
			String errMsg = "No Staff Position found for Org: " + orgId;
			logger.info(errMsg);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			return response;
		}

		response.getParams().setStatus(Constants.SUCCESSFUL);
		response.put(Constants.RESPONSE, staffDetails);
		response.setResponseCode(HttpStatus.OK);
		return response;
	}

	/**
	 * Returns the list staff objects from database that matches the property
	 * conditions
	 *
	 * @param orgId    String
	 * @param id       String
	 * @param position String
	 * @return List<Map<String, Object>>
	 */
	private List<StaffInfo> getStaffListByProperty(String orgId, String id, String position) {
		Map<String, Object> propertyMap = new HashMap<>();
		if (StringUtils.isNotBlank(orgId)) {
			propertyMap.put(Constants.ORG_ID, orgId);
		}
		if (StringUtils.isNotBlank(id)) {
			propertyMap.put(Constants.ID, id);
		}
		if (StringUtils.isNotBlank(position)) {
			propertyMap.put(Constants.POSITION, position);
		}
		List<Map<String, Object>> staffLists = cassandraOperation.getRecordsByProperties(Constants.DATABASE,
				Constants.STAFF_TABLE, propertyMap, new ArrayList<>());
		// convert to map to pojo object
		List<StaffInfo> staffInfo = new ArrayList<>();
		for (Map<String, Object> mapObj : staffLists) {
			staffInfo.add(mapper.convertValue(mapObj, StaffInfo.class));
		}
		return staffInfo;
	}

	/**
	 * Saves the staff update in the audit table
	 *
	 * @param data   StaffInfo
	 * @param userId String
	 * @throws JsonProcessingException
	 * @throws Exception
	 */
	private void saveAuditDetails(StaffInfo data, String userId) throws JsonProcessingException {
		Map<String, Object> auditMap = new HashMap<>();
		auditMap.put(Constants.ORG_ID, data.getOrgId());
		auditMap.put(Constants.AUDIT_TYPE, Constants.STAFF);
		auditMap.put(Constants.CREATED_DATE, dateFormatter.format(new Date()));
		auditMap.put(Constants.CREATED_BY, userId);
		auditMap.put(Constants.UPDATED_DATE, StringUtils.EMPTY);
		auditMap.put(Constants.UPDATED_BY, StringUtils.EMPTY);
		auditMap.put(Constants.TRANSACTION_DETAILS, mapper.writeValueAsString(data));
		cassandraOperation.insertRecord(Constants.DATABASE, Constants.AUDIT_TABLE, auditMap);
	}

	@Override
	public SBApiResponse submitStaffDetails(StaffInfo data, String userId) {
		SBApiResponse response = new SBApiResponse(Constants.API_STAFF_POSITION_ADD);
		try {
			validateAddStaffInfo(data);

			List<StaffInfo> existingList = getStaffListByProperty(data.getOrgId(), StringUtils.EMPTY,
					data.getPosition());
			if (!CollectionUtils.isEmpty(existingList)) {
				String errMsg = "Position exist for given name. Failed to create StaffInfo for OrgId: "
						+ data.getOrgId() + POSITION + data.getPosition();
				logger.error(errMsg);
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			}

			Map<String, Object> staffInfoMap = new HashMap<>();
			staffInfoMap.put(Constants.ORG_ID, data.getOrgId());
			staffInfoMap.put(Constants.ID, UUID.randomUUID().toString());
			staffInfoMap.put(Constants.POSITION, data.getPosition());
			staffInfoMap.put(Constants.TOTAL_POSITION_FILLED, data.getTotalPositionsFilled());
			staffInfoMap.put(Constants.TOTAL_POSITION_VACANT, data.getTotalPositionsVacant());

			cassandraOperation.insertRecord(Constants.DATABASE, Constants.STAFF_TABLE, staffInfoMap);
			data.setId((String) staffInfoMap.get(Constants.ID));

			saveAuditDetails(data, userId);

			response.getParams().setStatus(Constants.SUCCESSFUL);
			response.put(Constants.DATA, staffInfoMap);
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
	public SBApiResponse updateStaffDetails(StaffInfo data, String userId) {
		SBApiResponse response = new SBApiResponse(Constants.API_STAFF_POSITION_UPDATE);
		try {
			validateUpdateStaffInfo(data);

			List<StaffInfo> existingStaffList = getStaffListByProperty(data.getOrgId(), data.getId(),
					StringUtils.EMPTY);

			StaffInfo existingStaffInfo;
			if (!existingStaffList.isEmpty()) {
				existingStaffInfo = existingStaffList.get(0);
			} else {
				String errMsg = "Failed to find StaffInfo for OrgId: " + data.getOrgId() + ", Id: " + data.getId();
				logger.error(errMsg);
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			}

			if (data.getPosition() != null) {
				List<StaffInfo> existingList = getStaffListByProperty(data.getOrgId(), StringUtils.EMPTY,
						data.getPosition());

				if (!CollectionUtils.isEmpty(existingList)) {
					boolean isAnyOtherRecordExist = false;
					for (StaffInfo sModel : existingList) {
						if (!sModel.getId().equalsIgnoreCase(data.getId())
								&& (sModel.getPosition().equalsIgnoreCase(data.getPosition()))) {
							isAnyOtherRecordExist = true;
						}
					}
					if (isAnyOtherRecordExist) {
						// Return error
						String errMsg = "Position exist for given name. Failed to update StaffInfo for OrgId: "
								+ data.getOrgId() + POSITION + data.getPosition();
						logger.error(errMsg);
						response.getParams().setErrmsg(errMsg);
						response.setResponseCode(HttpStatus.BAD_REQUEST);
						return response;
					}
				}
				existingStaffInfo.setPosition(data.getPosition());
			}

			if (data.getTotalPositionsFilled() != null) {
				existingStaffInfo.setTotalPositionsFilled(data.getTotalPositionsFilled());
			}
			if (data.getTotalPositionsVacant() != null) {
				existingStaffInfo.setTotalPositionsVacant(data.getTotalPositionsVacant());
			}
			cassandraOperation.insertRecord(Constants.DATABASE, Constants.STAFF_TABLE,
					mapper.convertValue(existingStaffInfo, HashMap.class));

			saveAuditDetails(data, userId);

			response.put(Constants.DATA, existingStaffInfo);
			response.getParams().setStatus(Constants.SUCCESSFUL);
			response.setResponseCode(HttpStatus.OK);
		} catch (

		Exception ex) {
			String errMsg = "Exception occurred while updating the staff details. Exception: " + ex.getMessage();
			logger.error(errMsg, ex);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	private void validateAddStaffInfo(StaffInfo staffInfo) throws MyOwnRuntimeException {
		List<String> errObjList = new ArrayList<>();
		if (StringUtils.isEmpty(staffInfo.getOrgId())) {
			errObjList.add(Constants.ORG_ID);
		}
		if (StringUtils.isEmpty(staffInfo.getPosition())) {
			errObjList.add(Constants.POSITION);
		}
		if (staffInfo.getTotalPositionsFilled() == null || staffInfo.getTotalPositionsFilled() < 0) {
			errObjList.add(Constants.TOTAL_POSITION_FILLED);
		}
		if (staffInfo.getTotalPositionsVacant() == null || staffInfo.getTotalPositionsVacant() < 0) {
			errObjList.add(Constants.TOTAL_POSITION_VACANT);
		}

		if (!CollectionUtils.isEmpty(errObjList)) {
			throw new MyOwnRuntimeException(ONE_OR_MORE_REQUIRED_FIELDS_ARE_EMPTY_EMPTY_FIELDS + errObjList.toString());
		}
	}

	private void validateUpdateStaffInfo(StaffInfo staffInfo) throws MyOwnRuntimeException {
		List<String> errObjList = new ArrayList<>();
		if (StringUtils.isEmpty(staffInfo.getOrgId())) {
			errObjList.add(Constants.ORG_ID);
		}
		if (StringUtils.isEmpty(staffInfo.getId())) {
			errObjList.add(Constants.ID);
		}
		boolean position = false;
		boolean filled = false;
		boolean vacant = false;
		if (staffInfo.getPosition() != null) {
			if (StringUtils.isEmpty(staffInfo.getPosition())) {
				errObjList.add(Constants.POSITION);
			}
		} else {
			errObjList.add(Constants.POSITION);
			position = true;
		}
		if (staffInfo.getTotalPositionsFilled() != null) {
			if (staffInfo.getTotalPositionsFilled() < 0) {
				errObjList.add(Constants.TOTAL_POSITION_FILLED);
			}
		} else {
			errObjList.add(Constants.TOTAL_POSITION_FILLED);
			filled = true;
		}

		if (staffInfo.getTotalPositionsVacant() != null) {
			if (staffInfo.getTotalPositionsVacant() < 0) {
				errObjList.add(Constants.TOTAL_POSITION_VACANT);
			}
		} else {
			errObjList.add(Constants.TOTAL_POSITION_VACANT);
			vacant = true;
		}

		if (position && filled && vacant) {
			throw new MyOwnRuntimeException(ONE_OR_MORE_REQUIRED_FIELDS_ARE_EMPTY_EMPTY_FIELDS + errObjList.toString());
		} else {
			if (position) {
				errObjList.remove(Constants.POSITION);
			}
			if (filled) {
				errObjList.remove(Constants.TOTAL_POSITION_FILLED);
			}
			if (vacant) {
				errObjList.remove(Constants.TOTAL_POSITION_VACANT);
			}
		}

		if (!CollectionUtils.isEmpty(errObjList)) {
			throw new MyOwnRuntimeException(ONE_OR_MORE_REQUIRED_FIELDS_ARE_EMPTY_EMPTY_FIELDS + errObjList.toString());
		}
	}
}

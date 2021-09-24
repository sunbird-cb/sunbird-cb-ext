package org.sunbird.staff.service;

import org.sunbird.staff.model.StaffInfo;
import org.sunbird.common.model.Response;

public interface StaffService {
	
	public Response submitStaffDetails(StaffInfo data, String userId) throws Exception;
	
	public Response getStaffDetails(String orgId) throws Exception;

	public Response updateStaffDetails(StaffInfo data, String userId) throws Exception;

	public Response deleteStaffDetails(String orgId, String staffDetailsId) throws Exception;
	
	public Response getStaffAudit(String orgId, String auditType)throws Exception;

}

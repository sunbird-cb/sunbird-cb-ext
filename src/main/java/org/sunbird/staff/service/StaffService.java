package org.sunbird.staff.service;

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.staff.model.StaffInfo;

public interface StaffService {
	
	public SBApiResponse submitStaffDetails(StaffInfo data, String userId) throws Exception;
	
	public SBApiResponse getStaffDetails(String orgId) throws Exception;

	public SBApiResponse updateStaffDetails(StaffInfo data, String userId) throws Exception;

	public SBApiResponse deleteStaffDetails(String orgId, String staffDetailsId) throws Exception;
	
	public SBApiResponse getStaffAudit(String orgId)throws Exception;

}

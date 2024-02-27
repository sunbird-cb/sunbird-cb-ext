package org.sunbird.staff.service;

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.staff.model.StaffInfo;

public interface StaffService {
	
	public SBApiResponse submitStaffDetails(StaffInfo data, String userId);
	
	public SBApiResponse getStaffDetails(String orgId);

	public SBApiResponse updateStaffDetails(StaffInfo data, String userId);

	public SBApiResponse deleteStaffDetails(String orgId, String staffDetailsId);
	
	public SBApiResponse getStaffAudit(String orgId)throws Exception;

}

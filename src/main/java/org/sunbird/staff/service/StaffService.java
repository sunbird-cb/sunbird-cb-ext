package org.sunbird.staff.service;

import java.io.IOException;

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.staff.model.StaffInfo;

public interface StaffService {

	public SBApiResponse deleteStaffDetails(String orgId, String staffDetailsId);

	public SBApiResponse getStaffAudit(String orgId) throws IOException;

	public SBApiResponse getStaffDetails(String orgId);

	public SBApiResponse submitStaffDetails(StaffInfo data, String userId);

	public SBApiResponse updateStaffDetails(StaffInfo data, String userId);

}

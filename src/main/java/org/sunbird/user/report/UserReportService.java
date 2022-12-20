package org.sunbird.user.report;

import java.util.List;
import java.util.Map;

import org.sunbird.common.model.SBApiResponse;

public interface UserReportService {

	public void generateUserEnrolmentReport(Map<String, Map<String, String>> userEnrolmentMap, List<String> fields,
			SBApiResponse response);

	public void generateUserReport(Map<String, Map<String, String>> userInfoMap, List<String> fields,
			SBApiResponse response);
}

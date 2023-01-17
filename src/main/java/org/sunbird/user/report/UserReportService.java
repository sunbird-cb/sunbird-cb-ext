package org.sunbird.user.report;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.sunbird.common.model.SBApiResponse;

public interface UserReportService {

	public void generateUserEnrolmentReport(Map<String, Map<String, String>> userEnrolmentMap, List<String> fields,
			SBApiResponse response);

	public Workbook createReportWorkbook(List<String> fields);

	public Workbook appendData(Workbook wb, List<String> fields, Map<String, Map<String, String>> userInfoMap);

	public void completeReportWorkbook(Workbook wb, SBApiResponse response);

}

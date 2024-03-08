package org.sunbird.operationalreports.service;

/**
 * @author Deepak kumar Thakur & Mahesh R V
 */

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;

import java.io.IOException;

public interface OperationalReportService {
    public SBApiResponse grantReportAccessToMDOAdmin(SunbirdApiRequest request, String userOrgId, String authToken);

    public ResponseEntity<InputStreamResource> downloadFile(String reportType, String date, String orgId, String fileName) throws IOException;

    SBApiResponse getFileInfo(String authToken);
}
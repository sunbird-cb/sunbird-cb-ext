package org.sunbird.operationalreports.service;

import java.util.Map;

/**
 * @author Deepak kumar Thakur & Mahesh R V
 */

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.sunbird.common.model.SBApiResponse;

public interface OperationalReportService {
    public SBApiResponse grantReportAccessToMDOAdmin(Map<String, Object> request, String authToken);

    public ResponseEntity<InputStreamResource> downloadFile(String authToken) throws Exception;

    public SBApiResponse getFileInfo(String authToken);

    public SBApiResponse readGrantAccess(String authToken, boolean isAdminAPI);
}
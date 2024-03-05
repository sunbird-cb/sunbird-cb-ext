package org.sunbird.operationalreports.controller;

/**
 * @author Deepak kumar Thakur & Mahesh R V
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.util.Constants;
import org.sunbird.operationalreports.service.OperationalReportService;

import java.io.IOException;


@RestController
@RequestMapping("/operational/reports")
public class OperationalReportController {
    @Autowired
    private OperationalReportService operationalReport;

    @PostMapping("admin/grantaccess")
    public ResponseEntity<SBApiResponse> grantAccess
            (@RequestBody SunbirdApiRequest request,
             @RequestHeader(Constants.X_AUTH_TOKEN) String authToken,
             @RequestHeader(Constants.X_AUTH_USER_ORG_ID) String userOrgId) throws Exception {
        SBApiResponse response = operationalReport.grantReportAccessToMDOAdmin(request,userOrgId, authToken);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/download/{reportType}/{date}/{orgId}/{fileName}")
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable String reportType,
            @PathVariable String date,
            @PathVariable String orgId,
            @PathVariable String fileName) throws IOException {
        return operationalReport.downloadFile(reportType, date,orgId,fileName);
    }
}

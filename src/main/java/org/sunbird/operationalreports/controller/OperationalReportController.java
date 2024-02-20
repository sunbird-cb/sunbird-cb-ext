package org.sunbird.operationalreports.controller;

/**
 * @author Deepak kumar Thakur
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.sunbird.common.model.SBApiResponse;
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
            (@RequestHeader(Constants.X_AUTH_TOKEN) String authToken,
             @RequestHeader(Constants.X_AUTH_USER_ORG_ID) String userOrgId) throws Exception {
        SBApiResponse response = operationalReport.grantAccessToMDOAdmin(userOrgId, authToken);
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

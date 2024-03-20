package org.sunbird.operationalreports.controller;

import java.util.Map;

/**
 * @author Deepak kumar Thakur & Mahesh R V
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.operationalreports.service.OperationalReportService;

@RestController
@RequestMapping("/operational/reports")
public class OperationalReportController {
    @Autowired
    private OperationalReportService operationalReport;

    @PostMapping("/admin/grantaccess")
    public ResponseEntity<SBApiResponse> grantAccess(@RequestBody Map<String, Object> request,
            @RequestHeader(Constants.X_AUTH_TOKEN) String authToken) {
        SBApiResponse response = operationalReport.grantReportAccessToMDOAdmin(request, authToken);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @RequestHeader(Constants.X_AUTH_TOKEN) String authToken) throws Exception {
        return operationalReport.downloadFile(authToken);
    }

    @GetMapping("/v1/reportInfo")
    public SBApiResponse getFileInfo(
            @RequestHeader(Constants.X_AUTH_TOKEN) String authToken) {
        return operationalReport.getFileInfo(authToken);
    }

    @GetMapping("/admin/readaccess")
    public ResponseEntity<SBApiResponse> readAccess(
            @RequestHeader(Constants.X_AUTH_TOKEN) String authToken) throws Exception {
        SBApiResponse response = operationalReport.readGrantAccess(authToken, true);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/leader/readaccess")
    public ResponseEntity<SBApiResponse> mdoLeaderReadAccess(
            @RequestHeader(Constants.X_AUTH_TOKEN) String authToken) throws Exception {
        SBApiResponse response = operationalReport.readGrantAccess(authToken, false);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}

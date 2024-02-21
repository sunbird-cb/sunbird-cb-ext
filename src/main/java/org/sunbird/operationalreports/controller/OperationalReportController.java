package org.sunbird.operationalreports.controller;

/**
 * @author Deepak kumar Thakur
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.util.Constants;
import org.sunbird.operationalreports.service.OperationalReportService;


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

}

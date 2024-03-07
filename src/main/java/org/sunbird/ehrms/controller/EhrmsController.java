package org.sunbird.ehrms.controller;

/**
 * @author Deepak kumar Thakur
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.ehrms.service.EhrmsService;


@RestController
@RequestMapping("/ehrms")
public class EhrmsController {

    private final  EhrmsService ehrmsService;

    @Autowired
    public EhrmsController(EhrmsService ehrmsService) {
        this.ehrmsService = ehrmsService;
    }

    @GetMapping("/details")
    public ResponseEntity <SBApiResponse> fetchEhrmsProfileDetail
            (@RequestHeader(Constants.X_AUTH_TOKEN) String authToken,
             @RequestHeader(Constants.X_AUTH_USER_ORG_ID) String rootOrgId) throws Exception {
        SBApiResponse response = ehrmsService.fetchEhrmsProfileDetail(rootOrgId, authToken);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

}

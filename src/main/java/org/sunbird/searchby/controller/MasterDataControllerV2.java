package org.sunbird.searchby.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.searchby.service.MasterDataServiceImpl;

@RestController
@RequestMapping("/masterData/v2")
public class MasterDataControllerV2 {
    @Autowired
    private MasterDataServiceImpl masterDataService;

    @GetMapping("/deptPosition")
    public ResponseEntity<?> getDeptPositionsList(@RequestHeader(Constants.X_AUTH_USER_ORG_ID) String userOrgId) {
        SBApiResponse response = masterDataService.getDeptPositions(userOrgId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}

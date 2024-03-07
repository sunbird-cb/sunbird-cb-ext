package org.sunbird.cbp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.cbp.service.CbPlanService;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.util.Constants;

import java.util.List;

@RestController
@RequestMapping("cbplan")
public class CbPlanController {

    private CbPlanService cbPlanService;
    @Autowired
    public CbPlanController(CbPlanService cbPlanService) {
        this.cbPlanService = cbPlanService;
    }


    @PostMapping("/v1/create")
    public ResponseEntity<SBApiResponse> createCbPlan(
            @RequestBody SunbirdApiRequest request,
            @RequestHeader(Constants.X_AUTH_TOKEN) String token,
            @RequestHeader(Constants.X_AUTH_USER_ORG_ID) String userOrgId) {

        SBApiResponse response = cbPlanService.createCbPlan(request, userOrgId, token);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/v1/update")
    public ResponseEntity<SBApiResponse> updateCbPlan(
            @RequestBody SunbirdApiRequest request,
            @RequestHeader(Constants.X_AUTH_TOKEN) String token,
            @RequestHeader(Constants.X_AUTH_USER_ORG_ID) String userOrgId,
            @RequestHeader(Constants.X_AUTH_USER_ROLES) List<String> userRoles) {

        SBApiResponse response = cbPlanService.updateCbPlan(request, userOrgId, token, userRoles);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/v1/publish")
    public ResponseEntity<SBApiResponse> publishCbPlan(
            @RequestBody SunbirdApiRequest request,
            @RequestHeader(Constants.X_AUTH_TOKEN) String token,
            @RequestHeader(Constants.X_AUTH_USER_ORG_ID) String userOrgId,
            @RequestHeader(Constants.X_AUTH_USER_ROLES) List<String> userRoles) {

        SBApiResponse response = cbPlanService.publishCbPlan(request, userOrgId, token, userRoles);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @DeleteMapping("/v1/archive")
    public ResponseEntity<SBApiResponse> retireCbPlan(
            @RequestBody SunbirdApiRequest request,
            @RequestHeader(Constants.X_AUTH_TOKEN) String token,
            @RequestHeader(Constants.X_AUTH_USER_ORG_ID) String userOrgId,
            @RequestHeader(Constants.X_AUTH_USER_ROLES) List<String> userRoles) {

        SBApiResponse response = cbPlanService.retireCbPlan(request, userOrgId, token, userRoles);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/v1/read/{cbPlanId}")
    public ResponseEntity<SBApiResponse> readCbPlan(
            @PathVariable("cbPlanId") String cbPlanId,
            @RequestHeader(Constants.X_AUTH_TOKEN) String token,
            @RequestHeader(Constants.X_AUTH_USER_ORG_ID) String userOrgId) {

        SBApiResponse response = cbPlanService.readCbPlan(cbPlanId, userOrgId, token);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/v1/list")
    public ResponseEntity<SBApiResponse> listCBPlan(
        @RequestBody SunbirdApiRequest request,
        @RequestHeader(Constants.X_AUTH_TOKEN) String token,
        @RequestHeader(Constants.X_AUTH_USER_ORG_ID) String userOrgId) {
        SBApiResponse response = cbPlanService.listCbPlan(request, userOrgId, token);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/v1/user/list")
    public ResponseEntity<SBApiResponse> getCBPlanListForUser(
            @RequestHeader(Constants.X_AUTH_TOKEN) String token,
            @RequestHeader(Constants.X_AUTH_USER_ORG_ID)String userOrgId) {

        SBApiResponse response = cbPlanService.getCBPlanListForUser(userOrgId, token);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/v1/private/user/list")
    public ResponseEntity<SBApiResponse> getPrivateCBPlanListForUser(
            @RequestHeader(Constants.X_AUTH_USER_ID) String userId,
            @RequestHeader(Constants.X_AUTH_USER_ORG_ID)String userOrgId) {

        SBApiResponse response = cbPlanService.getCBPlanListForUser(userOrgId, userId,true);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/v1/admin/requestcontent")
    public ResponseEntity<SBApiResponse> requestCbplanContent(
            @RequestBody SunbirdApiRequest request,
            @RequestHeader(Constants.X_AUTH_TOKEN) String token,
            @RequestHeader(Constants.X_AUTH_USER_ORG_ID)String userOrgId){

        SBApiResponse response = cbPlanService.requestCbplanContent(request, token, userOrgId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}

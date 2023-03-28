package org.sunbird.searchby.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.searchby.model.PositionListResponse;
import org.sunbird.searchby.service.MasterDataServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/masterData/v1")
public class MasterDataController {

    @Autowired
    private MasterDataServiceImpl masterDataService;

    @GetMapping("/positions")
    public ResponseEntity<?> getPositionsList(@RequestHeader(Constants.X_AUTH_TOKEN) String userToken) {
        PositionListResponse response = masterDataService.getListPositions(userToken);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatusInfo().getStatusCode()));
    }

    @GetMapping("/getLanguages")
    public ResponseEntity<?> getLanguagesList() {
        return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.LANGUAGES), HttpStatus.OK);
    }

    @GetMapping("/getNationalities")
    public ResponseEntity<?> getNationalitiesList() {
        return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.NATIONALITIES), HttpStatus.OK);
    }
    @GetMapping("/getIndustries")
    public ResponseEntity<?> getIndustriesList() {
        return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.INDUSTRIES), HttpStatus.OK);
    }

    @GetMapping("/getGraduations")
    public ResponseEntity<?> getGraduationsList() {
        return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.GRADUATIONS), HttpStatus.OK);
    }

    @GetMapping("/getPostGraduations")
    public ResponseEntity<?> getPostGraduationsList() {
        return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.POST_GRADUATIONS), HttpStatus.OK);
    }

    @GetMapping("/getProfilePageMetaData")
    public ResponseEntity<?> getProfilePageMetaData() {
        return new ResponseEntity<>(masterDataService.getProfilePageMetaData(), HttpStatus.OK);
    }

    @GetMapping("/getMinistries")
    public ResponseEntity<?> getMinistriesList() {
        return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.MINISTRIES), HttpStatus.OK);
    }

    @GetMapping("/getCadre")
    public ResponseEntity<?> getCadreList() {
        return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.CADRE), HttpStatus.OK);
    }

    @GetMapping("/getService")
    public ResponseEntity<?> getServiceList() {
        return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.SERVICE), HttpStatus.OK);
    }

    @GetMapping("/getDesignation")
    public ResponseEntity<?> getDesignationList() {
       return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.DESIGNATIONS), HttpStatus.OK);
    }

    @GetMapping("/getGradePay")
    public ResponseEntity<?> getGradePayList() {
        return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.GRADE_PAY), HttpStatus.OK);
    }

    @PostMapping("/upsert")
    public ResponseEntity<?> upsertMasterData(@RequestBody Map<String,Object> request) {
        SBApiResponse response = masterDataService.upsertMasterData(request);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}

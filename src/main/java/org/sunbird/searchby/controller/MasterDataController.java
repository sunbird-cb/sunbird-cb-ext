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
        Map<String,Object> response = masterDataService.getMasterDataByType(Constants.LANGUAGES);
        return new ResponseEntity<>(response.get(Constants.RESULT), HttpStatus.OK);
    }

    @GetMapping("/getNationalities")
    public ResponseEntity<?> getNationalitiesList() {
        Map<String,Object> response = masterDataService.getMasterDataByType(Constants.NATIONALITIES);
        return new ResponseEntity<>(response.get(Constants.RESULT), HttpStatus.OK);
    }
    @GetMapping("/getIndustries")
    public ResponseEntity<?> getIndustriesList() {
        Map<String,Object> response = masterDataService.getMasterDataByType(Constants.INDUSTRIES);
        return new ResponseEntity<>(response.get(Constants.RESULT), HttpStatus.OK);
    }

    @GetMapping("/getGraduations")
    public ResponseEntity<?> getGraduationsList() {
        Map<String, Object> response = masterDataService.getMasterDataByType(Constants.GRADUATIONS);
        return new ResponseEntity<>(response.get(Constants.RESULT), HttpStatus.OK);
    }

    @GetMapping("/getPostGraduations")
    public ResponseEntity<?> getPostGraduationsList() {
        Map<String, Object> response = masterDataService.getMasterDataByType(Constants.POST_GRADUATIONS);
        return new ResponseEntity<>(response.get(Constants.RESULT), HttpStatus.OK);
    }

    @GetMapping("/getProfilePageMetaData")
    public ResponseEntity<?> getProfilePageMetaData() {
        Map<String, Object> response = masterDataService.getProfilePageMetaData();
        return new ResponseEntity<>(response.get(Constants.RESULT), HttpStatus.OK);
    }

    @GetMapping("/getMinistries")
    public ResponseEntity<?> getMinistriesList() {
        Map<String, Object> response = masterDataService.getMasterDataByType(Constants.MINISTRIES);
        return new ResponseEntity<>(response.get(Constants.RESULT), HttpStatus.OK);
    }

    @GetMapping("/getCadre")
    public ResponseEntity<?> getCadreList() {
        Map<String, Object> response = masterDataService.getMasterDataByType(Constants.CADRE);
        return new ResponseEntity<>(response.get(Constants.RESULT), HttpStatus.OK);
    }

    @GetMapping("/getService")
    public ResponseEntity<?> getServiceList() {
        Map<String, Object> response = masterDataService.getMasterDataByType(Constants.SERVICE);
        return new ResponseEntity<>(response.get(Constants.RESULT), HttpStatus.OK);
    }

    @GetMapping("/getDesignation")
    public ResponseEntity<?> getDesignationList() {
        Map<String, Object> response = masterDataService.getMasterDataByType(Constants.DESIGNATIONS);
        return new ResponseEntity<>(response.get(Constants.RESULT), HttpStatus.OK);
    }

    @GetMapping("/getGradePay")
    public ResponseEntity<?> getGradePayList() {
        Map<String, Object> response = masterDataService.getMasterDataByType(Constants.GRADE_PAY);
        return new ResponseEntity<>(response.get(Constants.RESULT), HttpStatus.OK);
    }

    @PostMapping("/upsert")
    public ResponseEntity<?> upsertMasterData(@RequestBody Map<String,Object> request) {
        SBApiResponse response = masterDataService.upsertMasterData(request);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}

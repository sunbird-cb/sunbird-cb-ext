package org.sunbird.searchby.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.searchby.model.FracApiResponseV2;
import org.sunbird.searchby.service.MasterDataServiceImpl;
import java.util.Map;

@RestController
public class MasterDataController {

    @Autowired
    private MasterDataServiceImpl masterDataService;

    @GetMapping("/v1/getPositions")
    public ResponseEntity<?> getPositionsList(@RequestHeader(Constants.X_AUTH_TOKEN) String userToken) {
        FracApiResponseV2 response = masterDataService.getListPositions(userToken);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatusInfo().getStatusCode()));
    }

    @GetMapping("/v1/getLanguage")
    public ResponseEntity<?> getLanguagesList() {
        SBApiResponse response = masterDataService.getMasterDataByType(Constants.LANGUAGE);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/v1/getNationalities")
    public ResponseEntity<?> getNationalitiesList() {
        SBApiResponse response = masterDataService.getMasterDataByType(Constants.NATIONALITY);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/v1/getIndustries")
    public ResponseEntity<?> getIndustriesList() {
        SBApiResponse response = masterDataService.getMasterDataByType(Constants.INDUSTRY);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/v1/getGraduations")
    public ResponseEntity<?> getGraduationsList() {
        SBApiResponse response = masterDataService.getMasterDataByType(Constants.GRADUATION);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/v1/getPostGraduations")
    public ResponseEntity<?> getPostGraduationsList() {
        SBApiResponse response = masterDataService.getMasterDataByType(Constants.POST_GRADUATION);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/v1/getMinistries")
    public ResponseEntity<?> getMinistriesList() {
        SBApiResponse response = masterDataService.getMasterDataByType(Constants.MINISTRY);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/v1/getCadre")
    public ResponseEntity<?> getCadreList() {
        SBApiResponse response = masterDataService.getMasterDataByType(Constants.CADRE);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/v1/getService")
    public ResponseEntity<?> getServiceList() {
        SBApiResponse response = masterDataService.getMasterDataByType(Constants.SERVICE);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
    @GetMapping("/v1/getDesignation")
    public ResponseEntity<?> getDesignationList() {
        SBApiResponse response = masterDataService.getMasterDataByType(Constants.DESIGNATION);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/v1/create")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> request) {
        SBApiResponse response = masterDataService.create(request);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PatchMapping("/v1/update")
    public ResponseEntity<?> update(@RequestBody Map<String, Object> request, @RequestParam String id, @RequestParam String type) {
        SBApiResponse response = masterDataService.update(request, id, type);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}

package org.sunbird.searchby.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.FracApiResponse;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.searchby.service.MasterDataServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/masterData/v1")
public class MasterDataController {

	@Autowired
	private MasterDataServiceImpl masterDataService;

	@GetMapping("/positions")
	public ResponseEntity<FracApiResponse> getPositionsList() {
		FracApiResponse response = masterDataService.getListPositions();
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatusInfo().getStatusCode()));
	}

	@GetMapping("/getLanguages")
	public ResponseEntity<Map<String,Object>> getLanguagesList() {
		return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.LANGUAGES), HttpStatus.OK);
	}

	@GetMapping("/getNationalities")
	public ResponseEntity<Map<String,Object>> getNationalitiesList() {
		return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.NATIONALITY), HttpStatus.OK);
	}

	@GetMapping("/getCountries")
	public ResponseEntity<Map<String,Object>> getCountriesList() {
		return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.COUNTRIES), HttpStatus.OK);
	}

	@GetMapping("/getIndustries")
	public ResponseEntity<Map<String,Object>> getIndustriesList() {
		return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.INDUSTRIES), HttpStatus.OK);
	}

	@GetMapping("/getGraduations")
	public ResponseEntity<Map<String,Object>> getGraduationsList() {
		return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.GRADUATIONS), HttpStatus.OK);
	}

	@GetMapping("/getPostGraduations")
	public ResponseEntity<Map<String,Object>> getPostGraduationsList() {
		return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.POST_GRADUATIONS), HttpStatus.OK);
	}

	@GetMapping("/getProfilePageMetaData")
	public ResponseEntity<Map<String,Object>> getProfilePageMetaData() {
		return new ResponseEntity<>(masterDataService.getProfilePageMetaData(), HttpStatus.OK);
	}

	@GetMapping("/getMinistries")
	public ResponseEntity<Map<String,Object>> getMinistriesList() {
		return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.MINISTRIES), HttpStatus.OK);
	}

	@GetMapping("/getCadre")
	public ResponseEntity<Map<String,Object>> getCadreList() {
		return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.CADRE), HttpStatus.OK);
	}

	@GetMapping("/getService")
	public ResponseEntity<Map<String,Object>> getServiceList() {
		return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.SERVICE), HttpStatus.OK);
	}

	@GetMapping("/getDesignation")
	public ResponseEntity<Map<String,Object>> getDesignationList() {
		return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.DESIGNATIONS), HttpStatus.OK);
	}

	@GetMapping("/getGradePay")
	public ResponseEntity<Map<String,Object>> getGradePayList() {
		return new ResponseEntity<>(masterDataService.getMasterDataByType(Constants.GRADE_PAY), HttpStatus.OK);
	}

	@PostMapping("/upsert")
	public ResponseEntity<SBApiResponse> upsertMasterData(@RequestBody Map<String, Object> request) {
		SBApiResponse response = masterDataService.upsertMasterData(request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}
}

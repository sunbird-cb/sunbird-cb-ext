package org.sunbird.storage.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.storage.service.StorageService;

import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
@RequestMapping("/storage")
public class StorageController {

	@Autowired
	StorageService storageService;
	
	@Autowired
	CbExtServerProperties serverConfig;

	@PostMapping("/upload")
	public ResponseEntity<?> upload(@RequestParam(value = "file", required = true) MultipartFile multipartFile)
			throws IOException {
		SBApiResponse uploadResponse = storageService.uploadFile(multipartFile, serverConfig.getCloudContainerName());
		return new ResponseEntity<>(uploadResponse, uploadResponse.getResponseCode());
	}

	@DeleteMapping("/delete")
	public ResponseEntity<?> deleteCloudFile(@RequestParam(value = "fileName", required = true) String fileName)
			throws JsonProcessingException {
		SBApiResponse deleteResponse = storageService.deleteFile(fileName, serverConfig.getCloudContainerName());
		return new ResponseEntity<>(deleteResponse, deleteResponse.getResponseCode());
	}

	@GetMapping("/v1/report/{reportType}/{date}/{orgId}/{fileName}")
	public ResponseEntity<?> downloadFile(@PathVariable("reportType") String reportType,
										  @PathVariable("date") String date,
										  @PathVariable("orgId") String orgId,
										  @RequestHeader(Constants.X_AUTH_TOKEN) String userToken,
										  @PathVariable("fileName") String fileName) {
		return storageService.downloadFile(reportType, date, orgId, fileName, userToken);
	}

	@GetMapping("/v1/reportInfo/{orgId}")
	public ResponseEntity<?> getFileInfo(@PathVariable("orgId") String orgId) {
		return storageService.getFileInfo(orgId);
	}

	@PostMapping("/profilePhotoUpload/{cloudFolderName}")
	public ResponseEntity<?> profileUpload(@PathVariable("cloudFolderName") String cloudFolderName,@RequestParam(value = "file", required = true) MultipartFile multipartFile)
			throws IOException {
		SBApiResponse uploadResponse = storageService.uploadFile(multipartFile, cloudFolderName, serverConfig.getCloudProfileImageContainerName());
		return new ResponseEntity<>(uploadResponse, uploadResponse.getResponseCode());
	}

	@GetMapping("/v1/spvReport/{reportType}/{date}/{fileName}")
	public ResponseEntity<?> downloadFileSPV(@PathVariable("reportType") String reportType,
										  @PathVariable("date") String date,
										  @RequestHeader(Constants.X_AUTH_TOKEN) String userToken,
										  @PathVariable("fileName") String fileName) {
		return storageService.downloadFile(reportType, date, fileName, userToken);
	}

	@GetMapping("/v1/spvReportInfo/{date}")
	public ResponseEntity<?> getFileInfoSPV(@RequestHeader(Constants.X_AUTH_TOKEN) String userToken,
											@PathVariable("date") String date) {
		return storageService.getFileInfoSpv(userToken, date);
	}

	@PostMapping("/orgStoreUpload")
	public ResponseEntity<?> orgStoreUpload(@RequestHeader(Constants.X_AUTH_TOKEN) String userToken
											,@RequestParam(value = "file", required = true) MultipartFile multipartFile)
			throws IOException {
		SBApiResponse uploadResponse = storageService.uploadFileForOrg(multipartFile, userToken);
		return new ResponseEntity<>(uploadResponse, uploadResponse.getResponseCode());
	}
}

package org.sunbird.storage.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
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

	@GetMapping("/v1/report/{cloudContainerName}/{date}/{folderName}/{fileName}")
	public ResponseEntity<?> downloadFile(@PathVariable("cloudContainerName") String cloudContainerName,
										  @PathVariable("date") String date,
										  @PathVariable("folderName") String folderName,
										  @PathVariable("fileName") String fileName) {
		return storageService.downloadFile(cloudContainerName, date, folderName, fileName);
	}
}

package org.sunbird.storage.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.storage.service.StorageService;

@RestController
@RequestMapping("/storage")
public class StorageController {

	@Autowired
	StorageService storageService;

	@DeleteMapping("/delete")
	public ResponseEntity<SBApiResponse> deleteCloudFile(
			@RequestParam(value = "fileName", required = true) String fileName) {
		SBApiResponse deleteResponse = storageService.deleteFile(fileName);
		return new ResponseEntity<>(deleteResponse, deleteResponse.getResponseCode());
	}

	@PostMapping("/upload")
	public ResponseEntity<SBApiResponse> upload(
			@RequestParam(value = "file", required = true) MultipartFile multipartFile) throws IOException {
		SBApiResponse uploadResponse = storageService.uploadFile(multipartFile);
		return new ResponseEntity<>(uploadResponse, uploadResponse.getResponseCode());
	}
}

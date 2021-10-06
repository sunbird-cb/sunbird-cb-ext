package org.sunbird.storage.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.storage.service.StorageService;
import java.io.IOException;


@RestController
@RequestMapping("/v1/storage")

public class StorageController {

    @Autowired
    StorageService storageService;

    @Autowired
    private CbExtServerProperties cbExtServerProperties;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam(value = "file", required = true) MultipartFile multipartFile) throws IOException {
             SBApiResponse uploadResponse = storageService.uploadFile(cbExtServerProperties.getAzureContainerName(), multipartFile);
             return  new ResponseEntity<>(uploadResponse, uploadResponse.getResponseCode());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCloudFile(@RequestParam(value = "fileName", required = true) String fileName) throws JsonProcessingException {
             SBApiResponse deleteResponse = storageService.deleteFile(fileName);
             return  new ResponseEntity<>(deleteResponse, deleteResponse.getResponseCode());
    }
}
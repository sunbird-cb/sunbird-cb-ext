package org.sunbird.storage.controller;

import java.io.File;
import java.io.FileOutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.storage.service.StorageServiceImpl;



import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/storage")

public class StorageController {

    @Autowired
    StorageServiceImpl storageService;

    @Autowired
    private CbExtServerProperties cbExtServerProperties;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestParam(value = "file", required = true) MultipartFile multipartFile
    ) throws IOException {
        String folderName = cbExtServerProperties.getAzureContainerName();
        File file = new File(multipartFile.getOriginalFilename());
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
        Map<String, String> uploadedFile = storageService.uploadFile(folderName, file);
        file.delete();
        return new ResponseEntity<>(uploadedFile, HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>>  deleteCloudFile(@RequestParam(value = "fileName", required = true) String fileName)
            throws JsonProcessingException {
            Boolean deleted = storageService.deleteFile(fileName);
        Map<String, String> deleteFile = new HashMap<>();
        deleteFile.put("name", fileName);
            if (deleted) {
                deleteFile.put("status", "deleted");
                return new ResponseEntity<>(deleteFile,HttpStatus.OK);
            }
        deleteFile.put("status", "not deleted");
        return new ResponseEntity<>(deleteFile,HttpStatus.FAILED_DEPENDENCY);
    }
}
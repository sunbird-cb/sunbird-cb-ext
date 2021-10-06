package org.sunbird.storage.controller;

import java.io.File;
import java.io.FileOutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
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
    SBApiResponse response = new SBApiResponse();
    @PostMapping("/upload")
    public SBApiResponse upload(@RequestParam(value = "file", required = true) MultipartFile multipartFile
    ) throws IOException {
        response.setId("api.file.upload");

        try {
            File file = new File(multipartFile.getOriginalFilename());
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(multipartFile.getBytes());
            fos.close();
            Map<String, String> uploadedFile = storageService.uploadFile(cbExtServerProperties.getAzureContainerName(), file);
            file.delete();
            if (uploadedFile != null) {
                response.getParams().setStatus(Constants.SUCCESSFUL);
                response.setResponseCode(HttpStatus.OK);
                response.getResult().putAll(uploadedFile);
            }
        } catch (IOException e) {
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg("file not found");
            response.setResponseCode(HttpStatus.BAD_REQUEST);
        }
        return response;
    }

    @DeleteMapping("/delete")
    public SBApiResponse deleteCloudFile(@RequestParam(value = "fileName", required = true) String fileName)
            throws JsonProcessingException {
        Map<String, String> deleteFile = new HashMap<>();
            Boolean deleted = storageService.deleteFile(fileName);
            deleteFile.put("name", fileName);
        response.setId("api.file.delete");
        if (deleted) {
                deleteFile.put("status", "deleted");
                response.getParams().setStatus(Constants.SUCCESSFUL);
                response.setResponseCode(HttpStatus.OK);
                response.getResult().putAll(deleteFile);
            } else {
                deleteFile.put("error", "file not found");
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg("file not found");
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        return response;
    }
}
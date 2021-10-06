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
import org.sunbird.common.util.Constants;
import org.sunbird.storage.service.StorageServiceImpl;
import org.sunbird.common.model.Response;



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
    public Response upload(@RequestParam(value = "file", required = true) MultipartFile multipartFile
    ) throws IOException {
        Response response = new Response();
        try {
//        String folderName = cbExtServerProperties.getAzureContainerName();
            File file = new File(multipartFile.getOriginalFilename());
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(multipartFile.getBytes());
            fos.close();
            Map<String, String> uploadedFile = storageService.uploadFile(cbExtServerProperties.getAzureContainerName(), file);
            file.delete();

            if (uploadedFile != null) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put("value", uploadedFile);
//        return new ResponseEntity<>(uploadedFile, HttpStatus.OK);

            }
        } catch (IOException e) {
            response.put(Constants.MESSAGE, Constants.FAILED);
            response.put("value", "file not uploaded");
        }
        return response;
    }

    @DeleteMapping("/delete")
    public Response deleteCloudFile(@RequestParam(value = "fileName", required = true) String fileName)
            throws JsonProcessingException {
        Response response = new Response();
        Map<String, String> deleteFile = new HashMap<>();
            Boolean deleted = storageService.deleteFile(fileName);
//            Map<String, String> deleteFile = new HashMap<>();
            deleteFile.put("name", fileName);
            if (deleted) {
                deleteFile.put("status", "deleted");
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put("value", deleteFile);

//                return new ResponseEntity<>(deleteFile, HttpStatus.OK);
            } else {

                deleteFile.put("status", "not deleted");
                response.put(Constants.MESSAGE, Constants.FAILED);
                response.put("value", deleteFile);

            }


//            return new ResponseEntity<>(deleteFile, HttpStatus.FAILED_DEPENDENCY);


        return response;
    }
}
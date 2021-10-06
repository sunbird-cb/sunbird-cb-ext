package org.sunbird.storage.service;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import scala.Option;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Service
public class StorageServiceImpl implements StorageService{
    private Logger log = LoggerFactory.getLogger(StorageServiceImpl.class);

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private CbExtServerProperties cbExtServerProperties;


    private static BaseStorageService storageService = null;

    public static BaseStorageService getCloudStoreService() {
        return storageService;
    }
    public SBApiResponse uploadFile(String folderName, MultipartFile MFile) throws IOException {
        storageService = StorageServiceFactory.getStorageService(new StorageConfig(cbExtServerProperties.getAzureTypeName(), cbExtServerProperties.getAzureIdentityName(), cbExtServerProperties.getAzureStorageKey(), null));
        SBApiResponse response = new SBApiResponse();
        response.setId("api.file.upload");
        try {
            File file = new File(MFile.getOriginalFilename());
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(MFile.getBytes());
            fos.close();
            String objectKey = "";
            objectKey = folderName + "/" + file.getName();
            String url = storageService.upload(cbExtServerProperties.getAzureContainerName(), file.getAbsolutePath(), objectKey,
                    Option.apply(false), Option.apply(1), Option.apply(5), Option.empty());
            file.delete();
            Map<String, String> uploadedFile = new HashMap<>();
            uploadedFile.put("name", objectKey);
            uploadedFile.put("url", url);
            response.getParams().setStatus(Constants.SUCCESSFUL);
            response.setResponseCode(HttpStatus.OK);
            response.getResult().putAll(uploadedFile);
            return response;
        } catch (Exception e) {
            log.error("uploadFile: ", e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg("file not found");
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }

    }

    public SBApiResponse deleteFile(String fileName) {
        SBApiResponse response = new SBApiResponse();
        response.setId("api.file.delete");
        Map<String, String> deleteFile = new HashMap<>();
        deleteFile.put("name", fileName);
        try {
            storageService.deleteObject(cbExtServerProperties.getAzureContainerName(), fileName, Option.apply(Boolean.FALSE));
            deleteFile.put("status", "deleted");
            response.getParams().setStatus(Constants.SUCCESSFUL);
            response.setResponseCode(HttpStatus.OK);
            response.getResult().putAll(deleteFile);
            return response;
        } catch (Exception e) {
            log.error("deleteFile: ", e);
            deleteFile.put("error", "file not found");
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg("file not found");
            response.setResponseCode(HttpStatus.NOT_FOUND);
            response.getResult().putAll(deleteFile);
            return response;
        }
    }
}

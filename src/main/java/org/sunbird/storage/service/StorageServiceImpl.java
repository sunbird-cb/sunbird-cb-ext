package org.sunbird.storage.service;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;
import org.sunbird.common.util.CbExtServerProperties;
import scala.Option;


import java.io.File;
import java.util.HashMap;
import java.util.Map;


@Service
public class StorageServiceImpl {
    private Logger log = LoggerFactory.getLogger(StorageServiceImpl.class);

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private CbExtServerProperties cbExtServerProperties;


    private static BaseStorageService storageService = null;

    public static BaseStorageService getCloudStoreService() {
        return storageService;
    }
    public Map<String, String> uploadFile(String folderName, File file) {
        storageService = StorageServiceFactory.getStorageService(new StorageConfig(cbExtServerProperties.getAzureTypeName(), cbExtServerProperties.getAzureIdentityName(), cbExtServerProperties.getAzureStorageKey(), null));

        try {
            String objectKey = "";
            objectKey = folderName + "/" + file.getName();
            String url = storageService.upload(cbExtServerProperties.getAzureContainerName(), file.getAbsolutePath(), objectKey,
                    Option.apply(false), Option.apply(1), Option.apply(5), Option.empty());
            Map<String, String> uploadedFile = new HashMap<>();
            uploadedFile.put("name", objectKey);
            uploadedFile.put("url", url);
            return uploadedFile;
        } catch (Exception e) {
            log.error("uploadFile: ", e);
            return null;
        }

    }

    public Boolean deleteFile(String fileName) {
        try {
            storageService.deleteObject(cbExtServerProperties.getAzureContainerName(), fileName, Option.apply(Boolean.FALSE));
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("deleteFile: ", e);
            return Boolean.FALSE;
        }
    }
}

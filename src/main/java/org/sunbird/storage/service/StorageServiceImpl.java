package org.sunbird.storage.service;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.storage.models.StorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import scala.Option;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


@Service
public class StorageServiceImpl {
    private Logger log = LoggerFactory.getLogger(StorageServiceImpl.class);

    @Autowired
    RestTemplate restTemplate;

    private static BaseStorageService storageService = null;
    private static StorageService storageConfig = new StorageService();

    static {
        storageConfig = getStorageService();//This statement is used to access bucket credentials
        storageService = StorageServiceFactory.getStorageService(new StorageConfig("getProvider()",
                "getIdentity()", "getCredential()", null));

    }

    public static BaseStorageService getCloudStoreService() {
        return storageService;
    }

    public Map<String, String> uploadFile(String folderName, File file) {
        try {
            String objectKey = "";
                objectKey = folderName + "/" + "_" + file.getName();
            String url = storageService.upload("getContainer()", file.getAbsolutePath(), objectKey,
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
}

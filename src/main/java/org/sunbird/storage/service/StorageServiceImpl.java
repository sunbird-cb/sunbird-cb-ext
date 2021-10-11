package org.sunbird.storage.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

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

@Service
public class StorageServiceImpl implements StorageService {

	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	private BaseStorageService storageService = null;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private CbExtServerProperties cbExtServerProperties;

	@PostConstruct
	public void init() {
		if (storageService == null) {
			storageService = StorageServiceFactory.getStorageService(new StorageConfig(
					cbExtServerProperties.getAzureTypeName(), cbExtServerProperties.getAzureIdentityName(),
					cbExtServerProperties.getAzureStorageKey(), null));
		}
	}

	@Override
	public SBApiResponse uploadFile(MultipartFile mFile) throws IOException {
		SBApiResponse response = new SBApiResponse();
		response.setId(Constants.API_FILE_UPLOAD);
		try {
			File file = new File(System.currentTimeMillis() + "_" + mFile.getOriginalFilename());
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(mFile.getBytes());
			fos.close();
			String objectKey = cbExtServerProperties.getAzureContainerName() + "/" + file.getName();
			String url = storageService.upload(cbExtServerProperties.getAzureContainerName(), file.getAbsolutePath(),
					objectKey, Option.apply(false), Option.apply(1), Option.apply(5), Option.empty());
			file.delete();
			Map<String, String> uploadedFile = new HashMap<>();
			uploadedFile.put(Constants.NAME, file.getName());
			uploadedFile.put(Constants.URL, url);
			response.getParams().setStatus(Constants.SUCCESSFUL);
			response.setResponseCode(HttpStatus.OK);
			response.getResult().putAll(uploadedFile);
			return response;
		} catch (Exception e) {
			logger.error("Failed to upload file. Exception: ", e);
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg("Failed to upload file. Exception: " + e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return response;
		}
	}

	@Override
	public SBApiResponse deleteFile(String fileName) {
		SBApiResponse response = new SBApiResponse();
		response.setId(Constants.API_FILE_DELETE);
		try {
			storageService.deleteObject(cbExtServerProperties.getAzureContainerName(), fileName,
					Option.apply(Boolean.FALSE));
			response.getParams().setStatus(Constants.SUCCESSFUL);
			response.setResponseCode(HttpStatus.OK);
			return response;
		} catch (Exception e) {
			logger.error("Failed to delete file: " + fileName + ", Exception: ", e);
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg("Failed to delete file. Exception: " + e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return response;
		}
	}

	protected void finalize() {
		try {
			if (storageService != null) {
				storageService.closeContext();
				storageService = null;
			}
		} catch (Exception e) {
		}
	}
}

package org.sunbird.storage.service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.Model;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;

import org.sunbird.common.util.ProjectUtil;
import scala.Option;

@Service
public class StorageServiceImpl implements StorageService {

	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	private BaseStorageService storageService = null;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private CbExtServerProperties serverProperties;

	@PostConstruct
	public void init() {
		if (storageService == null) {
			storageService = StorageServiceFactory.getStorageService(new StorageConfig(
					serverProperties.getCloudStorageTypeName(), serverProperties.getCloudStorageKey(),
					serverProperties.getCloudStorageSecret(), Option.apply(serverProperties.getCloudStorageCephs3Endpoint())));
		}
	}

	@Override
	public SBApiResponse uploadFile(MultipartFile mFile, String containerName) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_FILE_UPLOAD);
		File file = null;
		try {
			file = new File(System.currentTimeMillis() + "_" + mFile.getOriginalFilename());
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(mFile.getBytes());
			fos.close();
			return uploadFile(file, containerName);
		} catch (Exception e) {
			logger.error("Failed to upload file. Exception: ", e);
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg("Failed to upload file. Exception: " + e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return response;
		} finally {
			if (file != null) {
				file.delete();
			}
		}
	}

	@Override
	public SBApiResponse uploadFile(File file, String containerName) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_FILE_UPLOAD);
		try {
			String objectKey = containerName + "/" + file.getName();
			String url = storageService.upload(serverProperties.getCloudContainerName(), file.getAbsolutePath(),
					objectKey, Option.apply(false), Option.apply(1), Option.apply(5), Option.empty());
			Map<String, String> uploadedFile = new HashMap<>();
			uploadedFile.put(Constants.NAME, file.getName());
			uploadedFile.put(Constants.URL, url);
			response.getResult().putAll(uploadedFile);
			return response;
		} catch (Exception e) {
			logger.error("Failed to upload file. Exception: ", e);
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg("Failed to upload file. Exception: " + e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return response;
		} finally {
			if (file != null) {
				file.delete();
			}
		}
	}

	@Override
	public SBApiResponse deleteFile(String fileName, String containerName) {
		SBApiResponse response = new SBApiResponse();
		response.setId(Constants.API_FILE_DELETE);
		try {
			String objectKey = serverProperties.getCloudContainerName() + "/" + fileName;
			storageService.deleteObject(serverProperties.getCloudContainerName(), objectKey,
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
	@Override
	public SBApiResponse downloadFile(String fileName) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_FILE_DOWNLOAD);
		try {
			String objectKey = serverProperties.getBulkUploadContainerName() + "/" + fileName;
			storageService.download(serverProperties.getCloudContainerName(), objectKey, Constants.LOCAL_BASE_PATH,
					Option.apply(Boolean.FALSE));
			return response;
		} catch (Exception e) {
			logger.error("Failed to download the file: " + fileName + ", Exception: ", e);
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg("Failed to download the file. Exception: " + e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return response;
		}
	}

	@Override
	public ResponseEntity<Resource> downloadFile(String reportType, String date, String orgId, String fileName) {
		try {
			String objectKey = serverProperties.getReportDownloadFolderName() + "/" + reportType + "/" + date + "/" + orgId + "/" + fileName;
			storageService.download(serverProperties.getReportDownloadContainerName(), objectKey, Constants.LOCAL_BASE_PATH,
					Option.apply(Boolean.FALSE));
			Path tmpPath = Paths.get(Constants.LOCAL_BASE_PATH + fileName);
			ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(tmpPath));
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
			return ResponseEntity.ok()
					.headers(headers)
					.contentLength(tmpPath.toFile().length())
					.contentType(MediaType.parseMediaType(MediaType.MULTIPART_FORM_DATA_VALUE))
					.body(resource);
		} catch (Exception e) {
			logger.error("Failed to read the downloaded file: " + fileName + ", Exception: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} finally {
			try {
				File file = new File(Constants.LOCAL_BASE_PATH + fileName);
				if(file.exists()) {
					file.delete();
				}
			} catch(Exception e1) {
			}
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

	@Override
	public ResponseEntity<Map<String, Map<String, Object>>> getFileInfo(String orgId) {
		try {
			Map<String, Map<String, Object>> reportTypeInfo = new HashMap<>();
			for (String reportType : serverProperties.getReportTypeGetFileInfo()) {
				Map<String, Object> resourceMap = new HashMap<>();
				LocalDateTime now = LocalDateTime.now();
				DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				String todayFormattedDate = now.format(dateFormat);
				String mdoId = "";
				if (orgId != null && !orgId.isEmpty()) {
					if (orgId.contains("=")) {
						String[] array = orgId.split("=");
						mdoId = array[1];
					} else {
						mdoId = orgId;
					}
				}
				String fileName = mdoId + ".csv";
				String objectKey = serverProperties.getReportDownloadFolderName() + "/" + reportType + "/" + todayFormattedDate + "/" + orgId + "/" + fileName;
				Model.Blob blob = storageService.getObject(serverProperties.getReportDownloadContainerName(), objectKey, Option.apply(Boolean.FALSE));
				if (blob != null) {
					resourceMap.put("lastModified", blob.lastModified());
					resourceMap.put("fileMetaData", blob.metadata());
				} else {
					LocalDateTime yesterday = now.minusDays(1);
					String yesterdayFormattedDate = yesterday.format(dateFormat);
					objectKey = serverProperties.getReportDownloadFolderName() + "/" + reportType + "/" + yesterdayFormattedDate + "/" + orgId + "/" + fileName;
					blob = storageService.getObject(serverProperties.getReportDownloadContainerName(), objectKey, Option.apply(Boolean.FALSE));
					if (blob != null) {
						resourceMap.put("lastModified", blob.lastModified());
						resourceMap.put("fileMetaData", blob.metadata());
						logger.info("Unable to fetch fileInfo");
					} else {
						resourceMap.put("mssg", "No Report Available");
					}
				}
				reportTypeInfo.put(reportType, resourceMap);
			}
			return ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(reportTypeInfo);
		} catch (Exception e) {
			logger.error("Failed to read the downloaded file: " + ", Exception: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

	}
}

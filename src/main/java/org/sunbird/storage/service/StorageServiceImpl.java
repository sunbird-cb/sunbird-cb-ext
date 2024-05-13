package org.sunbird.storage.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.sunbird.common.util.AccessTokenValidator;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;

import org.sunbird.common.util.ProjectUtil;
import org.sunbird.user.service.UserUtilityService;
import scala.Option;

@Service
public class StorageServiceImpl implements StorageService {

	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	private BaseStorageService storageService = null;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private CbExtServerProperties serverProperties;

	@Autowired
	private AccessTokenValidator accessTokenValidator;

	@Autowired
	private UserUtilityService userUtilityService;

	@PostConstruct
	public void init() {
		if (storageService == null) {
			storageService = StorageServiceFactory.getStorageService(new StorageConfig(
					serverProperties.getCloudStorageTypeName(), serverProperties.getCloudStorageKey(),
					serverProperties.getCloudStorageSecret(), Option.apply(serverProperties.getCloudStorageCephs3Endpoint())));
		}
	}

	@Override
	public SBApiResponse uploadFile(MultipartFile mFile, String cloudFolderName) throws IOException {
		return uploadFile(mFile, cloudFolderName, serverProperties.getCloudContainerName());
	}
	public SBApiResponse uploadFile(MultipartFile mFile, String cloudFolderName, String containerName) throws IOException {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_FILE_UPLOAD);
		File file = null;
		try {
			file = new File(System.currentTimeMillis() + "_" + mFile.getOriginalFilename());
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(mFile.getBytes());
			fos.close();
			return uploadFile(file, cloudFolderName,containerName);
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
	public SBApiResponse uploadFile(File file, String cloudFolderName, String containerName) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_FILE_UPLOAD);
		try {
			String objectKey = cloudFolderName + "/" + file.getName();
			String url = storageService.upload(containerName, file.getAbsolutePath(),
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
	public ResponseEntity<Resource> downloadFile(String reportType, String date, String orgId, String fileName, String userToken) {
		try {
			String userId = accessTokenValidator.fetchUserIdFromAccessToken(userToken);
			if (StringUtils.isEmpty(userId)) {
				logger.error("Failed to get UserId for orgId: " + orgId);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}
			Map<String, Map<String, String>> userInfoMap = new HashMap<>();
			userUtilityService.getUserDetailsFromDB(Arrays.asList(userId), Arrays.asList(Constants.USER_ID, Constants.ROOT_ORG_ID), userInfoMap);
			if (MapUtils.isEmpty(userInfoMap)) {
				logger.error("Failed to get UserInfo from cassandra for userId: " + userId);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
			String rootOrgId = userInfoMap.get(userId).get(Constants.ROOT_ORG_ID);
			if (StringUtils.isNotEmpty(orgId)) {
				if (orgId.contains("=")) {
					rootOrgId = "mdoid=" + rootOrgId;
				}
			}
			if (!rootOrgId.equalsIgnoreCase(orgId)) {
				logger.error("User is not authorized to download the file for other org: " + rootOrgId + ", request orgId " + orgId);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}
			String objectKey = "";
			if (serverProperties.getReportPropertyFileAllMdo().contains(fileName)) {
				String reportSubFolderName = fileName.replace(".csv", "");
				objectKey = serverProperties.getReportDownloadFolderName() + "/" + reportType + "/" + date + "/" + reportSubFolderName + "/" + fileName;
			} else {
				objectKey = serverProperties.getReportDownloadFolderName() + "/" + reportType + "/" + date + "/" + orgId + "/" + fileName;
			}

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
		Map<String, String> reportFileNameMap = serverProperties.getReportMap();
		Map<String, Map<String, Object>> reportTypeInfo = new HashMap<>();
		for (String reportType : serverProperties.getReportTypeGetFileInfo()) {
			Map<String, Object> resourceMap = new HashMap<>();
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			String todayFormattedDate = now.format(dateFormat);
			String mdoId = "mdoid=" + orgId;
				/*if (orgId != null && !orgId.isEmpty()) {
					if (orgId.contains("=")) {
						String[] array = orgId.split("=");
						mdoId = array[1];
					} else {
						mdoId = orgId;
					}
				}*/
			String fileName = reportFileNameMap.get(reportType);
			String objectKey = "";
			if (serverProperties.getReportPropertyFileAllMdo().contains(fileName)) {
				String reportSubFolderName = fileName.replace(".csv", "");
				objectKey = serverProperties.getReportDownloadFolderName() + "/" + reportType + "/" + todayFormattedDate + "/" + reportSubFolderName + "/" + fileName;
			} else {
				objectKey = serverProperties.getReportDownloadFolderName() + "/" + reportType + "/" + todayFormattedDate + "/" + mdoId + "/" + fileName;
			}
			try {
				Model.Blob blob = storageService.getObject(serverProperties.getReportDownloadContainerName(), objectKey, Option.apply(Boolean.FALSE));
				if (blob != null) {
					resourceMap.put("lastModified", blob.lastModified());
					resourceMap.put("fileMetaData", blob.metadata());
				}
			} catch (Exception e) {
				logger.error("Failed to read the downloaded file for url: " + objectKey);
				LocalDateTime yesterday = now.minusDays(1);
				String yesterdayFormattedDate = yesterday.format(dateFormat);
				if (serverProperties.getReportPropertyFileAllMdo().contains(fileName)) {
					String reportSubFolderName = fileName.replace(".csv", "");
					objectKey = serverProperties.getReportDownloadFolderName() + "/" + reportType + "/" + yesterdayFormattedDate + "/" + reportSubFolderName + "/" + fileName;
				} else {
					objectKey = serverProperties.getReportDownloadFolderName() + "/" + reportType + "/" + yesterdayFormattedDate + "/" + mdoId + "/" + fileName;
				}
				try {
					Model.Blob blob = storageService.getObject(serverProperties.getReportDownloadContainerName(), objectKey, Option.apply(Boolean.FALSE));
					if (blob != null) {
						resourceMap.put("lastModified", blob.lastModified());
						resourceMap.put("fileMetaData", blob.metadata());
					} else {
						resourceMap.put("msg", "No Report Available");
						logger.info("Unable to fetch fileInfo");
					}
				} catch (Exception ex) {
					logger.error("Failed to read the downloaded file for url: " + objectKey);
				}
			}
			reportTypeInfo.put(fileName, resourceMap);
		}
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(reportTypeInfo);

	}

	@Override
	public ResponseEntity<?> downloadFile(String reportType, String date, String fileName, String userToken) {
		try {
			String userId = accessTokenValidator.fetchUserIdFromAccessToken(userToken);
			if (StringUtils.isEmpty(userId)) {
				logger.error("Failed to get user");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}
			Map<String, Map<String, String>> userInfoMap = new HashMap<>();
			userUtilityService.getUserDetailsFromDB(Arrays.asList(userId), Arrays.asList(Constants.USER_ID, Constants.CHANNEL), userInfoMap);
			if (MapUtils.isEmpty(userInfoMap)) {
				logger.error("Failed to get UserInfo from cassandra for userId: " + userId);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
			String channel = userInfoMap.get(userId).get(Constants.CHANNEL);

			if (!serverProperties.getSpvChannelName().equalsIgnoreCase(channel)) {
				logger.error("User is not authorized to download the file for other org: ");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}
			Map<String, String> spvReportSubFolderTypeMap = serverProperties.getSpvReportSubFolderTypeMap();
			String objectKey = serverProperties.getReportDownloadFolderName() + "/" + spvReportSubFolderTypeMap.get(fileName) + "/" + date + "/" + reportType + "/" + fileName;
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
				if (file.exists()) {
					file.delete();
				}
			} catch (Exception e1) {
			}
		}

	}

	@Override
	public ResponseEntity<?> getFileInfoSpv(String userToken, String date) {
		String userId = accessTokenValidator.fetchUserIdFromAccessToken(userToken);
		if (StringUtils.isEmpty(userId)) {
			logger.error("Failed to get user");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		Map<String, Map<String, String>> userInfoMap = new HashMap<>();
		userUtilityService.getUserDetailsFromDB(Arrays.asList(userId), Arrays.asList(Constants.USER_ID, Constants.CHANNEL), userInfoMap);
		if (MapUtils.isEmpty(userInfoMap)) {
			logger.error("Failed to get UserInfo from cassandra for userId: " + userId);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		String channel = userInfoMap.get(userId).get(Constants.CHANNEL);

		if (!serverProperties.getSpvChannelName().equalsIgnoreCase(channel)) {
			logger.error("User is not authorized to download the file for other org: ");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		Map<String, String> reportFileNameMap = serverProperties.getSpvReportMap();
		Map<String, String> spvReportSubFolderTypeMap = serverProperties.getSpvReportSubFolderTypeMap();
		Map<String, Map<String, Object>> reportTypeInfo = new HashMap<>();
		for (Map.Entry<String, String> entry : reportFileNameMap.entrySet()) {
			Map<String, Object> resourceMap = new HashMap<>();
			String fileName = entry.getValue();
			String reportType = "";
			if (fileName.contains(".")) {
				String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
				reportType = entry.getValue().replace("." + fileExtension, "");
			}

			String objectKey = serverProperties.getReportDownloadFolderName() + "/" + spvReportSubFolderTypeMap.get(fileName) + "/" + date + "/" + reportType + "/" + fileName;
			try {
				Model.Blob blob = storageService.getObject(serverProperties.getReportDownloadContainerName(), objectKey, Option.apply(Boolean.FALSE));
				if (blob != null) {
					resourceMap.put("lastModified", blob.lastModified());
					resourceMap.put("fileMetaData", blob.metadata());
				}
			} catch (Exception e) {
				logger.error("Failed to read the downloaded file for url: " + objectKey);
			}
			reportTypeInfo.put(fileName, resourceMap);
		}
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(reportTypeInfo);
	}

	@Override
	public SBApiResponse downloadFile(String fileName, String containerName) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_FILE_DOWNLOAD);
		try {
			String objectKey = containerName + "/" + fileName;
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
	public SBApiResponse uploadFileForOrg(MultipartFile mFile, String userToken) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_FILE_UPLOAD);
		try {
			String userId = accessTokenValidator.fetchUserIdFromAccessToken(userToken);
			if (StringUtils.isEmpty(userId)) {
				logger.error("Failed to get the UserInfo from token");
				response.getParams().setStatus(Constants.FAILED);
				response.getParams().setErrmsg("Failed to get the UserInfo from token");
				response.setResponseCode(HttpStatus.UNAUTHORIZED);
				return response;
			}
			Map<String, Map<String, String>> userInfoMap = new HashMap<>();
			userUtilityService.getUserDetailsFromDB(Arrays.asList(userId), Arrays.asList(Constants.USER_ID, Constants.ROOT_ORG_ID), userInfoMap);
			if (MapUtils.isEmpty(userInfoMap)) {
				logger.error("Failed to get the UserInfo from token");
				response.getParams().setStatus(Constants.FAILED);
				response.getParams().setErrmsg("Failed to get the UserInfo from token");
				response.setResponseCode(HttpStatus.UNAUTHORIZED);
				return response;
			}
			String orgId = userInfoMap.get(userId).get(Constants.ROOT_ORG_ID);
			return uploadFile(mFile, serverProperties.getOrgStoreFolderName() + "/" + orgId, serverProperties.getCloudPublicContainerName());
		} catch (IOException e) {
			logger.error("Failed to upload the file, Exception: ", e);
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg("Failed to upload the file, Exception: " + e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return response;
		}
	}
}

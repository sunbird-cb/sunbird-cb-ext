package org.sunbird.operationalreports.service;


import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.Model;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.AccessTokenValidator;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.config.PropertiesConfig;
import org.sunbird.operationalreports.exception.ZipProcessingException;
import org.sunbird.user.service.UserUtilityService;
import scala.Option;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Deepak kumar Thakur & Mahesh R V
 */
@Service
public class OperationalReportServiceImpl implements OperationalReportService {
    private static final Logger logger = LoggerFactory.getLogger(OperationalReportServiceImpl.class);

    private static final String ALPHANUMERIC_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Autowired
    CbExtServerProperties serverProperties;

    @Autowired
    PropertiesConfig configuration;

    @Autowired
    private OutboundRequestHandlerServiceImpl outboundReqService;

    @Autowired
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    @Autowired
    AccessTokenValidator accessTokenValidator;

    @Autowired
    private CassandraOperation cassandraOperation;

    @Autowired
    private CbExtServerProperties serverConfig;

    @Autowired
    private UserUtilityService userUtilityService;

    private BaseStorageService storageService = null;

    @Override
    public SBApiResponse grantReportAccessToMDOAdmin(SunbirdApiRequest request, String userOrgId, String authToken) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.GRANT_REPORT_ACCESS_API);
        try {
            Map<String, Object> mdoAdminDetails = (Map<String, Object>) request.getRequest();
            String mdoAdminUserId = (String) ((Map<String, Object>) mdoAdminDetails.get(Constants.MDO_ADMIN_ROLE)).get(Constants.USER_ID);
            String reportExpiryDate = (String) ((Map<String, Object>) mdoAdminDetails.get(Constants.MDO_ADMIN_ROLE)).get(Constants.REPORT_EXPIRY_DATE);
            Map<String, String> headersValue = new HashMap<>();
            headersValue.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
            String mdoLeaderUserId = accessTokenValidator.fetchUserIdFromAccessToken(authToken);
            if (null == mdoLeaderUserId) {
                throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,"UserId Couldn't fetch from auth token");
            }
            logger.info("MDO Leader User ID : {}", mdoLeaderUserId);
            StringBuilder url = new StringBuilder(configuration.getLmsServiceHost()).append(configuration.getLmsUserSearchEndPoint());
            Map<String, Object> userSearchRequest = getSearchObject(Arrays.asList(mdoLeaderUserId, mdoAdminUserId));
            Map<String, Object> searchProfileApiResp = outboundReqService.fetchResultUsingPost(url.toString(), userSearchRequest, headersValue);
            if (searchProfileApiResp != null) {
                String mdoUserOrgId = (String) searchProfileApiResp.get(Constants.ROOT_ORG_ID);
                logger.info("User Org ID : {}", mdoUserOrgId);
                Map<String, Object> map = (Map<String, Object>) searchProfileApiResp.get(Constants.RESULT);
                Map<String, Object> userSearchResponse = (Map<String, Object>) map.get(Constants.RESPONSE);
                List<Map<String, Object>> contents = (List<Map<String, Object>>) userSearchResponse.get(Constants.CONTENT);
                Set<String> rootOrgIds = new HashSet<>();
                String rootOrgId = null;
                if (!CollectionUtils.isEmpty(contents)) {
                    for (Map<String, Object> content : contents) {
                        rootOrgId = (String) content.get(Constants.ROOT_ORG_ID);
                        rootOrgIds.add(rootOrgId);
                    }
                }
                if (rootOrgIds.size() != 1) {
                    logger.error("Failed to grant access due to different org.");
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Failed to grant access due to different org.");
                }
                StringBuilder uri = new StringBuilder(serverConfig.getSbUrl());
                Map<String, Object> roleRead = (Map<String, Object>) outboundRequestHandlerService.fetchResult(String.valueOf(uri.append(serverConfig.getSbRoleRead()).append(mdoAdminUserId)));
                List<Map<String, Object>> roles = (List<Map<String, Object>>) ((Map<String, Object>) roleRead.get(Constants.RESULT)).get(Constants.ROLES);
                List<String> assignedRoles = new ArrayList<>();
                for (Map<String, Object> roleMap : roles) {
                    assignedRoles.add((String) roleMap.get(Constants.ROLE));
                }
                if (!assignedRoles.contains(Constants.MDO_ADMIN)) {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Grantee doesn't have MDO_ADMIN role");
                }
                assignedRoles.add(Constants.MDO_REPORT_ACCESSOR);
                Map<String, Object> assignRoleReq = new HashMap<>();
                Map<String, Object> roleRequestBody = new HashMap<>();
                roleRequestBody.put(Constants.ORGANIZATION_ID, rootOrgId);
                roleRequestBody.put(Constants.USER_ID, mdoAdminUserId);
                roleRequestBody.put(Constants.ROLES, assignedRoles);
                assignRoleReq.put(Constants.REQUEST, roleRequestBody);
                outboundRequestHandlerService.fetchResultUsingPost(serverConfig.getSbUrl() + serverConfig.getSbAssignRolePath(), assignRoleReq,
                        headersValue);
                Map<String, Object> primaryKeyMap = new HashMap<>();
                primaryKeyMap.put(Constants.ID, mdoAdminUserId);
                long reportExpiryDateMillis = getParsedDate(reportExpiryDate);
                Map<String, Object> keyMap = new HashMap<>();
                keyMap.put(Constants.REPORT_ACCESS_EXPIRY_TABLE, reportExpiryDateMillis);
                cassandraOperation.updateRecord(
                        Constants.KEYSPACE_SUNBIRD, Constants.USER, keyMap, primaryKeyMap);
                response.getResult().put(Constants.STATUS, Constants.SUCCESS);
                response.getResult().put(Constants.MESSAGE, "Report access has been granted successfully");
            }
        } catch (HttpClientErrorException e) {
            logger.error("An exception occurred {}", e.getMessage(), e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.BAD_REQUEST);
        } catch (ParseException | RuntimeException e) {
            logger.error("An exception occurred {}", e.getMessage(), e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @PostConstruct
    public void init() {
        if (storageService == null) {
            storageService = StorageServiceFactory.getStorageService(new StorageConfig(
                    serverProperties.getCloudStorageTypeName(), serverProperties.getCloudStorageKey(),
                    serverProperties.getCloudStorageSecret(), Option.apply(serverProperties.getCloudStorageCephs3Endpoint())));
        }
    }


    /**
     * Downloads a file from storage, processes it, and prepares it for download.
     *
     * @param reportType The type of report.
     * @param date       The date of the report.
     * @param orgId      The organization ID.
     * @param fileName   The name of the file to be downloaded.
     * @return A ResponseEntity containing the file for download.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public ResponseEntity<InputStreamResource> downloadFile(String reportType, String date, String orgId, String fileName) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        try {
            // Construct the object key for downloading from storage
            String objectKey = serverProperties.getReportDownloadFolderName() + "/" + reportType + "/" + date + "/" + orgId + "/" + fileName;
            // Download the file from storage
            storageService.download(serverProperties.getReportDownloadContainerName(), objectKey, Constants.LOCAL_BASE_PATH, Option.apply(Boolean.FALSE));
            // Set the file path
            Path filePath = Paths.get(String.format("%s/%s", Constants.LOCAL_BASE_PATH, fileName));
            // Set headers for the response
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // Prepare password for encryption
            int passwordLength = serverProperties.getZipFilePasswordLength();
            String password = generateAlphanumericPassword(passwordLength);
            headers.add(Constants.PASSWORD, password);
            // Unzip the downloaded file
            String sourceFolderPath = String.format(Constants.STRING_FORMAT_UNZIP, Constants.LOCAL_BASE_PATH, reportType, date, orgId + Constants.OUTPUT_PATH + UUID.randomUUID());
            String destinationFolderPath = sourceFolderPath + Constants.UNZIP_PATH;
            String zipFilePath = String.valueOf(filePath);
            unlockZipFolder(zipFilePath, destinationFolderPath, serverProperties.getUnZipFilePassword());
            // Encrypt the unzipped files and create a new zip file
            createZipFolder(sourceFolderPath, fileName, password);
            // Prepare InputStreamResource for the file to be downloaded
            InputStreamResource inputStreamResource = new InputStreamResource(Files.newInputStream(Paths.get(sourceFolderPath + "/" + fileName)));
            // Clean up temporary files
            removeDirectory(sourceFolderPath);
            // Return ResponseEntity with the file for download
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(Files.size(filePath))
                    .body(inputStreamResource);
        } catch (Exception e) {
            logger.error("Failed to read the downloaded file: " + fileName + ", Exception: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * Creates a zip folder from the files in the specified source folder path.
     *
     * @param sourceFolderPath The path of the source folder.
     * @param fileName         The name of the zip file to be created.
     * @param password         The password for encrypting the zip file.
     */
    public static void createZipFolder(String sourceFolderPath, String fileName, String password) {
        // Initialize an ArrayList to store files to be added to the zip folder
        ArrayList<File> filesToAdd = new ArrayList<>();
        // Retrieve all files from the source folder
        getAllFiles(new File(sourceFolderPath + "/unzippath"), filesToAdd);
        try (ZipFile zipFile = new ZipFile(sourceFolderPath + "/" + fileName)) {
            // Configure zip parameters
            ZipParameters parameters = new ZipParameters();
            if (password != null && !password.isEmpty()) {
                // Set password and encryption method if password is provided
                zipFile.setPassword(password.toCharArray());
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
            }
            // Add files to the zip folder
            zipFile.addFiles(filesToAdd, parameters);
            logger.info("Zip folder created successfully.");
        } catch (IOException e) {
            // Throw exception if an error occurs during zip folder creation
            throw new ZipProcessingException("An error occurred while creating zip folder");
        }
    }



    /**
     * Unlocks a password-protected zip folder and extracts its contents to the specified destination folder path.
     *
     * @param zipFilePath The path of the password-protected zip folder.
     * @param destinationFolderPath The path where the contents of the zip folder will be extracted.
     * @param password The password for unlocking the zip folder
     */
    public static void unlockZipFolder(String zipFilePath, String destinationFolderPath, String password) {
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            // Check if the zip folder is encrypted and a password is provided
            if (zipFile.isEncrypted() && (password != null && !password.isEmpty())) {
                // Set password for decryption
                zipFile.setPassword(password.toCharArray());
            }
            // Extract contents of the zip folder to the destination folder
            zipFile.extractAll(destinationFolderPath);
            logger.info("Zip folder unlocked successfully.");
        } catch (IOException e) {
            // Throw exception if an error occurs during unlocking of the zip folder
            throw new ZipProcessingException("IO Exception while unlocking the zip folder");
        }
    }



    /**
     * Recursively traverses a directory and its subdirectories to collect all files.
     *
     * @param dir The directory to start traversing from.
     * @param fileList The list to which the files will be added.
     */
    public static void getAllFiles(File dir, List<File> fileList) {
        // Get the list of files in the current directory
        File[] files = dir.listFiles();
        // If the directory is not empty
        if (files != null) {
            // Iterate through each file in the directory
            for (File file : files) {
                // If the file is a directory, recursively call getAllFiles to traverse it
                if (file.isDirectory()) {
                    getAllFiles(file, fileList);
                } else { // If the file is a regular file, add it to the fileList
                    fileList.add(file);
                }
            }
        }
    }



    /**
     * Recursively removes a directory and all its contents.
     *
     * @param directoryPath The path of the directory to be removed.
     */
    public static void removeDirectory(String directoryPath) {
        // Get the Path object for the specified directory path
        Path path = Paths.get(directoryPath);
        try (Stream<Path> pathStream = Files.walk(path)) {
            // Traverse all paths in the directory in reverse order to delete inner files first
            pathStream
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            // Attempt to delete each path (file or directory)
                            Files.delete(p);
                        } catch (IOException e) {
                            logger.info("Failed to delete: " + p + " - " + e.getMessage());
                        }
                    });
            // Delete the directory itself if it still exists
            Files.deleteIfExists(path);
            logger.info("Directory removed successfully.");
        } catch (IOException e) {
            logger.info("Failed to remove directory: " + e.getMessage());
        }
    }


    /**
     * Generates a random alphanumeric password of the specified length.
     *
     * @param length The length of the password to generate.
     * @return A randomly generated alphanumeric password.
     */
    public static String generateAlphanumericPassword(int length) {
        // Initialize SecureRandom for generating random numbers
        SecureRandom random = new SecureRandom();
        // StringBuilder to construct the password
        StringBuilder sb = new StringBuilder(length);
        // Loop to generate random characters
        for (int i = 0; i < length; i++) {
            // Generate a random index within the range of alphanumeric characters
            int randomIndex = random.nextInt(ALPHANUMERIC_CHARACTERS.length());
            // Append a randomly selected character to the StringBuilder
            sb.append(ALPHANUMERIC_CHARACTERS.charAt(randomIndex));
        }
        // Return the generated password
        return sb.toString();
    }

    private Map<String, Object> getSearchObject(List<String> userIds) {
        Map<String, Object> requestObject = new HashMap<>();
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> filters = new HashMap<>();
        filters.put(Constants.USER_ID, userIds);
        request.put(Constants.LIMIT, userIds.size());
        request.put(Constants.OFFSET, 0);
        request.put(Constants.FILTERS, filters);
        request.put(Constants.FIELDS_CONSTANT, Arrays.asList(Constants.USER_ID, Constants.STATUS, Constants.CHANNEL, Constants.ROOT_ORG_ID));
        requestObject.put(Constants.REQUEST, request);
        return requestObject;
    }

    private long getParsedDate(String reportExpiryDate) throws ParseException {
        long reportExpiryDateMillis;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date parsedDate = dateFormat.parse(reportExpiryDate);
        reportExpiryDateMillis = parsedDate.getTime();
        return reportExpiryDateMillis;
    }


    public SBApiResponse getFileInfo(String authToken) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.GET_FILE_INFO_OPERATIONAL_REPORTS);
        try {
            logger.info("Inside the getFileInfo()");
            String userId = accessTokenValidator.fetchUserIdFromAccessToken(authToken);
            if (null == userId) {
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg("User Id does not exist");
                return response;
            }
            Map<String, Map<String, String>> userInfoMap = new HashMap<>();
            userUtilityService.getUserDetailsFromDB(
                    Collections.singletonList(userId), Arrays.asList(Constants.ROOT_ORG_ID, Constants.USER_ID), userInfoMap);
            Map<String, String> userDetailsMap = userInfoMap.get(userId);
            String rootOrg = userDetailsMap.get(Constants.ROOT_ORG_ID);
            String objectKey = serverProperties.getOperationalReportFolderName() + "/" + rootOrg + "/" + serverProperties.getOperationReportFileName();
            logger.info("Object key for the operational Reports : " + objectKey);
            Model.Blob blob = storageService.getObject(serverProperties.getReportDownloadContainerName(), objectKey, Option.apply(Boolean.FALSE));
            if (blob != null) {
                logger.info("File details" + blob.lastModified());
                logger.info("File details" + blob.metadata());
                response.put(Constants.LAST_MODIFIED, blob.lastModified());
                response.put(Constants.FILE_METADATA, blob.metadata());
            }
        } catch (Exception e) {
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg("Unable to process the request with the provided authToken");
            return response;
        }
        return response;
    }
}



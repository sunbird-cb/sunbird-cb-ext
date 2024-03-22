package org.sunbird.operationalreports.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.Model;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.AccessTokenValidator;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.config.PropertiesConfig;
import org.sunbird.operationalreports.exception.ZipProcessingException;
import org.sunbird.user.service.UserUtilityService;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import scala.Option;

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
    public SBApiResponse grantReportAccessToMDOAdmin(Map<String, Object> requestBody, String authToken) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.GRANT_REPORT_ACCESS_API);
        try {
            String mdoLeaderUserId = accessTokenValidator.fetchUserIdFromAccessToken(authToken);
            if (StringUtils.isBlank(mdoLeaderUserId)) {
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg("Failed to read user details from access token.");
                return response;
            }
            Map<String, Object> request = (Map<String, Object>) requestBody.get(Constants.REQUEST);
            String mdoAdminUserId = (String) request.get(Constants.USER_ID);
            String reportExpiryDateRequest = (String) request.get(Constants.REPORT_EXPIRY_DATE);
            Map<String, Map<String, String>> userInfoMap = new HashMap<>();
            userUtilityService.getUserDetailsFromDB(
                    Arrays.asList(mdoLeaderUserId, mdoAdminUserId),
                    Arrays.asList(Constants.ROOT_ORG_ID, Constants.USER_ID), userInfoMap);
            String mdoLeaderOrgId = userInfoMap.get(mdoLeaderUserId).get(Constants.ROOT_ORG_ID);
            String mdoAdminOrgId = userInfoMap.get(mdoAdminUserId).get(Constants.ROOT_ORG_ID);
            if (!mdoLeaderOrgId.equalsIgnoreCase(mdoAdminOrgId)) {
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg("Requested User is not belongs to same organisation.");
                return response;
            }
            Map<String, Object> mdoAdminData = userUtilityService.getUsersReadData(mdoAdminUserId, null, null);
            List<String> mdoAdminRoles = (List<String>) mdoAdminData.get(Constants.ROLES);
            if (!mdoAdminRoles.contains(Constants.MDO_ADMIN)) {
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg("Requested User is not MDO_ADMIN.");
                return response;
            }
            if (!mdoAdminRoles.contains(Constants.MDO_REPORT_ACCESSOR)) {
                mdoAdminRoles.add(Constants.MDO_REPORT_ACCESSOR);
                Map<String, Object> assignRoleReq = new HashMap<>();
                Map<String, Object> roleRequestBody = new HashMap<>();
                roleRequestBody.put(Constants.ORGANIZATION_ID, mdoLeaderOrgId);
                roleRequestBody.put(Constants.USER_ID, mdoAdminUserId);
                roleRequestBody.put(Constants.ROLES, mdoAdminRoles);
                assignRoleReq.put(Constants.REQUEST, roleRequestBody);

                Map<String, Object> assignRoleResp = outboundRequestHandlerService.fetchResultUsingPost(
                        serverConfig.getSbUrl() + serverConfig.getSbAssignRolePath(), assignRoleReq,
                        null);
                if (!Constants.OK.equalsIgnoreCase((String) assignRoleResp.get(Constants.RESPONSE_CODE))) {
                    logger.error("Failed to assign MDO_REPORT_ACCESSOR role for user. Response : %s",
                            (new ObjectMapper()).writeValueAsString(assignRoleResp));
                    response.getParams().setStatus(Constants.FAILED);
                    response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    response.getResult().put(Constants.MESSAGE, "Failed to assign MDO_REPORT_ACCESSOR role for user.");
                    return response;
                }
            }

            Map<String, Object> dbResponse = upsertReportAccessExpiry(mdoAdminUserId, mdoLeaderOrgId,
                    reportExpiryDateRequest);
            if (Constants.SUCCESS.equalsIgnoreCase((String) dbResponse.get(Constants.RESPONSE))) {
                response.getResult().put(Constants.STATUS, Constants.SUCCESS);
            } else {
                response.getParams().setStatus(Constants.FAILED);
                response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
                response.getResult().put(Constants.MESSAGE, "Failed to update DB record.");
            }
        } catch (Exception e) {
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
                    serverProperties.getCloudStorageSecret(),
                    Option.apply(serverProperties.getCloudStorageCephs3Endpoint())));
        }
    }

    @Override
    public ResponseEntity<InputStreamResource> downloadFile(String authToken) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        String sourceFolderPath = null;
        try {
            String userId = accessTokenValidator.fetchUserIdFromAccessToken(authToken);
            if (null == userId) {
                throw new Exception("User Id does not exist");
            }
            Map<String, Map<String, String>> userInfoMap = new HashMap<>();
            userUtilityService.getUserDetailsFromDB(
                    Collections.singletonList(userId), Arrays.asList(Constants.ROOT_ORG_ID, Constants.USER_ID),
                    userInfoMap);
            Map<String, String> userDetailsMap = userInfoMap.get(userId);
            String rootOrg = userDetailsMap.get(Constants.ROOT_ORG_ID);
            String objectKey = serverProperties.getOperationalReportFolderName() + "/mdoid=" + rootOrg + "/"
                    + serverProperties.getOperationReportFileName();
            // Download the file from storage
            storageService.download(serverProperties.getReportDownloadContainerName(), objectKey,
                    Constants.LOCAL_BASE_PATH, Option.apply(Boolean.FALSE));
            // Set the file path
            Path filePath = Paths.get(Constants.LOCAL_BASE_PATH + serverProperties.getOperationReportFileName());
            // Set headers for the response
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + serverProperties.getOperationReportFileName() + "\"");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // Prepare password for encryption
            int passwordLength = serverProperties.getZipFilePasswordLength();
            String password = generateAlphanumericPassword(passwordLength);
            headers.add(Constants.PASSWORD, password);
            // Unzip the downloaded file
            sourceFolderPath = String.format("%s/%s/%s/%s", Constants.LOCAL_BASE_PATH, rootOrg,
                    Constants.OUTPUT_PATH, UUID.randomUUID());
            String destinationFolderPath = sourceFolderPath + Constants.UNZIP_PATH;
            String zipFilePath = String.valueOf(filePath);
            unlockZipFolder(zipFilePath, destinationFolderPath, serverProperties.getUnZipFilePassword());
            // Encrypt the unzipped files and create a new zip file
            createZipFolder(sourceFolderPath, serverProperties.getOperationReportFileName(), password);
            // Prepare InputStreamResource for the file to be downloaded
            InputStreamResource inputStreamResource = new InputStreamResource(Files
                    .newInputStream(Paths.get(sourceFolderPath + "/" + serverProperties.getOperationReportFileName())));
            // Return ResponseEntity with the file for download
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(Files.size(Paths.get(sourceFolderPath + "/" + serverProperties.getOperationReportFileName())))
                    .body(inputStreamResource);
        } catch (Exception e) {
            logger.error("Failed to read the downloaded file: " + serverProperties.getOperationReportFileName()
                    + ", Exception: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }  finally {
            if (sourceFolderPath != null) {
                try {
                    removeDirectory(String.valueOf(Paths.get(sourceFolderPath)));
                } catch (InvalidPathException e) {
                    logger.error("Failed to delete the file: " + sourceFolderPath + ", Exception: ", e);
                }
            }
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
     * Unlocks a password-protected zip folder and extracts its contents to the
     * specified destination folder path.
     *
     * @param zipFilePath           The path of the password-protected zip folder.
     * @param destinationFolderPath The path where the contents of the zip folder
     *                              will be extracted.
     * @param password              The password for unlocking the zip folder
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
     * Recursively traverses a directory and its subdirectories to collect all
     * files.
     *
     * @param dir      The directory to start traversing from.
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
            // Traverse all paths in the directory in reverse order to delete inner files
            // first
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
        request.put(Constants.FIELDS_CONSTANT,
                Arrays.asList(Constants.USER_ID, Constants.STATUS, Constants.CHANNEL, Constants.ROOT_ORG_ID));
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
                    Collections.singletonList(userId), Arrays.asList(Constants.ROOT_ORG_ID, Constants.USER_ID),
                    userInfoMap);
            Map<String, String> userDetailsMap = userInfoMap.get(userId);
            String rootOrg = userDetailsMap.get(Constants.ROOT_ORG_ID);
            String objectKey = serverProperties.getOperationalReportFolderName() + "/mdoid=" + rootOrg + "/"
                    + serverProperties.getOperationReportFileName();
            logger.info("Object key for the operational Reports : " + objectKey);
            Model.Blob blob = storageService.getObject(serverProperties.getReportDownloadContainerName(), objectKey,
                    Option.apply(Boolean.FALSE));
            if (blob != null) {
                logger.info("File details" + blob.lastModified());
                logger.info("File details" + blob.metadata());
                response.put(Constants.LAST_MODIFIED, blob.lastModified());
                response.put(Constants.FILE_METADATA, blob.metadata());
            }
        } catch (Exception e) {
            logger.error("Failed to get the report file information. Exception: ", e);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg("Failed to get the report file information");
            return response;
        }
        return response;
    }

    public SBApiResponse readGrantAccess(String authToken, boolean isAdminAPI) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.READ_REPORT_ACCESS_API);
        String userId = accessTokenValidator.fetchUserIdFromAccessToken(authToken);
        if (StringUtils.isBlank(userId)) {
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg("Failed to read user details from access token.");
            return response;
        }
        Map<String, Map<String, String>> userInfoMap = new HashMap<>();
        userUtilityService.getUserDetailsFromDB(
                Arrays.asList(userId),
                Arrays.asList(Constants.ROOT_ORG_ID, Constants.USER_ID), userInfoMap);
        String userOrgId = userInfoMap.get(userId).get(Constants.ROOT_ORG_ID);
        if (StringUtils.isBlank(userOrgId)) {
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg("Requested User is not belongs to same organisation.");
            return response;
        }

        Map<String, Object> primaryKeyMap = new HashMap<>();
        primaryKeyMap.put(Constants.ORG_ID, userOrgId);
        if (isAdminAPI) {
            primaryKeyMap.put(Constants.USER_ID, userId);
        }
        Map<String, Object> userAccessReportExpiryDetails = getReportAccessDetails(primaryKeyMap);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : userAccessReportExpiryDetails.entrySet()) {
            Map<String, Object> mdoIdAccessDetails = new HashMap<>();
            mdoIdAccessDetails.put(Constants.USER_ID, entry.getKey());
            mdoIdAccessDetails.put(Constants.REPORT_ACCESS_EXPIRY,
                    ((Map<String, Object>) entry.getValue()).get(Constants.USER_REPORT_EXPIRY_DATE));
            result.add(mdoIdAccessDetails);
        }
        response.getResult().put(Constants.STATUS, Constants.SUCCESS);
        response.getResult().put(Constants.RESPONSE, result);
        return response;
    }

    private Map<String, Object> upsertReportAccessExpiry(String mdoAdminUserId, String rootOrgId,
            String reportExpiryDate)
            throws ParseException {
        Map<String, Object> primaryKeyMap = new HashMap<>();
        primaryKeyMap.put(Constants.USER_ID_LOWER, mdoAdminUserId);
        primaryKeyMap.put(Constants.ORG_ID, rootOrgId);
        Date reportExpiryDateMillis = parseDateFromString(reportExpiryDate);
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(Constants.USER_REPORT_EXPIRY_DATE, reportExpiryDateMillis);
        return cassandraOperation.updateRecord(
                Constants.KEYSPACE_SUNBIRD, Constants.REPORT_ACCESS_EXPIRY_TABLE, keyMap, primaryKeyMap);
    }

    private Map<String, Object> getReportAccessDetails(Map<String, Object> primaryKeyMap) {
        return cassandraOperation.getRecordsByPropertiesByKey(Constants.KEYSPACE_SUNBIRD,
                Constants.REPORT_ACCESS_EXPIRY_TABLE, primaryKeyMap,
                Arrays.asList(Constants.USER_REPORT_EXPIRY_DATE, Constants.USER_ID), Constants.USER_ID);
    }

    private Date parseDateFromString(String dateString) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return Date.from(offsetDateTime.toInstant());
    }
}

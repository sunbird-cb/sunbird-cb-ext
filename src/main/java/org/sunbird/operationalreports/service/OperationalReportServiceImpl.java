package org.sunbird.operationalreports.service;


import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.cloud.storage.BaseStorageService;
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
import java.util.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
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
               String mdoLeaderUserId = accessTokenValidator.fetchUserIdFromAccessToken(authToken);
               if (StringUtils.isBlank(mdoLeaderUserId)) {
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                    response.getParams().setStatus(Constants.FAILED);
                    response.getParams().setErrmsg("Invalid UserId in the request");
                    return response;
                }
                Map<String, Object> mdoAdminDetails = (Map<String, Object>) request.getRequest();
                String mdoAdminUserId = (String) mdoAdminDetails.get(Constants.USER_ID);
                String reportExpiryDateRequest = (String) mdoAdminDetails.get(Constants.REPORT_EXPIRY_DATE);
                Map<String, String> headersValue = new HashMap<>();
                headersValue.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                Map<String, Map<String, String>> userInfoMap = new HashMap<>();
                userUtilityService.getUserDetailsFromDB(
                       Arrays.asList(mdoLeaderUserId, mdoAdminUserId), Arrays.asList("rootOrgId","userId"),userInfoMap );
                logger.info("MDO Leader User ID : {}", mdoLeaderUserId);
                String mdoLeaderOrgId = userInfoMap.get(mdoLeaderUserId).get("rootOrgId");
                String mdoAdminOrgId = userInfoMap.get(mdoAdminUserId).get("rootOrgId");
                if(!mdoLeaderOrgId.equalsIgnoreCase(mdoAdminOrgId)) {
                        response.setResponseCode(HttpStatus.BAD_REQUEST);
                        response.getParams().setStatus(Constants.FAILED);
                        response.getParams().setErrmsg("Invalid UserId in the request");
                        return response;
                }
                Map<String, Object> searchProfileApiResp = new HashMap<>();
                if (searchProfileApiResp != null) {
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
                    Map<String, Object> mdoAdminData = userUtilityService.getUsersReadData(mdoAdminUserId, null, null);
                    List<String> mdoAdminRoles = (List<String>) mdoAdminData.get(Constants.ROLES);
                    if (!mdoAdminRoles.contains(Constants.MDO_ADMIN)) {
                        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Grantee doesn't have MDO_ADMIN role");
                    }
                    if (mdoAdminRoles.contains(Constants.MDO_REPORT_ACCESSOR)) {
                        Map<String, Object> mdoReportAccessDetails = (Map<String, Object>) getReportAccessDetails(userOrgId).get(mdoAdminUserId);
                        Date expiryDate = (Date) mdoReportAccessDetails.get(Constants.USER_REPORT_EXPIRY_DATE);
                        if (expiryDate.compareTo(parseDateFromString(reportExpiryDateRequest)) == 0) {
                            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Grantee already has MDO_REPORT_ACCESSOR role till : " + reportExpiryDateRequest);
                        }
                        this.upsertReportAccessExpiry(mdoAdminUserId, rootOrgId, reportExpiryDateRequest);
                        response.getResult().put(Constants.STATUS, Constants.SUCCESS);
                        response.getResult().put(Constants.MESSAGE, "Report access has been granted successfully");
                        return response;
                    }
                    mdoAdminRoles.add(Constants.MDO_REPORT_ACCESSOR);
                    Map<String, Object> assignRoleReq = new HashMap<>();
                    Map<String, Object> roleRequestBody = new HashMap<>();
                    roleRequestBody.put(Constants.ORGANIZATION_ID, rootOrgId);
                    roleRequestBody.put(Constants.USER_ID, mdoAdminUserId);
                    roleRequestBody.put(Constants.ROLES, mdoAdminRoles);
                    assignRoleReq.put(Constants.REQUEST, roleRequestBody);
                    outboundRequestHandlerService.fetchResultUsingPost(serverConfig.getSbUrl() + serverConfig.getSbAssignRolePath(), assignRoleReq,
                            headersValue);
                    Map<String, Object> primaryKeyMap = new HashMap<>();
                    primaryKeyMap.put(Constants.USER_ID_LOWER, mdoAdminUserId);
                    primaryKeyMap.put(Constants.USER_ORG_ID, rootOrgId);
                    Date reportExpiryDate = parseDateFromString(reportExpiryDateRequest);
                    primaryKeyMap.put(Constants.USER_REPORT_EXPIRY_DATE, reportExpiryDate);
                    cassandraOperation.insertRecord(
                            Constants.KEYSPACE_SUNBIRD, Constants.REPORT_ACCESS_EXPIRY_TABLE, primaryKeyMap);
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

    public SBApiResponse readGrantAccess(String authToken,String userOrgId) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.READ_REPORT_ACCESS_API);
        Map<String, Object> primaryKeyMap = new HashMap<>();
        primaryKeyMap.put(Constants.USER_ORG_ID,userOrgId);
        Map<String, Object> userAccessReportExpiryDetails = getReportAccessDetails(userOrgId);
        List<Map<String, Object>> result = new ArrayList<>();
        for(Map.Entry<String, Object> entry:userAccessReportExpiryDetails.entrySet()){
            Map<String, Object> mdoIdAccessDetails = new HashMap<>();
            mdoIdAccessDetails.put(Constants.USER_ID,entry.getKey());
            mdoIdAccessDetails.put(Constants.REPORT_ACCESS_EXPIRY, ((Map<String, Object>) entry.getValue()).get(Constants.USER_REPORT_EXPIRY_DATE));
            result.add(mdoIdAccessDetails);
        }
        response.getResult().put(Constants.STATUS, Constants.SUCCESS);
        response.getResult().put(Constants.RESPONSE, result);
        return response;
    }

    private Map<String, Object> getReportAccessDetails(String userOrgId) {
        Map<String, Object> primaryKeyMap = new HashMap<>();
        primaryKeyMap.put(Constants.USER_ORG_ID, userOrgId);
        return cassandraOperation.getRecordsByPropertiesByKey(Constants.KEYSPACE_SUNBIRD,
                Constants.REPORT_ACCESS_EXPIRY_TABLE, primaryKeyMap, Arrays.asList(Constants.USER_REPORT_EXPIRY_DATE, Constants.USER_ID), Constants.USER_ID);
    }

    private void upsertReportAccessExpiry(String mdoAdminUserId, String rootOrgId, String reportExpiryDate) throws ParseException {
        Map<String, Object> primaryKeyMap = new HashMap<>();
        primaryKeyMap.put(Constants.USER_ID_LOWER, mdoAdminUserId);
        primaryKeyMap.put(Constants.USER_ORG_ID, rootOrgId);
        Date reportExpiryDateMillis = parseDateFromString(reportExpiryDate);
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(Constants.USER_REPORT_EXPIRY_DATE, reportExpiryDateMillis);
        cassandraOperation.updateRecord(
                Constants.KEYSPACE_SUNBIRD, Constants.REPORT_ACCESS_EXPIRY_TABLE, keyMap, primaryKeyMap);
    }

    public Date parseDateFromString(String dateString) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return Date.from(offsetDateTime.toInstant());
    }
}



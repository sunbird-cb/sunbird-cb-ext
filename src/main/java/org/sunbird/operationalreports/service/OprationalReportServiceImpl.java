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
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.operationalreports.exception.ZipProcessingException;
import scala.Option;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Deepak kumar Thakur & Mahesh R V
 */
@Service
public class OprationalReportServiceImpl implements OperationalReportService {
    private static final Logger logger = LoggerFactory.getLogger(OprationalReportServiceImpl.class);

    private static final String ALPHANUMERIC_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Autowired
    CbExtServerProperties serverProperties;

    private BaseStorageService storageService = null;

    public SBApiResponse grantAccessToMDOAdmin(String userOrgId, String authToken) {
        //Need to Implimented
        return null;
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
            String sourceFolderPath = String.format("%s/%s/%s/%s", Constants.LOCAL_BASE_PATH, reportType, date, orgId + "/output");
            String destinationFolderPath = sourceFolderPath + "/unzippath";
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
            // Log error if download or processing fails
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
            // Log success message
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
            // Log success message
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
                            // Log a message if deletion fails for a specific path
                            logger.info("Failed to delete: " + p + " - " + e.getMessage());
                        }
                    });
            // Delete the directory itself if it still exists
            Files.deleteIfExists(path);
            // Log success message
            logger.info("Directory removed successfully.");
        } catch (IOException e) {
            // Log an error message if an exception occurs during directory removal
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

}



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
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.operationalreports.exception.ZipProcessingException;
import scala.Option;

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
 * @author Deepak kumar Thakur
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

    /**
     * @param reportType
     * @param date
     * @param orgId
     * @param fileName
     * @return
     */
    @Override
    public ResponseEntity<InputStreamResource> downloadFile(String reportType, String date, String orgId, String fileName) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        try {
            /**
             *
             * s3://igot/zipReports/mount/data/analytics/reports/zip/2024-02-06/zipReports2.zip
             * standalone-reports/reportType/date/orgId/response.json
             */
            String objectKey = serverProperties.getReportDownloadFolderName() + "/" + reportType + "/" + date + "/" + orgId + "/" + fileName;
            logger.info("Local Base Path Value is : " +  Constants.LOCAL_BASE_PATH);
            logger.info("Get Download Folder Name value is : " +  serverProperties.getReportDownloadFolderName());
            logger.info("Get Download Container Name value is : " +  serverProperties.getReportDownloadContainerName());
            logger.info("Object key value is : " + objectKey);
            storageService.download(serverProperties.getReportDownloadContainerName(), objectKey, Constants.LOCAL_BASE_PATH, Option.apply(Boolean.FALSE));
            Path filePath = Paths.get(String.format("%s/%s", Constants.LOCAL_BASE_PATH, fileName));
            logger.info("filePath value is : " + filePath);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String sourceFolderPath = String.format("%s/%s/%s/%s", Constants.LOCAL_BASE_PATH, reportType, date, orgId + "/output");
            logger.info("sourceFolderPath value is : " + sourceFolderPath);
            String destinationFolderPath = sourceFolderPath + "/unzippath";
            logger.info("destinationFolderPath value is : " + destinationFolderPath);
            String zipFilePath = String.valueOf(filePath);
            logger.info("zipFilePath value is : " + zipFilePath);
            int passwordLength = 15;
            String password = generateAlphanumericPassword(passwordLength);
            headers.add("password", password);
            unlockZipFolder(zipFilePath, destinationFolderPath, "123456");
            createZipFolder(sourceFolderPath, fileName, password);
            InputStreamResource inputStreamResource = new InputStreamResource(Files.newInputStream(Paths.get(sourceFolderPath + "/" + fileName)));
            removeDirectory(sourceFolderPath);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(Files.size(filePath))
                    .body(inputStreamResource);
        } catch (Exception e) {
            logger.error("Failed to read the downloaded file: " + fileName + ", Exception: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public static void createZipFolder(String sourceFolderPath, String fileName, String password) {
        ArrayList<File> filesToAdd = new ArrayList<>();
        getAllFiles(new File(sourceFolderPath + "/unzippath"), filesToAdd);
        try (ZipFile zipFile = new ZipFile(sourceFolderPath + "/" + fileName)) {
            ZipParameters parameters = new ZipParameters();
            if (password != null && !password.isEmpty()) {
                zipFile.setPassword(password.toCharArray());
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
            }
            zipFile.addFiles(filesToAdd, parameters);
            logger.info("Zip folder created successfully.");
        } catch (IOException e) {
            throw new ZipProcessingException("An error occurred while creating zip folder");
        }
    }


    public static void unlockZipFolder(String zipFilePath, String destinationFolderPath, String password) {
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            if (zipFile.isEncrypted() && (password != null && !password.isEmpty())) {
                zipFile.setPassword(password.toCharArray());
            }
            zipFile.extractAll(destinationFolderPath);
            logger.info("Zip folder unlocked successfully.");
        } catch (IOException e) {
            throw new ZipProcessingException("IO Exception while unlocking the zip folder");
        }
    }


    public static void getAllFiles(File dir, List<File> fileList) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    getAllFiles(file, fileList);
                } else {
                    fileList.add(file);
                }
            }
        }
    }


    public static void removeDirectory(String directoryPath) {
        Path path = Paths.get(directoryPath);
        try (Stream<Path> pathStream = Files.walk(path)) {
            pathStream
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            logger.info("Failed to delete: " + p + " - " + e.getMessage());
                        }
                    });
            Files.deleteIfExists(path);
            logger.info("Directory removed successfully.");
        } catch (IOException e) {
            logger.info("Failed to remove directory: " + e.getMessage());
        }
    }

    public static String generateAlphanumericPassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALPHANUMERIC_CHARACTERS.length());
            sb.append(ALPHANUMERIC_CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }
}



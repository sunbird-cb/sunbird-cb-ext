package org.sunbird.profile.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.storage.service.StorageService;
import org.sunbird.user.registration.model.UserRegistration;
import org.sunbird.user.service.UserUtilityService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Service
public class UserBulkUploadService {
    private Logger logger = LoggerFactory.getLogger(UserBulkUploadService.class);
    ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    CbExtServerProperties serverProperties;
    @Autowired
    UserUtilityService userUtilityService;
    @Autowired
    CassandraOperation cassandraOperation;

    @Autowired
    StorageService storageService;

    public void initiateUserBulkUploadProcess(String inputData) {
        logger.info("UserBulkUploadService:: initiateUserBulkUploadProcess: Started");
        long duration = 0;
        long startTime = System.currentTimeMillis();
        try {
            HashMap<String, String> inputDataMap = objectMapper.readValue(inputData,
                    new TypeReference<HashMap<String, String>>() {
                    });
            List<String> errList = validateReceivedKafkaMessage(inputDataMap);
            if (errList.isEmpty()) {
                updateUserBulkUploadStatus(inputDataMap.get(Constants.ROOT_ORG_ID),
                        inputDataMap.get(Constants.IDENTIFIER), Constants.STATUS_IN_PROGRESS_UPPERCASE, 0, 0, 0);
                storageService.downloadFile(inputDataMap.get(Constants.FILE_NAME));
                processBulkUpload(inputDataMap);
            } else {
                logger.error(String.format("Error in the Kafka Message Received : %s", errList));
            }
        } catch (Exception e) {
            logger.error(String.format("Error in the scheduler to upload bulk users %s", e.getMessage()),
                    e);
        }
        duration = System.currentTimeMillis() - startTime;
        logger.info("UserBulkUploadService:: initiateUserBulkUploadProcess: Completed. Time taken: "
                + duration + " milli-seconds");
    }

    public void updateUserBulkUploadStatus(String rootOrgId, String identifier, String status, int totalRecordsCount,
                                           int successfulRecordsCount, int failedRecordsCount) {
        try {
            Map<String, Object> compositeKeys = new HashMap<>();
            compositeKeys.put(Constants.ROOT_ORG_ID_LOWER, rootOrgId);
            compositeKeys.put(Constants.IDENTIFIER, identifier);
            Map<String, Object> fieldsToBeUpdated = new HashMap<>();
            if (!status.isEmpty()) {
                fieldsToBeUpdated.put(Constants.STATUS, status);
            }
            if (totalRecordsCount >= 0) {
                fieldsToBeUpdated.put(Constants.TOTAL_RECORDS, totalRecordsCount);
            }
            if (successfulRecordsCount >= 0) {
                fieldsToBeUpdated.put(Constants.SUCCESSFUL_RECORDS_COUNT, successfulRecordsCount);
            }
            if (failedRecordsCount >= 0) {
                fieldsToBeUpdated.put(Constants.FAILED_RECORDS_COUNT, failedRecordsCount);
            }
            fieldsToBeUpdated.put(Constants.DATE_UPDATE_ON, new Timestamp(System.currentTimeMillis()));
            cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_USER_BULK_UPLOAD,
                    fieldsToBeUpdated, compositeKeys);
        } catch (Exception e) {
            logger.error(String.format("Error in Updating User Bulk Upload Status in Cassandra %s", e.getMessage()), e);
        }
    }

    private void processBulkUpload(HashMap<String, String> inputDataMap) throws IOException {
        File file = null;
        FileInputStream fis = null;
        XSSFWorkbook wb = null;
        int totalRecordsCount = 0;
        int noOfSuccessfulRecords = 0;
        int failedRecordsCount = 0;
        String status = "";
        String phone = "";
        try {
            file = new File(Constants.LOCAL_BASE_PATH + inputDataMap.get(Constants.FILE_NAME));
            if (file.exists() && file.length() > 0) {
                fis = new FileInputStream(file);
                wb = new XSSFWorkbook(fis);
                XSSFSheet sheet = wb.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.iterator();
                // incrementing the iterator inorder to skip the headers in the first row
                if (rowIterator.hasNext()) {
                    Row firstRow = rowIterator.next();
                    Cell statusCell = firstRow.getCell(7);
                    Cell errorDetails = firstRow.getCell(8);
                    if (statusCell == null) {
                        statusCell = firstRow.createCell(7);
                    }
                    if (errorDetails == null) {
                        errorDetails = firstRow.createCell(8);
                    }
                    statusCell.setCellValue("Status");
                    errorDetails.setCellValue("Error Details");
                }
                int count = 0;
                while (rowIterator.hasNext()) {
                    logger.info("UserBulkUploadService:: Record " + count++);
                    long duration = 0;
                    long startTime = System.currentTimeMillis();
                    StringBuffer str = new StringBuffer();
                    List<String> errList = new ArrayList<>();
                    List<String> invalidErrList = new ArrayList<>();
                    Row nextRow = rowIterator.next();
                    UserRegistration userRegistration = new UserRegistration();
                    if (nextRow.getCell(0) == null || nextRow.getCell(0).getCellType() == CellType.BLANK) {
                        errList.add("Full Name");
                    } else {
                        if (nextRow.getCell(0).getCellType() == CellType.STRING) {
                            userRegistration.setFirstName(nextRow.getCell(0).getStringCellValue().trim());
                            if (!ProjectUtil.validateFullName(userRegistration.getFirstName())) {
                                invalidErrList.add("Invalid Full Name");
                            }
                        } else {
                            invalidErrList.add("Invalid column type. Expecting string format");
                        }
                    }
                    if (nextRow.getCell(1) == null || nextRow.getCell(1).getCellType() == CellType.BLANK) {
                        errList.add("Email");
                    } else {
                        if (nextRow.getCell(1).getCellType() == CellType.STRING) {
                            userRegistration.setEmail(nextRow.getCell(1).getStringCellValue().trim());
                        } else {
                            invalidErrList.add("Invalid column type. Expecting string format");
                        }
                    }
                    if (nextRow.getCell(2) == null || nextRow.getCell(2).getCellType() == CellType.BLANK) {
                        errList.add("Phone");
                    } else {
                        if (nextRow.getCell(2).getCellType() == CellType.NUMERIC) {
                            phone = NumberToTextConverter.toText(nextRow.getCell(2).getNumericCellValue());
                            userRegistration.setPhone(phone.trim());
                        } else if (nextRow.getCell(2).getCellType() == CellType.STRING) {
                            userRegistration.setPhone(nextRow.getCell(2).getStringCellValue().trim());
                        } else {
                            invalidErrList.add("Invalid column type. Expecting number/string format");
                        }
                    }
                    if (nextRow.getCell(3) == null || nextRow.getCell(3).getCellType() == CellType.BLANK) {
                        errList.add("Group");
                    } else {
                        if (nextRow.getCell(3).getCellType() == CellType.STRING) {
                            userRegistration.setGroup(nextRow.getCell(3).getStringCellValue().trim());
                            if (!userUtilityService.validateGroup(userRegistration.getGroup())) {
                                invalidErrList.add("Invalid Group : Group can be only among one of these " + serverProperties.getBulkUploadGroupValue());
                            }
                        } else {
                            invalidErrList.add("Invalid column type. Expecting string format");
                        }
                    }
                    if (nextRow.getCell(4) != null && nextRow.getCell(4).getCellType() != CellType.BLANK) {
                        if (nextRow.getCell(4).getCellType() == CellType.STRING) {
                            String tagStr = nextRow.getCell(4).getStringCellValue().trim();
                            List<String> tagList = new ArrayList<String>();
                            if (!StringUtils.isEmpty(tagStr)) {
                                String[] tagStrList = tagStr.split(",", -1);
                                for(String tag : tagStrList) {
                                    tagList.add(tag.trim());
                                }
                            }
                            userRegistration.setTag(tagList);
                            if (!ProjectUtil.validateTag(userRegistration.getTag())) {
                                invalidErrList.add("Invalid Tag : Tags are comma seperated string values. A Tag can contain only alphabets with spaces. eg: Bihar Circle, Patna Division");
                            }
                        } else {
                            invalidErrList.add("Invalid column type. Expecting string format");
                        }
                    }
                    if (nextRow.getCell(5) != null && nextRow.getCell(5).getCellType() != CellType.BLANK) {
                        if (nextRow.getCell(5).getCellType() == CellType.NUMERIC) {
                            userRegistration.setExternalSystemId(NumberToTextConverter.toText(nextRow.getCell(5).getNumericCellValue()).trim());
                            if (!ProjectUtil.validateExternalSystemId(userRegistration.getExternalSystemId())) {
                                invalidErrList.add("Invalid External System ID : External System Id can contain alphanumeric characters and have a max length of 30");
                            }
                        } else if (nextRow.getCell(5).getCellType() == CellType.STRING) {
                            userRegistration.setExternalSystemId(nextRow.getCell(5).getStringCellValue().trim());
                            if (!ProjectUtil.validateExternalSystemId(userRegistration.getExternalSystemId())) {
                                invalidErrList.add("Invalid External System ID : External System Id can contain alphanumeric characters and have a max length of 30");
                            }
                        } else {
                            invalidErrList.add("Invalid column type. Expecting string/number format");
                        }
                    }
                    if (nextRow.getCell(6) != null && !StringUtils.isBlank(nextRow.getCell(6).toString())) {
                        if (nextRow.getCell(6).getCellType() == CellType.STRING) {
                            userRegistration.setExternalSystem(nextRow.getCell(6).getStringCellValue().trim());
                            if (!ProjectUtil.validateExternalSystem(userRegistration.getExternalSystem())) {
                                invalidErrList.add("Invalid External System : External System can contain only alphabets and can have a max length of 255");
                            }
                        } else {
                            invalidErrList.add("Invalid column type. Expecting string format");
                        }
                    }
                    userRegistration.setOrgName(inputDataMap.get(Constants.ORG_NAME));
                    userRegistration.setChannel(inputDataMap.get(Constants.ORG_NAME));
                    userRegistration.setSbOrgId(inputDataMap.get(Constants.ROOT_ORG_ID));
                    Cell statusCell = nextRow.getCell(7);
                    Cell errorDetails = nextRow.getCell(8);
                    if (statusCell == null) {
                        statusCell = nextRow.createCell(7);
                    }
                    if (errorDetails == null) {
                        errorDetails = nextRow.createCell(8);
                    }
                    if (totalRecordsCount == 0 && errList.size() == 4) {
                        setErrorDetails(str, errList, statusCell, errorDetails);
                        failedRecordsCount++;
                        break;
                    } else if (totalRecordsCount > 0 && errList.size() == 4) {
                        break;
                    }
                    totalRecordsCount++;
                    if (!errList.isEmpty()) {
                        setErrorDetails(str, errList, statusCell, errorDetails);
                        failedRecordsCount++;
                    } else {
                        invalidErrList.addAll(validateEmailContactAndDomain(userRegistration));
                        if (invalidErrList.isEmpty()) {
                            userRegistration.setUserAuthToken(inputDataMap.get(Constants.X_AUTH_TOKEN));
                            String responseCode = userUtilityService.createBulkUploadUser(userRegistration);
                            if (!Constants.OK.equalsIgnoreCase(responseCode)) {
                                failedRecordsCount++;
                                statusCell.setCellValue(Constants.FAILED_UPPERCASE);
                                errorDetails.setCellValue(responseCode);
                            } else {
                                noOfSuccessfulRecords++;
                                statusCell.setCellValue(Constants.SUCCESS_UPPERCASE);
                                errorDetails.setCellValue("");
                            }
                        } else {
                            failedRecordsCount++;
                            statusCell.setCellValue(Constants.FAILED_UPPERCASE);
                            errorDetails.setCellValue(invalidErrList.toString());
                        }
                    }
                    duration = System.currentTimeMillis() - startTime;
                    logger.info("UserBulkUploadService:: Record Completed. Time taken: "
                            + duration + " milli-seconds");
                }
                if (totalRecordsCount == 0) {
                    XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
                    Cell statusCell = row.createCell(7);
                    Cell errorDetails = row.createCell(8);
                    statusCell.setCellValue(Constants.FAILED_UPPERCASE);
                    errorDetails.setCellValue(Constants.EMPTY_FILE_FAILED);

                }
                status = uploadTheUpdatedFile(file, wb);
                if (!(Constants.SUCCESSFUL.equalsIgnoreCase(status) && failedRecordsCount == 0
                        && totalRecordsCount == noOfSuccessfulRecords && totalRecordsCount >= 1)) {
                    status = Constants.FAILED_UPPERCASE;
                }
            } else {
                logger.info("Error in Process Bulk Upload : The File is not downloaded/present");
                status = Constants.FAILED_UPPERCASE;
            }
            updateUserBulkUploadStatus(inputDataMap.get(Constants.ROOT_ORG_ID), inputDataMap.get(Constants.IDENTIFIER),
                    status, totalRecordsCount, noOfSuccessfulRecords, failedRecordsCount);
        } catch (Exception e) {
            logger.error(String.format("Error in Process Bulk Upload %s", e.getMessage()), e);
            updateUserBulkUploadStatus(inputDataMap.get(Constants.ROOT_ORG_ID), inputDataMap.get(Constants.IDENTIFIER),
                    Constants.FAILED_UPPERCASE, 0, 0, 0);
        } finally {
            if (wb != null)
                wb.close();
            if (fis != null)
                fis.close();
            if (file != null)
                file.delete();
        }
    }

    private void setErrorDetails(StringBuffer str, List<String> errList, Cell statusCell, Cell errorDetails) {
        str.append("Failed to process user record. Missing Parameters - ").append(errList);
        statusCell.setCellValue(Constants.FAILED_UPPERCASE);
        errorDetails.setCellValue(str.toString());
    }

    private String uploadTheUpdatedFile(File file, XSSFWorkbook wb)
            throws IOException {
        FileOutputStream fileOut = new FileOutputStream(file);
        wb.write(fileOut);
        fileOut.close();
        SBApiResponse uploadResponse = storageService.uploadFile(file, serverProperties.getBulkUploadContainerName(),serverProperties.getCloudContainerName());
        if (!HttpStatus.OK.equals(uploadResponse.getResponseCode())) {
            logger.info(String.format("Failed to upload file. Error: %s",
                    uploadResponse.getParams().getErrmsg()));
            return Constants.FAILED_UPPERCASE;
        }
        return Constants.SUCCESSFUL_UPPERCASE;
    }

    private List<String> validateEmailContactAndDomain(UserRegistration userRegistration) {
        StringBuffer str = new StringBuffer();
        List<String> errList = new ArrayList<>();
        if (!ProjectUtil.validateEmailPattern(userRegistration.getEmail())) {
            errList.add("Invalid Email Id");
        }
        if (!ProjectUtil.validateContactPattern(userRegistration.getPhone())) {
            errList.add("Invalid Mobile Number");
        }
        if (!errList.isEmpty()) {
            str.append("Failed to Validate User Details. Error Details - [").append(errList).append("]");
        }
        return errList;
    }

    private List<String> validateReceivedKafkaMessage(HashMap<String, String> inputDataMap) {
        StringBuffer str = new StringBuffer();
        List<String> errList = new ArrayList<>();
        if (StringUtils.isEmpty(inputDataMap.get(Constants.ROOT_ORG_ID))) {
            errList.add("RootOrgId is not present");
        }
        if (StringUtils.isEmpty(inputDataMap.get(Constants.IDENTIFIER))) {
            errList.add("Identifier is not present");
        }
        if (StringUtils.isEmpty(inputDataMap.get(Constants.FILE_NAME))) {
            errList.add("Filename is not present");
        }
        if (StringUtils.isEmpty(inputDataMap.get(Constants.ORG_NAME))) {
            errList.add("Orgname is not present");
        }
        if (StringUtils.isEmpty(inputDataMap.get(Constants.X_AUTH_TOKEN))) {
            errList.add("User Token is not present");
        }
        if (!errList.isEmpty()) {
            str.append("Failed to Validate User Details. Error Details - [").append(errList.toString()).append("]");
        }
        return errList;
    }

}
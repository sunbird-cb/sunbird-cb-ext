package org.sunbird.calendar.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.AccessTokenValidator;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.producer.Producer;
import org.sunbird.storage.service.StorageServiceImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class CalendarBulkUploadServiceImpl implements CalendarBulkUploadService {

    private Logger logger = LoggerFactory.getLogger(CalendarBulkUploadServiceImpl.class);

    @Autowired
    StorageServiceImpl storageService;

    @Autowired
    Producer kafkaProducer;

    @Autowired
    CassandraOperation cassandraOperation;

    @Autowired
    AccessTokenValidator accessTokenValidator;

    @Autowired
    CbExtServerProperties serverConfig;

    @Autowired
    EventUtilityService eventUtilityService;


    ObjectMapper objectMapper = new ObjectMapper();

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ssXXX");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public SBApiResponse bulkUploadCalendarEvent(MultipartFile multipartFile, String orgId, String userAuthToken, String channelName) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_CALENDAR_EVENT_BULK_UPLOAD);
        try {
            String userId = validateAuthTokenAndFetchUserId(userAuthToken);
            if (StringUtils.isBlank(userId)) {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg(Constants.USER_ID_DOESNT_EXIST);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }

            if (isFileExistForProcessingForOrg(orgId)) {
                setErrorData(response, "Failed to upload for another request as previous request is in processing state, please try after some time.", HttpStatus.TOO_MANY_REQUESTS);
                return response;
            }
            SBApiResponse uploadResponse = storageService.uploadFile(multipartFile, serverConfig.getCalendarEventBulkUploadContainerName());
            if (!HttpStatus.OK.equals(uploadResponse.getResponseCode())) {
                setErrorData(response, String.format("Failed to upload file. Error: %s",
                        (String) uploadResponse.getParams().getErrmsg()), HttpStatus.INTERNAL_SERVER_ERROR);
                return response;
            }

            Map<String, Object> uploadedFile = new HashMap<>();
            uploadedFile.put(Constants.ROOT_ORG_ID, orgId);
            uploadedFile.put(Constants.IDENTIFIER, UUID.randomUUID().toString());
            uploadedFile.put(Constants.FILE_NAME, uploadResponse.getResult().get(Constants.NAME));
            uploadedFile.put(Constants.FILE_PATH, uploadResponse.getResult().get(Constants.URL));
            uploadedFile.put(Constants.DATE_CREATED_ON, new Timestamp(System.currentTimeMillis()));
            uploadedFile.put(Constants.STATUS, Constants.INITIATED_CAPITAL);
            uploadedFile.put(Constants.COMMENT, StringUtils.EMPTY);
            uploadedFile.put(Constants.CREATED_BY, userId);

            SBApiResponse insertResponse = cassandraOperation.insertRecord(Constants.DATABASE,
                    Constants.TABLE_CALENDAR_EVENT_BULK_UPLOAD, uploadedFile);

            if (!Constants.SUCCESS.equalsIgnoreCase((String) insertResponse.get(Constants.RESPONSE))) {
                setErrorData(response, "Failed to update database with user bulk upload file details.", HttpStatus.INTERNAL_SERVER_ERROR);
                return response;
            }

            response.getParams().setStatus(Constants.SUCCESSFUL);
            response.setResponseCode(HttpStatus.OK);
            response.getResult().putAll(uploadedFile);
            uploadedFile.put(Constants.ORG_NAME, channelName);
            uploadedFile.put(Constants.X_AUTH_TOKEN, userAuthToken);
            kafkaProducer.pushWithKey(serverConfig.getCalendarEventBulkUploadTopic(), uploadedFile, orgId);
        } catch (Exception e) {
            setErrorData(response,
                    String.format("Failed to process calendar Event bulk upload request. Error: ", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public void initiateCalendarEventBulkUploadProcess(String value) {
        logger.info("CalendarBulkUploadService:: initiateUserBulkUploadProcess: Started");
        long duration = 0;
        long startTime = System.currentTimeMillis();
        try {
            HashMap<String, String> inputDataMap = objectMapper.readValue(value,
                    new TypeReference<HashMap<String, String>>() {
                    });
            List<String> errList = validateReceivedKafkaMessage(inputDataMap);
            if (errList.isEmpty()) {
                updateCalendarBulkUploadStatus(inputDataMap.get(Constants.ROOT_ORG_ID),
                        inputDataMap.get(Constants.IDENTIFIER), Constants.STATUS_IN_PROGRESS_UPPERCASE, 0, 0, 0);
                storageService.downloadFile(inputDataMap.get(Constants.FILE_NAME), serverConfig.getCalendarEventBulkUploadContainerName());
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

    private boolean isFileExistForProcessingForOrg(String mdoId) {
        Map<String, Object> bulkUplaodPrimaryKey = new HashMap<String, Object>();
        bulkUplaodPrimaryKey.put(Constants.ROOT_ORG_ID, mdoId);
        List<String> fields = Arrays.asList(Constants.ROOT_ORG_ID, Constants.IDENTIFIER, Constants.STATUS);

        List<Map<String, Object>> bulkUploadMdoList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CALENDAR_EVENT_BULK_UPLOAD, bulkUplaodPrimaryKey, fields);
        if (CollectionUtils.isEmpty(bulkUploadMdoList)) {
            return false;
        }
        return bulkUploadMdoList.stream()
                .anyMatch(entry -> Constants.STATUS_IN_PROGRESS_UPPERCASE.equalsIgnoreCase((String) entry.get(Constants.STATUS)));
    }

    private void setErrorData(SBApiResponse response, String errMsg, HttpStatus httpStatus) {
        response.getParams().setStatus(Constants.FAILED);
        response.getParams().setErrmsg(errMsg);
        response.setResponseCode(httpStatus);
    }

    private String validateAuthTokenAndFetchUserId(String authUserToken) {
        return accessTokenValidator.fetchUserIdFromAccessToken(authUserToken);
    }

    public void updateCalendarBulkUploadStatus(String rootOrgId, String identifier, String status, int totalRecordsCount,
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
            cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CALENDAR_EVENT_BULK_UPLOAD,
                    fieldsToBeUpdated, compositeKeys);
        } catch (Exception e) {
            logger.error(String.format("Error in Updating  Bulk Upload Status in Cassandra %s", e.getMessage()), e);
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
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
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
                    Cell statusCell = firstRow.getCell(9);
                    Cell errorDetails = firstRow.getCell(10);
                    if (statusCell == null) {
                        statusCell = firstRow.createCell(9);
                    }
                    if (errorDetails == null) {
                        errorDetails = firstRow.createCell(10);
                    }
                    statusCell.setCellValue("Status");
                    errorDetails.setCellValue("Error Details");
                }
                int count = 0;
                while (rowIterator.hasNext()) {
                    logger.info("CalendarBulkUploadService:: Record " + count++);
                    long duration = 0;
                    long startTime = System.currentTimeMillis();
                    StringBuffer str = new StringBuffer();
                    List<String> errList = new ArrayList<>();
                    List<String> invalidErrList = new ArrayList<>();
                    Map<String, Object> eventInfoMap = new HashMap<>();
                    Date startDate = null;
                    Date endDate = null;
                    Row nextRow = rowIterator.next();
                    if (nextRow.getCell(0) == null || nextRow.getCell(0).getCellType() == CellType.BLANK) {
                        errList.add("Training name");
                    } else {
                        if (nextRow.getCell(0).getCellType() == CellType.STRING) {
                            eventInfoMap.put(Constants.NAME, nextRow.getCell(0).getStringCellValue().trim());
                        } else {
                            invalidErrList.add("Invalid column type. Expecting string format");
                        }
                    }
                    if (nextRow.getCell(1) == null || nextRow.getCell(1).getCellType() == CellType.BLANK) {
                        errList.add("Training link");
                    } else {
                        if (nextRow.getCell(1).getCellType() == CellType.STRING) {
                            eventInfoMap.put(Constants.REGISTRATION_LINK, nextRow.getCell(1).getStringCellValue().trim());
                        } else {
                            invalidErrList.add("Invalid column type. Expecting string format");
                        }
                    }
                    if (nextRow.getCell(2) == null || nextRow.getCell(2).getCellType() == CellType.BLANK) {
                        errList.add("Training description");
                    } else {
                        if (nextRow.getCell(2).getCellType() == CellType.STRING) {
                            eventInfoMap.put(Constants.DESCRIPTION, nextRow.getCell(2).getStringCellValue().trim());
                        } else {
                            invalidErrList.add("Invalid column type. Expecting string format");
                        }
                    }
                    if (nextRow.getCell(3) == null || nextRow.getCell(3).getCellType() == CellType.BLANK) {
                        errList.add("Type");
                    } else {
                        if (nextRow.getCell(3).getCellType() == CellType.STRING) {
                            eventInfoMap.put(Constants.EVENT_TYPE, nextRow.getCell(3).getStringCellValue().trim());
                        } else {
                            invalidErrList.add("Invalid column type. Expecting string format");
                        }
                    }
                    if (nextRow.getCell(4) == null || nextRow.getCell(4).getCellType() == CellType.BLANK) {
                        errList.add("Start date");
                    } else {
                        if (nextRow.getCell(4).getCellType() == CellType.NUMERIC) {
                            startDate = DateUtil.getJavaDate(nextRow.getCell(4).getNumericCellValue());
                            eventInfoMap.put(Constants.START_DATE, dateFormat.format(startDate));
                            eventInfoMap.put(Constants.REGISTRATION_END_DATE, dateFormat.format(startDate));
                        } else {
                            invalidErrList.add("Invalid column type. Expecting Date format in DD/MM/YYYY");
                        }
                    }
                    if (nextRow.getCell(5) == null || nextRow.getCell(5).getCellType() == CellType.BLANK) {
                        errList.add("End date");
                    } else {
                        if (nextRow.getCell(5).getCellType() == CellType.NUMERIC) {
                            endDate = DateUtil.getJavaDate(nextRow.getCell(5).getNumericCellValue());
                            eventInfoMap.put(Constants.END_DATE, dateFormat.format(endDate));
                        } else {
                            invalidErrList.add("Invalid column type. Expecting Date format in DD/MM/YYYY");
                        }
                    }
                    if (endDate != null && startDate != null && endDate.before(startDate)) {
                        invalidErrList.add("End Date should be equal to or greater than startDate.");
                    }
                    if (nextRow.getCell(6) == null || nextRow.getCell(6).getCellType() == CellType.BLANK) {
                        errList.add("Start time");
                    } else {
                        if (nextRow.getCell(6).getCellType() == CellType.NUMERIC) {
                            Date eventStartTime = DateUtil.getJavaDate(nextRow.getCell(6).getNumericCellValue());
                            eventInfoMap.put(Constants.START_TIME_KEY, sdf.format(eventStartTime.getTime()));
                        } else {
                            invalidErrList.add("Invalid column type. Expecting Time format HH:mm:ss");
                        }
                    }
                    if (nextRow.getCell(7) == null || nextRow.getCell(7).getCellType() == CellType.BLANK) {
                        errList.add("End time");
                    } else {
                        if (nextRow.getCell(7).getCellType() == CellType.NUMERIC) {
                            Date eventEndTime = DateUtil.getJavaDate(nextRow.getCell(7).getNumericCellValue());
                            eventInfoMap.put(Constants.END_TIME_KEY, sdf.format(eventEndTime.getTime()));
                        } else {
                            invalidErrList.add("Invalid column type. Expecting Time format HH:mm:ss");
                        }
                    }
                    if (nextRow.getCell(8) == null || nextRow.getCell(8).getCellType() == CellType.BLANK) {
                        errList.add("Venue");
                    } else {
                        if (nextRow.getCell(8).getCellType() == CellType.STRING) {
                            Map<String, Object> eventVenueInfo = new HashMap<>();
                            eventVenueInfo.put(Constants.ADDRESS, nextRow.getCell(8).getStringCellValue().trim());
                            eventInfoMap.put(Constants.LOCATION, eventVenueInfo);
                        } else {
                            invalidErrList.add("Invalid column type. Expecting string format");
                        }
                    }
                    eventInfoMap.put(Constants.SOURCE_NAME, (inputDataMap.get(Constants.ORG_NAME)));
                    eventInfoMap.put(Constants.CATEGORY, Constants.CALENDAR);
                    eventInfoMap.put("mimeType", "application/html");
                    eventInfoMap.put("locale", "en");
                    eventInfoMap.put(Constants.CREATED_BY, inputDataMap.get(Constants.CREATED_BY));
                    eventInfoMap.put(Constants.RESOURCE_TYPE, "Webinar");
                    eventInfoMap.put(Constants.CREATED_FOR, Arrays.asList(inputDataMap.get(Constants.ROOT_ORG_ID)));
                    eventInfoMap.put(Constants.CODE, "Calendar Event");
                    eventInfoMap.put(Constants.CHANNEL, inputDataMap.get(Constants.ROOT_ORG_ID));
                    Cell statusCell = nextRow.getCell(9);
                    Cell errorDetails = nextRow.getCell(10);
                    if (statusCell == null) {
                        statusCell = nextRow.createCell(9);
                    }
                    if (errorDetails == null) {
                        errorDetails = nextRow.createCell(10);
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
                        Map<String, Object> eventInfoFromESMap = eventInfoFromES((String) eventInfoMap.get(Constants.NAME), inputDataMap.get(Constants.ROOT_ORG_ID),
                                inputDataMap.get(Constants.X_AUTH_TOKEN));
                        if (MapUtils.isNotEmpty(eventInfoFromESMap) && ((Integer) eventInfoFromESMap.get(Constants.COUNT)).intValue() == 1) {
                            List<Map<String, Object>> searchEventInfo = (List<Map<String, Object>>)eventInfoFromESMap.get(Constants.EVENT_KEY);
                            if (CollectionUtils.isNotEmpty(searchEventInfo)) {
                                eventInfoMap.put(Constants.IDENTIFIER, searchEventInfo.get(0).get(Constants.IDENTIFIER));
                                eventInfoMap.put(Constants.VERSION_KEY, searchEventInfo.get(0).get(Constants.VERSION_KEY));
                                Map<String, Object> responseObject = eventUtilityService.updateEvent(eventInfoMap, inputDataMap.get(Constants.X_AUTH_TOKEN));
                                if (MapUtils.isEmpty(responseObject)) {
                                    failedRecordsCount++;
                                    statusCell.setCellValue(Constants.FAILED_UPPERCASE);
                                    errorDetails.setCellValue("Error while updating the event");
                                } else {
                                    Map<String, Object> publishEventMap = new HashMap<>();
                                    publishEventMap.put(Constants.STATUS, Constants.LIVE);
                                    publishEventMap.put(Constants.VERSION_KEY, responseObject.get(Constants.VERSION_KEY));
                                    publishEventMap.put(Constants.IDENTIFIER, responseObject.get(Constants.IDENTIFIER));
                                    Map<String, Object> publishEventObject = eventUtilityService.publishEvent(publishEventMap, inputDataMap.get(Constants.X_AUTH_TOKEN));
                                    if (MapUtils.isEmpty(publishEventObject)) {
                                        failedRecordsCount++;
                                        statusCell.setCellValue(Constants.FAILED_UPPERCASE);
                                        errorDetails.setCellValue("Error while publishing the event");
                                    } else {
                                        noOfSuccessfulRecords++;
                                        statusCell.setCellValue(Constants.SUCCESS_UPPERCASE);
                                        errorDetails.setCellValue("");
                                    }
                                }
                            } else {
                                failedRecordsCount++;
                                statusCell.setCellValue(Constants.FAILED_UPPERCASE);
                                errorDetails.setCellValue("Error while updating the event");
                            }
                        } else if (MapUtils.isNotEmpty(eventInfoFromESMap) && ((Integer) eventInfoFromESMap.get(Constants.COUNT)).intValue() == 0) {
                            Map<String, Object> responseObject = eventUtilityService.createEvent(eventInfoMap, inputDataMap.get(Constants.X_AUTH_TOKEN));
                            if (MapUtils.isEmpty(responseObject)) {
                                failedRecordsCount++;
                                statusCell.setCellValue(Constants.FAILED_UPPERCASE);
                                errorDetails.setCellValue("Error while creating the event");
                            } else {
                                Map<String, Object> publishEventMap = new HashMap<>();
                                publishEventMap.put(Constants.STATUS, Constants.LIVE);
                                publishEventMap.put(Constants.VERSION_KEY, responseObject.get(Constants.VERSION_KEY));
                                publishEventMap.put(Constants.IDENTIFIER, responseObject.get(Constants.IDENTIFIER));
                                Map<String, Object> publishEventObject = eventUtilityService.publishEvent(publishEventMap, inputDataMap.get(Constants.X_AUTH_TOKEN));
                                if (MapUtils.isEmpty(publishEventObject)) {
                                    failedRecordsCount++;
                                    statusCell.setCellValue(Constants.FAILED_UPPERCASE);
                                    errorDetails.setCellValue("Error while publishing the event");
                                } else {
                                    noOfSuccessfulRecords++;
                                    statusCell.setCellValue(Constants.SUCCESS_UPPERCASE);
                                    errorDetails.setCellValue("");
                                }
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
                    Cell statusCell = row.createCell(9);
                    Cell errorDetails = row.createCell(10);
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
            updateCalendarBulkUploadStatus(inputDataMap.get(Constants.ROOT_ORG_ID), inputDataMap.get(Constants.IDENTIFIER),
                    status, totalRecordsCount, noOfSuccessfulRecords, failedRecordsCount);
        } catch (Exception e) {
            logger.error(String.format("Error in Process Bulk Upload %s", e.getMessage()), e);
            updateCalendarBulkUploadStatus(inputDataMap.get(Constants.ROOT_ORG_ID), inputDataMap.get(Constants.IDENTIFIER),
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
        SBApiResponse uploadResponse = storageService.uploadFile(file, serverConfig.getCalendarEventBulkUploadContainerName(), serverConfig.getCloudContainerName());
        if (!HttpStatus.OK.equals(uploadResponse.getResponseCode())) {
            logger.info(String.format("Failed to upload file. Error: %s",
                    uploadResponse.getParams().getErrmsg()));
            return Constants.FAILED_UPPERCASE;
        }
        return Constants.SUCCESSFUL_UPPERCASE;
    }

    private List<String> validateReceivedKafkaMessage(HashMap<String, String> inputDataMap) {
        StringBuffer str = new StringBuffer();
        List<String> errList = new ArrayList<>();
        if (org.apache.commons.lang.StringUtils.isEmpty(inputDataMap.get(Constants.ROOT_ORG_ID))) {
            errList.add("RootOrgId is not present");
        }
        if (org.apache.commons.lang.StringUtils.isEmpty(inputDataMap.get(Constants.IDENTIFIER))) {
            errList.add("Identifier is not present");
        }
        if (org.apache.commons.lang.StringUtils.isEmpty(inputDataMap.get(Constants.FILE_NAME))) {
            errList.add("Filename is not present");
        }
        if (org.apache.commons.lang.StringUtils.isEmpty(inputDataMap.get(Constants.ORG_NAME))) {
            errList.add("Orgname is not present");
        }
        if (org.apache.commons.lang.StringUtils.isEmpty(inputDataMap.get(Constants.X_AUTH_TOKEN))) {
            errList.add("User Token is not present");
        }
        if (!errList.isEmpty()) {
            str.append("Failed to Validate User Details. Error Details - [").append(errList.toString()).append("]");
        }
        return errList;
    }

    private Map<String, Object> eventInfoFromES(String eventName, String orgId, String userToken) {
        Map<String, Object> filters = new HashMap<>();
        filters.put(Constants.STATUS, Arrays.asList(Constants.LIVE));
        filters.put(Constants.CHANNEL, orgId);
        filters.put(Constants.NAME, eventName);
        filters.put(Constants.CONTENT_TYPE_KEY, "Event");
        filters.put(Constants.CATEGORY, "Calendar");
        return eventUtilityService.compositeSearchForEvent(filters, userToken);
    }
}

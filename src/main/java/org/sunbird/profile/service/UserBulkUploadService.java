package org.sunbird.profile.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.storage.service.StorageService;
import org.sunbird.user.registration.model.UserRegistration;
import org.sunbird.user.service.UserUtilityService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Service
public class UserBulkUploadService {
	private static final CbExtLogger logger = new CbExtLogger(UserBulkUploadService.class.getName());
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
			if (inputDataMap.containsKey(Constants.FILE_NAME)) {
				String rootOrgId = inputDataMap.get(Constants.ROOT_ORG_ID);
				String identifier = inputDataMap.get(Constants.IDENTIFIER);
				String fileName = inputDataMap.get(Constants.FILE_NAME);
				String orgName = inputDataMap.get(Constants.ORG_NAME);
				updateUserBulkUploadStatus(rootOrgId, identifier, Constants.STATUS_IN_PROGRESS_UPPERCASE, 0, 0, 0);
				storageService.downloadFile(fileName);
				processBulkUpload(fileName, rootOrgId, identifier, orgName);
			} else {
				logger.info("Error in the scheduler to upload bulk users : File Name not present in the input data");
			}
		} catch (Exception e) {
			logger.error(String.format("Error in the scheduler to upload bulk users %s", e.getMessage()),
					e);
		}
		duration = System.currentTimeMillis() - startTime;
		logger.info("UserBulkUploadService:: initiateUserBulkUploadProcess: Completed. Time taken: "
				+ duration + " milli-seconds");
	}

	public Boolean updateUserBulkUploadStatus(String rootOrgId, String identifier, String status, int totalRecordsCount, int successfulRecordsCount, int failedRecordsCount) {
		try {
			Map<String, Object> compositeKeys = new HashMap<>();
			compositeKeys.put(Constants.ROOT_ORG_ID_LOWER, rootOrgId);
			compositeKeys.put(Constants.IDENTIFIER, identifier);
			Map<String, Object> fieldsToBeUpdated = new HashMap<>();
			if (!status.isEmpty()) {
				fieldsToBeUpdated.put(Constants.STATUS, status);
			}
			if (totalRecordsCount>=0) {
				fieldsToBeUpdated.put(Constants.TOTAL_RECORDS, totalRecordsCount);
			}
			if (successfulRecordsCount>=0) {
				fieldsToBeUpdated.put(Constants.SUCCESSFUL_RECORDS_COUNT, successfulRecordsCount);
			}
			if (failedRecordsCount>=0) {
				fieldsToBeUpdated.put(Constants.FAILED_RECORDS_COUNT, failedRecordsCount);
			}
			cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_USER_BULK_UPLOAD,
					fieldsToBeUpdated, compositeKeys);
		} catch (Exception e) {
			logger.error(String.format("Error in Updating User Bulk Upload Status in Cassandra %s", e.getMessage()), e);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	private void processBulkUpload(String fileName, String rootOrgId, String identifier, String orgName) throws IOException {
		File file = null;
		FileInputStream fis = null;
		XSSFWorkbook wb = null;
		int totalRecordsCount=0;
		int noOfSuccessfulRecords=0;
		int failedRecordsCount=0;
		try
		{
			file = new File(Constants.LOCAL_BASE_PATH + fileName);
			if (file.exists() && file.length()>0) {
				fis = new FileInputStream(file);
				wb = new XSSFWorkbook(fis);
				XSSFSheet sheet = wb.getSheetAt(0);
				Iterator<Row> rowIterator = sheet.iterator();
				if(rowIterator.hasNext()) {
					rowIterator.next();
				}
				while (rowIterator.hasNext()) {
					Row nextRow = rowIterator.next();
					if (nextRow.getCell(0)!=null) {
						UserRegistration userRegistration = new UserRegistration();
						userRegistration.setFirstName(nextRow.getCell(0).getStringCellValue());
						userRegistration.setLastName(nextRow.getCell(1).getStringCellValue());
						userRegistration.setEmail(nextRow.getCell(2).getStringCellValue());
						userRegistration.setContactNumber((int)nextRow.getCell(3).getNumericCellValue());
						userRegistration.setOrgName(orgName);
						List<String> errList = validateEmailContactAndDomain(userRegistration);
						Cell statusCell= nextRow.getCell(4);
						Cell errorDetails = nextRow.getCell(5);
						if (statusCell == null)
						{
							statusCell = nextRow.createCell(4);
						}
						if (errorDetails == null)
						{
							errorDetails = nextRow.createCell(5);
						}
						totalRecordsCount++;
						if (errList.isEmpty()) {
							boolean isUserCreated = userUtilityService.createUser(userRegistration);
							if (isUserCreated) {
								noOfSuccessfulRecords++;
								statusCell.setCellValue(Constants.SUCCESS.toUpperCase());
								errorDetails.setCellValue("");
							} else {
								failedRecordsCount++;
								statusCell.setCellValue(Constants.FAILED.toUpperCase());
								errorDetails.setCellValue(Constants.USER_CREATION_FAILED);
							}
						} else {
							failedRecordsCount++;
							statusCell.setCellValue(Constants.FAILED.toUpperCase());
							errorDetails.setCellValue(errList.toString());
						}
					}
					else
					{
						break;
					}
				}
				uploadTheUpdatedFile(rootOrgId, identifier, file, wb, totalRecordsCount, noOfSuccessfulRecords, failedRecordsCount);
			} else {
				logger.info("Error in Process Bulk Upload : The File is not downloaded/present");
				updateUserBulkUploadStatus(rootOrgId, identifier, Constants.FAILED.toUpperCase(), totalRecordsCount, noOfSuccessfulRecords, failedRecordsCount);
			}
		}
		catch(Exception e)
		{
			logger.error(String.format("Error in Process Bulk Upload %s", e.getMessage()), e);
			updateUserBulkUploadStatus(rootOrgId, identifier, Constants.FAILED.toUpperCase(), 0, 0, 0);
		}
		finally {
			if(wb!=null)
				wb.close();
			if(fis!=null)
				fis.close();
			if(file!=null)
				file.delete();
		}
	}

	private void uploadTheUpdatedFile(String rootOrgId, String identifier, File file, XSSFWorkbook wb, int totalRecordsCount, int noOfSuccessfulRecords, int failedRecordsCount) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(file);
		wb.write(fileOut);
		fileOut.close();
		SBApiResponse uploadResponse = storageService.uploadFile(file, serverProperties.getBulkUploadContainerName());
		if (!HttpStatus.OK.equals(uploadResponse.getResponseCode())) {
			logger.info(String.format("Failed to upload file. Error: %s",
					uploadResponse.getParams().getErrmsg()));
			updateUserBulkUploadStatus(rootOrgId, identifier, Constants.FAILED.toUpperCase(), totalRecordsCount, noOfSuccessfulRecords, failedRecordsCount);
		}
		else {
			if (failedRecordsCount == 0 && totalRecordsCount == noOfSuccessfulRecords && totalRecordsCount>=1) {
				updateUserBulkUploadStatus(rootOrgId, identifier, Constants.SUCCESSFUL.toUpperCase(), totalRecordsCount, noOfSuccessfulRecords, failedRecordsCount);
			} else {
				updateUserBulkUploadStatus(rootOrgId, identifier, Constants.FAILED.toUpperCase(), totalRecordsCount, noOfSuccessfulRecords, failedRecordsCount);
			}
		}
	}

	private List<String> validateEmailContactAndDomain(UserRegistration userRegistration) {
		StringBuffer str = new StringBuffer();
		List<String> errList = new ArrayList<>();
		if (!userUtilityService.isDomainAccepted(userRegistration.getEmail())) {
			errList.add("Domain not accepted");
		}
		if (!ProjectUtil.validateFirstName(userRegistration.getFirstName())) {
			errList.add("Invalid First Name");
		}
		if (!ProjectUtil.validateLastName(userRegistration.getLastName())) {
			errList.add("Invalid Last Name");
		}
		if (!ProjectUtil.validateEmailPattern(userRegistration.getEmail())) {
			errList.add("Invalid Email Address");
		}
		if (!ProjectUtil.validateContactPattern(String.valueOf(userRegistration.getContactNumber()))) {
			errList.add("Invalid Contact Number");
		}
		if (userUtilityService.isUserExist(Constants.EMAIL, userRegistration.getEmail())) {
			errList.add(Constants.EMAIL_EXIST_ERROR);
		}
		if (userUtilityService.isUserExist(Constants.PHONE, String.valueOf(userRegistration.getContactNumber()))) {
			errList.add(Constants.PHONE_NUMBER_EXIST_ERROR);
		}

		if (!errList.isEmpty()) {
			str.append("Failed to Validate User Details. Error Details - [").append(errList.toString()).append("]");
		}
		return errList;
	}

}
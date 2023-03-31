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
				updateUserBulkUploadStatus(rootOrgId, identifier, Constants.STATUS_IN_PROGRESS.toUpperCase(), 0, 0, 0);
				storageService.downloadFile(fileName);
				processBulkUpload(fileName, rootOrgId, identifier);
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

	private void processBulkUpload(String fileName, String rootOrgId, String identifier) throws IOException {
		File file = null;
		FileInputStream fis = null;
		XSSFWorkbook wb = null;
		int totalRecordsCount=0;
		int noOfSuccessfulRecords=0;
		int failedRecordsCount=0;
		try
		{
			file = new File(Constants.LOCAL_BASE_PATH + fileName);
			if (file.exists()) {
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
						List<String> errList = validateEmailContactAndDomain(userRegistration);
						totalRecordsCount++;
						Cell statusCell= nextRow.getCell(16);
						Cell errorDetails = nextRow.getCell(17);
						if (statusCell == null)
						{
							statusCell = nextRow.createCell(16);
						}
						if (errorDetails == null)
						{
							errorDetails = nextRow.createCell(17);
						}
						if (errList.isEmpty()) {
							userRegistration.setProposedDeptName(nextRow.getCell(5).getStringCellValue());
							userRegistration.setOrgName(nextRow.getCell(6).getStringCellValue());
							userRegistration.setRoles(addRolesIfApplicable(nextRow));
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
				}
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
					if (failedRecordsCount == 0 && totalRecordsCount == noOfSuccessfulRecords) {
						updateUserBulkUploadStatus(rootOrgId, identifier, Constants.SUCCESSFUL.toUpperCase(), totalRecordsCount, noOfSuccessfulRecords, failedRecordsCount);
					} else {
						updateUserBulkUploadStatus(rootOrgId, identifier, Constants.FAILED.toUpperCase(), totalRecordsCount, noOfSuccessfulRecords, failedRecordsCount);
					}
				}
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
			wb.close();
			fis.close();
			file.delete();
		}
	}

	private List<String> addRolesIfApplicable(Row nextRow) {
		List<String> roles = new ArrayList<>();
		if(nextRow.getCell(7)!=null && nextRow.getCell(7).getStringCellValue().equalsIgnoreCase("Y"))
		{
			roles.add(Constants.MDO_ADMIN);
		}
		if(nextRow.getCell(8)!=null && nextRow.getCell(8).getStringCellValue().equalsIgnoreCase("Y"))
		{
			roles.add(Constants.CONTENT_CREATOR);
		}
		if(nextRow.getCell(9)!=null && nextRow.getCell(9).getStringCellValue().equalsIgnoreCase("Y"))
		{
			roles.add(Constants.CONTENT_REVIEWER);
		}
		if(nextRow.getCell(10)!=null && nextRow.getCell(10).getStringCellValue().equalsIgnoreCase("Y"))
		{
			roles.add(Constants.CONTENT_PUBLISHER);
		}
		if(nextRow.getCell(11)!=null && nextRow.getCell(11).getStringCellValue().equalsIgnoreCase("Y"))
		{
			roles.add(Constants.CBP_ADMIN);
		}
		if(nextRow.getCell(15)!=null && nextRow.getCell(15).getStringCellValue().equalsIgnoreCase("Y"))
		{
			roles.add(Constants.PUBLIC);
		}
		return roles;
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
		if (userUtilityService.isUserExist(userRegistration.getEmail())) {
			errList.add("Another user with the same email already exists");
		}
		if (userUtilityService.validateIfUserContactAlreadyExists(userRegistration.getContactNumber())) {
			errList.add("Another user with the same phone already exists");
		}
		if (!errList.isEmpty()) {
			str.append("Failed to Validate User Details. Error Details - [").append(errList.toString()).append("]");
		}
		return errList;
	}

}
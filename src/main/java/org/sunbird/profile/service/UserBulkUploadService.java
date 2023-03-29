package org.sunbird.profile.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.storage.service.StorageService;
import org.sunbird.user.registration.model.UserRegistration;
import org.sunbird.user.service.UserUtilityService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Service
public class UserBulkUploadService {
	private static final CbExtLogger logger = new CbExtLogger(UserBulkUploadService.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	@Autowired
	CbExtServerProperties serverProperties;
	@Autowired
	UserUtilityService userUtilityService;

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
				String fileName = inputDataMap.get(Constants.FILE_NAME);
				storageService.downloadFile(fileName);
				processBulkUpload(fileName);
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

	private void processBulkUpload(String fileName) throws IOException {
		File file = null;
		FileInputStream fis = null;
		XSSFWorkbook wb = null;
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
					UserRegistration userRegistration = new UserRegistration();
					userRegistration.setEmail(nextRow.getCell(2).getStringCellValue());
					userRegistration.setContactNumber((int) nextRow.getCell(3).getNumericCellValue());
					List<String> errList = validateEmailContactAndDomain(userRegistration.getEmail(), userRegistration.getContactNumber());
					if(errList.isEmpty())
					{
						userRegistration.setFirstName(nextRow.getCell(0).getStringCellValue());
						userRegistration.setLastName(nextRow.getCell(1).getStringCellValue());
						userRegistration.setProposedDeptName(nextRow.getCell(5).getStringCellValue());
						userRegistration.setOrgName(nextRow.getCell(6).getStringCellValue());
						boolean isUserCreated = userUtilityService.createUser(userRegistration);
						if(isUserCreated)
						{
							nextRow.createCell(16).setCellValue(Constants.SUCCESS.toUpperCase());
							nextRow.createCell(17).setCellValue("");
						}
						else
						{
							nextRow.createCell(16).setCellValue(Constants.FAILED.toUpperCase());
							nextRow.createCell(17).setCellValue(Constants.USER_CREATION_FAILED);
						}
					}
					else
					{
						nextRow.createCell(16).setCellValue(Constants.FAILED.toUpperCase());
						nextRow.createCell(17).setCellValue(errList.toString());
					}
				}
				try {
					FileItem fileItem = new DiskFileItem("file", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml", false, file.getName(), (int) file.length() , null);
					fileItem.getOutputStream();
					MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
					storageService.uploadFile(multipartFile, serverProperties.getBulkUploadContainerName());
				} catch (Exception e) {
					logger.error(String.format("Error while uploading the file back %s", e.getMessage()), e);
				}
			} else {
				logger.info("Error in Process Bulk Upload : The File is not downloaded/present");
			}
		}
		catch(Exception e)
		{
			logger.error(String.format("Error in Process Bulk Upload %s", e.getMessage()), e);
		}
		finally {
			wb.close();
			fis.close();
			file.delete();
		}
	}

	private List<String> validateEmailContactAndDomain(String email, int contactNumber) {
		StringBuffer str = new StringBuffer();
		List<String> errList = new ArrayList<>();
		if (!userUtilityService.isDomainAccepted(email)) {
			errList.add("Domain not accepted");
		}
		if (!ProjectUtil.validateEmailPattern(email)) {
			errList.add("Invalid Email Address");
		}
		if (!ProjectUtil.validateContactPattern(String.valueOf(contactNumber))) {
			errList.add("Invalid Contact Number");
		}
		if (userUtilityService.isUserExist(email)) {
			errList.add("Another user with the same email already exists");
		}
		if (userUtilityService.validateIfUserContactAlreadyExists(contactNumber)) {
			errList.add("Another user with the same phone already exists");
		}
		if (!errList.isEmpty()) {
			str.append("Failed to Validate User Details. Error Details - [").append(errList.toString()).append("]");
		}
		return errList;
	}

}
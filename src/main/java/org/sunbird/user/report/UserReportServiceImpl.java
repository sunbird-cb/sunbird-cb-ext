package org.sunbird.user.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;

@Service
public class UserReportServiceImpl implements UserReportService {

	private Logger log = LoggerFactory.getLogger(getClass().getName());

	@Value("${user.report.store.path}")
	private String userStorePath;

	public void generateUserEnrolmentReport(Map<String, Map<String, String>> userEnrolmentMap, List<String> fields,
			SBApiResponse response) {
		log.info("UserReportServiceImpl:: generateUserEnrolmentReport started");
		long startTime = System.currentTimeMillis();

		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("KarmayogiBharat User Enrolment Details");
		int rowNum = 0;
		Row row = sheet.createRow(rowNum++);
		int cellNum = 0;
		for (String fieldName : fields) {
			Cell cell = row.createCell(cellNum++);
			cell.setCellValue(fieldName);
		}

		Iterator<Entry<String, Map<String, String>>> it = userEnrolmentMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Map<String, String>> entry = it.next();
			Row rowE = sheet.createRow(rowNum++);
			cellNum = 0;
			for (String field : fields) {
				Cell cell = rowE.createCell(cellNum++);
				String cellVal = entry.getValue().get(field);
				if (StringUtils.isNotBlank(cellVal)) {
					cell.setCellValue(cellVal);
				} else {
					cell.setCellValue("");
				}
			}
		}

		String fileName = userStorePath + "/userEnrolmentReport-" + java.time.LocalDate.now()
				+ System.currentTimeMillis() + ".xlsx";
		log.info("Constructed File Name -> " + fileName);

		try {
			File file = new File(fileName);
			file.createNewFile();
			OutputStream fileOut = new FileOutputStream(file, false);
			wb.write(fileOut);
			wb.close();
			response.getResult().put(Constants.FILE_NAME, fileName);
		} catch (Exception e) {
			log.error("Failed to write the workbook created for UserEnrolment Report. Exception: ", e);
		}

		log.info(String.format(
				"UserReportServiceImpl:: generateUserEnrolmentReport started and it took %s seconds to complete.",
				(System.currentTimeMillis() - startTime) / 1000));
	}

	public void generateUserReport(Map<String, Map<String, String>> userInfoMap, List<String> fields,
			SBApiResponse response) {
		log.info("UserReportServiceImpl:: generateUserReport started");
		long startTime = System.currentTimeMillis();

		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("KarmayogiBharat User Details");
		int rowNum = 0;
		Row row = sheet.createRow(rowNum++);
		int cellNum = 0;
		for (String fieldName : fields) {
			Cell cell = row.createCell(cellNum++);
			cell.setCellValue(fieldName);
		}

		Iterator<Entry<String, Map<String, String>>> it = userInfoMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Map<String, String>> entry = it.next();
			Row rowE = sheet.createRow(rowNum++);
			cellNum = 0;
			for (String field : fields) {
				Cell cell = rowE.createCell(cellNum++);
				String cellVal = entry.getValue().get(field);
				if (StringUtils.isNotBlank(cellVal)) {
					cell.setCellValue(cellVal);
				} else {
					cell.setCellValue("");
				}
			}
		}

		String fileName = userStorePath + "/userReport-" + java.time.LocalDate.now() + System.currentTimeMillis()
				+ ".xlsx";
		log.info("Constructed File Name -> " + fileName);

		try {
			File file = new File(fileName);
			file.createNewFile();
			OutputStream fileOut = new FileOutputStream(file, false);
			wb.write(fileOut);
			wb.close();
			response.getResult().put(Constants.FILE_NAME, fileName);
		} catch (Exception e) {
			log.error("Failed to write the workbook created for UserEnrolment Report. Exception: ", e);
		}

		log.info(String.format("UserReportServiceImpl:: generateUserReport started and it took %s seconds to complete.",
				(System.currentTimeMillis() - startTime) / 1000));
	}
}

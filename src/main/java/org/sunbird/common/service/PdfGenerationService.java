package org.sunbird.common.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.xalan.xsltc.compiler.util.InternalError;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.workallocation.model.ChildNode;
import org.sunbird.workallocation.model.CompetencyDetails;
import org.sunbird.workallocation.model.RoleCompetency;
import org.sunbird.workallocation.model.WAObject;
import org.sunbird.workallocation.model.WorkAllocation;
import org.sunbird.workallocation.util.WorkAllocationConstants;

import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

@Service
@SuppressWarnings("unchecked")
public class PdfGenerationService {

	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	@Value("${domain.host.name}")
	public String baseUrl;

	public byte[] getWAPdf(WorkAllocation wa, String statusSelected) {
		JSONArray pageTable = new JSONArray();
		JSONObject jPages = new JSONObject();
		jPages.put("pages", true);
		jPages.put("size", "a4");
		jPages.put("title", "Work Allocation Summary");
		jPages.put("top-margin", 50);
		pageTable.add(jPages);

		pageTable.add(getDepartmentLogoAndQRCode(wa, statusSelected));
		pageTable.add(getHeaderAndDate());
		pageTable.add(getRoleActivityHeader(statusSelected));
		pageTable.add(getUserRoleActivities(wa, statusSelected));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		try {
//			JsonPDF.writeToStream((new ByteArrayInputStream(pageTable.toJSONString().getBytes())), out, null);
//		} catch (Exception e) {
//			String errorMessage = "Failed to generate PDF from JSON Object. Exception: " + e.getMessage();
//			logger.warn(errorMessage);
//			logger.error(e);
//			return getWaErrorPdf(errorMessage);
//		}
//
		return out.toByteArray();
	}

	public byte[] getWaExpiredError(WorkAllocation wa, String oldWaId) {
		if (ObjectUtils.isEmpty(wa.getActiveWAObject())) {
			// There is no active WA object found. We can not create QR Code for
			// redirection.
			// TODO - Construct Error pdf and return.
		}

		return null;
	}

	public byte[] getWaErrorPdf(String errorMessage) {
		JSONArray pageTable = new JSONArray();
		JSONObject jPages = new JSONObject();
		jPages.put("pages", true);
		jPages.put("size", "a4");
		jPages.put("title", "Work Allocation Summary");
		jPages.put("top-margin", 50);
		pageTable.add(jPages);

		JSONObject paragraphSpacing = new JSONObject();
		paragraphSpacing.put("spacing-after", 5);

		JSONArray headerArray = new JSONArray();
		headerArray.add("paragraph");
		headerArray.add(paragraphSpacing);
		headerArray.add(errorMessage);

		pageTable.add(headerArray);
		
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			JsonPDF.writeToStream((new ByteArrayInputStream(pageTable.toJSONString().getBytes())), out, null);
			return out.toByteArray();
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	/**
	 * Prepares JSON data for Department and QR code from given WA object.
	 * 
	 * @param wa             - WorkAllocation Object
	 * @param statusSelected - Status Selected to identify the object
	 * @return - Returns PDF-Table structure in JSON format
	 */
	private JSONArray getDepartmentLogoAndQRCode(WorkAllocation wa, String statusSelected) {
		WAObject waObj = null;
		if (WorkAllocationConstants.PUBLISHED_STATUS.equalsIgnoreCase(statusSelected)) {
			waObj = wa.getActiveWAObject();
		} else if (WorkAllocationConstants.DRAFT_STATUS.equalsIgnoreCase(statusSelected)) {
			waObj = wa.getDraftWAObject();
		} else {
			throw new InternalError("Invalid status provided for generating PDF file.");
		}

		JSONArray pdfTable = new JSONArray();
		pdfTable.add("pdf-table");
		{
			JSONObject pdfTableProperties = new JSONObject();
			pdfTableProperties.put("width-percent", 100);
			pdfTableProperties.put("cell-border", false);
			pdfTableProperties.put("spacing-after", 20);
			pdfTable.add(pdfTableProperties);

			JSONArray widthColumn = new JSONArray();
			widthColumn.add(50);
			widthColumn.add(50);
			pdfTable.add(widthColumn);

			JSONArray firstRow = new JSONArray();

			// Adding Department Details...
			{
				JSONArray deptColArray = new JSONArray();
				deptColArray.add("pdf-table");
				widthColumn = new JSONArray();
				widthColumn.add(40);
				widthColumn.add(60);
				deptColArray.add(widthColumn);

				JSONArray singleRow = new JSONArray();

				// Cell for Logo
				JSONArray deptLogoArray = new JSONArray();
				deptLogoArray.add("pdf-cell");
				JSONObject firstColProperties = new JSONObject();
				firstColProperties.put("align", "left");
				firstColProperties.put("border", true);
				firstColProperties.put("border-width", 20);
				JSONArray borderColor = new JSONArray();
				borderColor.add(0);
				borderColor.add(0);
				borderColor.add(0);
				firstColProperties.put("border-color", borderColor);
				JSONArray borderEnabled = new JSONArray();
				borderEnabled.add("top");
				borderEnabled.add("bottom");
				borderEnabled.add("left");
				borderEnabled.add("right");
				firstColProperties.put("set-border", borderEnabled);
				deptLogoArray.add(firstColProperties);
				// Add image
				{
					// TODO check the DeptRepo for Dept Image details.
					JSONArray deptLogoImage = new JSONArray();
					deptLogoImage.add("image");
					JSONObject imageProperties = new JSONObject();
					imageProperties.put("width", 100);
					imageProperties.put("height", 100);
					deptLogoImage.add(imageProperties);
					deptLogoImage.add("classpath:government-of-india.jpg");
					deptLogoArray.add(deptLogoImage);
				}

				singleRow.add(deptLogoArray);

				// Cell for DeptName
				JSONArray deptNameColArray = new JSONArray();
				deptNameColArray.add("pdf-cell");
				JSONObject secondColProperties = new JSONObject();
				secondColProperties.put("valign", "bottom");
				deptNameColArray.add(secondColProperties);
				// Add Name
				{
					JSONArray deptName = new JSONArray();
					deptName.add("paragraph");
					deptName.add(waObj.getDeptName());

					deptNameColArray.add(deptName);
				}
				singleRow.add(deptNameColArray);
				deptColArray.add(singleRow);

				firstRow.add(deptColArray);
			}

			// Adding QR Code
			{
				JSONArray deptColArray = new JSONArray();
				deptColArray.add("pdf-table");
				widthColumn = new JSONArray();
				widthColumn.add(40);
				widthColumn.add(60);
				deptColArray.add(widthColumn);

				JSONArray singleRow = new JSONArray();

				// Cell for Logo
				JSONArray deptLogoArray = new JSONArray();
				deptLogoArray.add("pdf-cell");
				JSONObject firstColProperties = new JSONObject();
				firstColProperties.put("align", "left");
				deptLogoArray.add(firstColProperties);
				// Add image
				{
					JSONArray deptLogoImage = new JSONArray();
					deptLogoImage.add("image");
					baseUrl = baseUrl.concat(wa.getUserId()).concat("/").concat(waObj.getId());
					File qrCodeFile = QRCode.from(baseUrl).to(ImageType.PNG).file();

					deptLogoImage.add(qrCodeFile.getAbsolutePath());
					deptLogoArray.add(deptLogoImage);
				}

				singleRow.add(deptLogoArray);

				// Cell for DeptName
				JSONArray deptNameColArray = new JSONArray();
				deptNameColArray.add("pdf-cell");
				firstColProperties = new JSONObject();
				firstColProperties.put("valign", "bottom");
				deptNameColArray.add(firstColProperties);
				// Add Name
				{
					JSONArray deptName = new JSONArray();
					deptName.add("paragraph");
					deptName.add("Scan this QR code to find the latest updated digital version of this document");

					deptNameColArray.add(deptName);
				}
				singleRow.add(deptNameColArray);
				deptColArray.add(singleRow);

				firstRow.add(deptColArray);
			}

			pdfTable.add(firstRow);
		}
		return pdfTable;
	}

	/**
	 * Returns PDF-Table object for Header with Date
	 * 
	 * @return - PDF-Table structure in JSON format
	 */
	private JSONArray getHeaderAndDate() {
		JSONArray pdfTable = new JSONArray();
		pdfTable.add("pdf-table");
		{
			JSONObject pdfTableProperties = new JSONObject();
			pdfTableProperties.put("width-percent", 100);
			pdfTableProperties.put("cell-border", false);
			pdfTableProperties.put("spacing-after", 10);
			pdfTable.add(pdfTableProperties);

			JSONArray widthRow = new JSONArray();
			widthRow.add(100);
			pdfTable.add(widthRow);

			JSONArray firstRow = new JSONArray();
			JSONArray firstColArray = new JSONArray();
			firstColArray.add("pdf-cell");
			JSONObject firstColProperties = new JSONObject();
			firstColProperties.put("align", "left");
			firstColProperties.put("style", "bold");
			firstColProperties.put("size", 13);
			firstColArray.add(firstColProperties);
			firstColArray.add("Work allocation summary");
			firstRow.add(firstColArray);

			pdfTable.add(firstRow);

			JSONArray secondRow = new JSONArray();
			JSONArray secondColArray = new JSONArray();
			secondColArray.add("pdf-cell");
			JSONObject secondColProperties = new JSONObject();
			secondColProperties.put("align", "left");
			secondColProperties.put("size", 11);
			secondColArray.add(secondColProperties);

			String pattern = "h:mm a dd MMM yyy";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			String date = "As of " + simpleDateFormat.format(new Date());

			secondColArray.add(date);
			secondRow.add(secondColArray);

			pdfTable.add(secondRow);
		}

		return pdfTable;
	}

	/**
	 * Constructs the JSON data for PDF-Table for the given status
	 * 
	 * @param statusSelected - WorkAllocation object status.
	 * @return - Returns PDF-Table structure in JSON format
	 */
	private JSONArray getRoleActivityHeader(String statusSelected) {
		JSONArray pdfTable = new JSONArray();
		pdfTable.add("pdf-table");
		{
			JSONObject pdfTableProperties = new JSONObject();
			pdfTableProperties.put("width-percent", 100);
			pdfTableProperties.put("cell-border", false);
			pdfTableProperties.put("spacing-after", 10);
			pdfTable.add(pdfTableProperties);

			JSONArray widthRow = new JSONArray();
			widthRow.add(25);
			widthRow.add(30);
			widthRow.add(45);
			pdfTable.add(widthRow);

			JSONArray firstRow = new JSONArray();
			JSONArray firstColArray = new JSONArray();
			firstColArray.add("pdf-cell");
			JSONObject firstColProperties = new JSONObject();
			firstColProperties.put("align", "left");
			firstColProperties.put("size", 11);
			JSONArray bgColorArray = new JSONArray();
			bgColorArray.add(225);
			bgColorArray.add(225);
			bgColorArray.add(225);
			firstColProperties.put("background-color", bgColorArray);
			firstColArray.add(firstColProperties);
			firstColArray.add("Full name");
			firstRow.add(firstColArray);

			JSONArray secondColArray = new JSONArray();
			secondColArray.add("pdf-cell");
			JSONObject secondColProperties = new JSONObject();
			secondColProperties.put("align", "left");
			secondColProperties.put("size", 11);
			secondColProperties.put("background-color", bgColorArray);
			secondColArray.add(secondColProperties);
			secondColArray.add("Roles");
			firstRow.add(secondColArray);

			JSONArray thridColArray = new JSONArray();
			thridColArray.add("pdf-cell");
			JSONObject thirdColProperties = new JSONObject();
			thirdColProperties.put("align", "left");
			thirdColProperties.put("size", 11);
			thirdColProperties.put("background-color", bgColorArray);
			thridColArray.add(thirdColProperties);
			String header = "Activities";
			if (WorkAllocationConstants.DRAFT_STATUS.equalsIgnoreCase(statusSelected)) {
				header = "Activities and competencies";
			}
			thridColArray.add(header);
			firstRow.add(thridColArray);

			pdfTable.add(firstRow);
		}

		return pdfTable;
	}

	/**
	 * Constructs the JSON data for PDF-Table using given details.
	 * 
	 * @param wa             - Work Allocation Object
	 * @param statusSelected - Status selected to process the object
	 * @return - Returns PDF-Table structure in JSON format
	 */
	private JSONArray getUserRoleActivities(WorkAllocation wa, String statusSelected) {
		WAObject waObj = getWaObject(wa, statusSelected);
		JSONArray pdfTable = new JSONArray();
		pdfTable.add("pdf-table");
		{
			JSONObject pdfTableProperties = new JSONObject();
			pdfTableProperties.put("width-percent", 100);
			pdfTableProperties.put("cell-border", false);
			pdfTableProperties.put("spacing-after", 10);
			pdfTable.add(pdfTableProperties);

			JSONArray widthRow = new JSONArray();
			widthRow.add(25);
			widthRow.add(75);
			pdfTable.add(widthRow);

			JSONArray firstRow = new JSONArray();
			{
				JSONArray firstColArray = new JSONArray();
				firstColArray.add("pdf-cell");
				JSONObject firstColProperties = new JSONObject();
				firstColProperties.put("align", "left");
				firstColProperties.put("size", 11);
				firstColArray.add(firstColProperties);
				firstColArray.add(wa.getUserName());

				firstRow.add(firstColArray);
			}

			JSONArray secondColPdfTable = new JSONArray();
			secondColPdfTable.add("pdf-table");
			widthRow = new JSONArray();
			widthRow.add(40);
			widthRow.add(60);
			secondColPdfTable.add(widthRow);
			List<RoleCompetency> roleCompetencyList = waObj.getRoleCompetencyList();
			boolean flag = true;
			for (RoleCompetency roleCompetency : roleCompetencyList) {
				JSONArray roleCompeteArray = parseRoleCompetency(roleCompetency, statusSelected);
				secondColPdfTable.add(roleCompeteArray);
				if (flag) {
					secondColPdfTable.add(lineRow());
					flag = !flag;
				}
			}

			firstRow.add(secondColPdfTable);

			pdfTable.add(firstRow);
		}
		return pdfTable;
	}

	private WAObject getWaObject(WorkAllocation wa, String statusSelected) {
		WAObject waObj = null;
		if (WorkAllocationConstants.PUBLISHED_STATUS.equalsIgnoreCase(statusSelected)) {
			waObj = wa.getActiveWAObject();
		} else if (WorkAllocationConstants.DRAFT_STATUS.equalsIgnoreCase(statusSelected)) {
			waObj = wa.getDraftWAObject();
		} else {
			throw new InternalError("Invalid status provided for generating PDF file.");
		}
		return waObj;
	}

	private JSONArray parseRoleCompetency(RoleCompetency roleCompetency, String statusSelected) {
		JSONArray roleCompetencyRow = new JSONArray();

		JSONArray firstColArray = new JSONArray();
		firstColArray.add("pdf-cell");
		JSONObject firstColProperties = new JSONObject();
		firstColProperties.put("align", "left");
		firstColProperties.put("size", 11);
		firstColArray.add(firstColProperties);
		firstColArray.add(roleCompetency.getRoleDetails().getName());

		roleCompetencyRow.add(firstColArray);

		JSONArray secondColArray = new JSONArray();
		secondColArray.add("pdf-cell");
		JSONObject secondColProperties = new JSONObject();
		secondColProperties.put("align", "left");
		secondColProperties.put("size", 11);
		secondColArray.add(firstColProperties);

		JSONObject paragraphSpacing = new JSONObject();
		paragraphSpacing.put("spacing-after", 5);

		if (WorkAllocationConstants.DRAFT_STATUS.equalsIgnoreCase(statusSelected)) {
			JSONArray headerArray = new JSONArray();
			headerArray.add("paragraph");
			headerArray.add(paragraphSpacing);
			headerArray.add("Activities");
			secondColArray.add(headerArray);
		}

		for (ChildNode activity : roleCompetency.getRoleDetails().getChildNodes()) {
			JSONArray chunkArray = new JSONArray();
			chunkArray.add("paragraph");
			chunkArray.add(paragraphSpacing);
			chunkArray.add(activity.getName());
			secondColArray.add(chunkArray);
		}

		if (WorkAllocationConstants.DRAFT_STATUS.equalsIgnoreCase(statusSelected)) {
			JSONArray headerArray = new JSONArray();
			headerArray.add("paragraph");
			headerArray.add(paragraphSpacing);
			headerArray.add("Competencies");
			secondColArray.add(headerArray);
			for (CompetencyDetails competency : roleCompetency.getCompetencyDetails()) {
				JSONArray chunkArray = new JSONArray();
				chunkArray.add("paragraph");
				chunkArray.add(paragraphSpacing);
				chunkArray.add(competency.getName());
				secondColArray.add(chunkArray);
			}
		}

		roleCompetencyRow.add(secondColArray);

		return roleCompetencyRow;
	}

	private JSONArray lineRow() {
		JSONArray lineRow = new JSONArray();

		JSONArray firstColArray = new JSONArray();
		firstColArray.add("line");
		JSONObject dottedLine = new JSONObject();
		dottedLine.put("dotted", true);
		firstColArray.add(dottedLine);

		lineRow.add(firstColArray);

		JSONArray secondColArray = new JSONArray();
		secondColArray.add("line");
		secondColArray.add(dottedLine);
		lineRow.add(secondColArray);

		return lineRow;
	}
}

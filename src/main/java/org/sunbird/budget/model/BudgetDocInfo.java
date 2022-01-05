package org.sunbird.budget.model;

import java.util.HashMap;
import java.util.Map;

import org.sunbird.common.util.Constants;

public class BudgetDocInfo {
	private String orgId;
	private String budgetYear;
	private String id;
	private String proofId;
	private String fileName;
	private String fileType;
	private String fileSize;
	private String fileUrl;
	private String uploadedDate;
	private String uploadedBy;

	public String getBudgetYear() {
		return budgetYear;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileSize() {
		return fileSize;
	}

	public String getFileType() {
		return fileType;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public String getId() {
		return id;
	}

	public String getOrgId() {
		return orgId;
	}

	public Map<String, String> getProofDoc() {
		Map<String, String> proofDoc = new HashMap<>();
		proofDoc.put(Constants.BUDGET_DOC_FILE_NAME, fileName);
		proofDoc.put(Constants.BUDGET_DOC_FILE_SIZE, fileSize);
		proofDoc.put(Constants.BUDGET_DOC_FILE_TYPE, fileType);
		proofDoc.put(Constants.BUDGET_DOC_FILE_URL, fileUrl);
		return proofDoc;
	}

	public String getProofId() {
		return proofId;
	}

	public String getUploadedBy() {
		return uploadedBy;
	}

	public String getUploadedDate() {
		return uploadedDate;
	}

	public void setBudgetYear(String budgetYear) {
		this.budgetYear = budgetYear;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public void setProofId(String proofId) {
		this.proofId = proofId;
	}

	public void setUploadedBy(String uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public void setUploadedDate(String uploadedDate) {
		this.uploadedDate = uploadedDate;
	}
}
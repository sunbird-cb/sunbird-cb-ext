package org.sunbird.budget.model;
import java.util.List;
import java.util.Map;

public class BudgetDocInfo {

    private String id;
    private String orgId;
    private String budgetYear;
    private String schemeName;
    private String fileName;
    private String fileType;
    private String fileSize;
    private String uploadedOn;
    private String url;
    private List<Map<String, String>> proofdocs;

    public List<Map<String, String>> getProofdocs() {
        return proofdocs;
    }

    public void setProofdocs(List<Map<String, String>> proofdocs) {
        this.proofdocs = proofdocs;
    }
    public String getUploadedOn() {
        return uploadedOn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUploadedOn(String uploadedOn) {
        this.uploadedOn = uploadedOn;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getId() {
        return id;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getBudgetYear() {
        return budgetYear;
    }

    public void setBudgetYear(String budgetYear) {
        this.budgetYear = budgetYear;
    }

    public void setId(String id) {
        this.id = id;
    }


}

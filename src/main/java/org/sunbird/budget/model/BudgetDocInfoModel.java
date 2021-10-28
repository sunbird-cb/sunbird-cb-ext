package org.sunbird.budget.model;

import java.util.List;
import java.util.Map;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("org_budget_scheme")
public class BudgetDocInfoModel {

    public BudgetDocInfoModel() {
        super();
    }



    public BudgetDocInfoModel(BudgetDocInfoPrimaryKeyModel primaryKey, String schemeName,List<Map<String, String>> proofDocs) {
        this.primaryKey = primaryKey;
        this.schemeName = schemeName;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedOn = uploadedOn;
        this.url = url;
        this.proofDocs = proofDocs;



    }

    @PrimaryKey
    private BudgetDocInfoPrimaryKeyModel primaryKey;

    @Column("schemeName")
    private String schemeName;

    @Column("fileName")
    private String fileName;

    @Column("fileType")
    private String fileType;

    @Column("uploadedOn")
    private String uploadedOn;

    @Column("url")
    private String url;

    @Column("fileSize")
    private String fileSize;

    @Column("proofDocs")
    private List<Map<String, String>> proofDocs;

    public List<Map<String, String>> getProofDocs() {
        return proofDocs;
    }

    public void setProofDocs(List<Map<String, String>> proofDocs) {
        this.proofDocs = proofDocs;
    }
    public BudgetDocInfoPrimaryKeyModel getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(BudgetDocInfoPrimaryKeyModel primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public String getFileName () {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUploadedOn () {
        return uploadedOn;
    }

    public void setUploadedOn(String uploadedOn) {
        this.uploadedOn = uploadedOn;
    }

    public String getUrl () {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileType () {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public BudgetDocInfo getBudgetDocInfo() {
        BudgetDocInfo bdInfo = new BudgetDocInfo();
        bdInfo.setOrgId(primaryKey.getOrgId());
        bdInfo.setBudgetYear(primaryKey.getBudgetYear());
        bdInfo.setId(primaryKey.getId());
        bdInfo.setSchemeName(schemeName);
        bdInfo.getProofdocs();
        return bdInfo;
    }

}
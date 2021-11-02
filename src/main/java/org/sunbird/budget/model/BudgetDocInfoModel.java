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



    public BudgetDocInfoModel(BudgetDocInfoPrimaryKeyModel primaryKey,List<Map<String, String>> proofDocs) {
        this.primaryKey = primaryKey;
        this.proofDocs = proofDocs;

    }

    @PrimaryKey
    private BudgetDocInfoPrimaryKeyModel primaryKey;

    @Column("schemeName")
    private String schemeName;

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

    public BudgetDocInfo getBudgetDocInfo() {
        BudgetDocInfo bdInfo = new BudgetDocInfo();
        bdInfo.setOrgId(primaryKey.getOrgId());
        bdInfo.setBudgetYear(primaryKey.getBudgetYear());
        bdInfo.setId(primaryKey.getId());
        bdInfo.getProofDocs();
        return bdInfo;
    }

}
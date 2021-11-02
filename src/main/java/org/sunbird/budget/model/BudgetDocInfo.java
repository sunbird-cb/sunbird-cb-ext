package org.sunbird.budget.model;
import java.util.List;
import java.util.Map;

public class BudgetDocInfo {

    private String id;
    private String orgId;
    private String budgetYear;
    private String schemeName;

    private List<Map<String, String>> proofDocs;

    public List<Map<String, String>> getProofDocs() {
        return proofDocs;
    }

    public void setProofDocs(List<Map<String, String>> proofDocs) {
        this.proofDocs = proofDocs;
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

package org.sunbird.budget.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;
import org.sunbird.budget.model.BudgetDocInfoModel;
import org.sunbird.budget.model.BudgetInfoModel;
import org.sunbird.budget.model.BudgetInfoPrimaryKeyModel;

@Repository
public interface BudgetRepository extends CassandraRepository<BudgetInfoModel, BudgetInfoPrimaryKeyModel> {

	@Query(" select * from org_budget_scheme where orgId=?0 and budgetYear=?1")
	public List<BudgetInfoModel> getAllByOrgIdAndBudgetYear(String orgId, String budgetYear);

	@Query("select * from org_budget_scheme where orgId=?0 and id=?1")
	public Optional<BudgetInfoModel> findByOrgIdAndId(String orgId, String id);

	@Query("select * from org_budget_scheme where orgId=?0 and budgetYear=?1 and schemeName=?2")
	public List<BudgetInfoModel> getAllByOrgIdAndBudgetYearAndSchemeName(String orgId, String budgetYear,
			String schemeName);

	@Query(" select * from org_budget_proof where orgId=?0 and budgetYear=?1")
	public List<BudgetDocInfoModel> getAllDocByOrgIdAndBudgetYear(String orgId, String budgetYear);

	@Query(" select proofDocs from org_budget_scheme where orgId=?0 and budgetYear=?1")
	public List<BudgetDocInfoModel> getAllProofByOrgIdAndBudgetYear(String orgId, String budgetYear);

}

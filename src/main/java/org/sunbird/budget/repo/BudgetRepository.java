package org.sunbird.budget.repo;

import java.util.List;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;
import org.sunbird.budget.model.BudgetInfoModel;
import org.sunbird.budget.model.BudgetInfoPrimaryKeyModel;
import org.sunbird.staff.model.StaffInfoModel;

@Repository
public interface BudgetRepository extends CassandraRepository<BudgetInfoModel, BudgetInfoPrimaryKeyModel>{

	@Query(" select * from org_budget_scheme where orgId=?0")
	public List<BudgetInfoModel>getBudgetDetails(String orgId);
	
	@Query("update org_budget_scheme set trainingBudgetUtilization=?0 where orgId=?1 and id=?2 and budgetYear=?3")
    public List<StaffInfoModel> updateBudgetDetails(long trainingBudgetUtilization,String rootOrg, String id, String year);
	
	@Query(" delete from org_budget_scheme where orgId=?0 and id=?1 and budgetYear=?2")
    public List<BudgetInfoModel> deleteBudgetDetails(String rootOrg, String id, String year);
}

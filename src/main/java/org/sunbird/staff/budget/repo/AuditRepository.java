package org.sunbird.staff.budget.repo;

import java.util.List;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;
import org.sunbird.staff.budget.model.Audit;
import org.sunbird.staff.budget.model.AuditPrimaryKey;

@Repository
public interface AuditRepository extends CassandraRepository<Audit, AuditPrimaryKey>{

	@Query("select * from org_audit where orgId=?0 and auditType=?1")
	public List<Audit> getAudit(String orgId, String auditType);
}

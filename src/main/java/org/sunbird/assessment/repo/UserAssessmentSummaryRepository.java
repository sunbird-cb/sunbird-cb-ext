package org.sunbird.assessment.repo;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAssessmentSummaryRepository
		extends CassandraRepository<UserAssessmentSummaryModel, UserAssessmentSummaryPrimaryKeyModel> {

	/**
	 * returns assessment count for a user
	 * 
	 * @param userId
	 * @return
	 */
	@Query("select count(*) from user_assessment_summary where root_org=?0 and user_id=?1")
	public long countByPrimaryKeyRootOrgAndPrimaryKeyUserId(String rootOrg, String userId);
}

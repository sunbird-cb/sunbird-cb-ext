package org.sunbird.assessment.repo;

import java.util.List;
import java.util.Map;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAssessmentTopPerformerRepository
		extends CassandraRepository<UserAssessmentTopPerformerModel, UserAssessmentTopPerformerPrimaryKeyModel> {
	/**
	 * finds the top performers of given assessments(limit 250)
	 * 
	 * @param ids
	 * @return
	 */

	@Query("select * from user_assessment_top_performer where root_org = ?0 and parent_source_id in ?1 limit 250")
	public List<Map<String, Object>> findByPrimaryKeyRootOrgAndPrimaryKeyParentSourceIdIn(String rootOrg,
			List<String> ids);
}

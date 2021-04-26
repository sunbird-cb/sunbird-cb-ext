package org.sunbird.assessment.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.data.cassandra.core.CassandraOperations;

public class UserAssessmentMasterRepositoryImpl implements UserAssessmentMasterRepositoryCustom {

	@Autowired
	CassandraOperations cassandraOperations;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sunbird.assessment.repo.
	 * UserAssessmentMasterRepositoryCustom#updateAssessment(org.sunbird.assessment.
	 * repo.UserAssessmentMasterModel,
	 * org.sunbird.assessment.repo.UserAssessmentSummaryModel)
	 */
	@Override
	public UserAssessmentMasterModel updateAssessment(UserAssessmentMasterModel assessment,
			UserAssessmentSummaryModel assessmentSummary) {
		CassandraBatchOperations batchOps = cassandraOperations.batchOps();
		batchOps.insert(assessment);
		if (assessmentSummary.getPrimaryKey() != null)
			batchOps.insert(assessmentSummary);
		batchOps.execute();
		return assessment;
	}
}

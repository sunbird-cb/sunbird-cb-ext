package org.sunbird.assessment.repo;

public interface UserAssessmentMasterRepositoryCustom {

	/**
	 * updates assessment and assessment summary
	 * 
	 * @param assessment
	 * @param assessmentSummary
	 * @return
	 */
	public UserAssessmentMasterModel updateAssessment(UserAssessmentMasterModel assessment,
			UserAssessmentSummaryModel assessmentSummary);
}

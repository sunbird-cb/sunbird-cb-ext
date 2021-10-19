package org.sunbird.assessment.service;

import java.util.Map;

import org.sunbird.assessment.dto.AssessmentSubmissionDTO;

public interface AssessmentService {
	/**
	 * submits an assessment
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	 Map<String, Object> submitAssessment(String rootOrg, AssessmentSubmissionDTO data, String userEmail)
			throws Exception;

	/**
	 * gets assessments given a content id and user id
	 * 
	 * @param courseId
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	Map<String, Object> getAssessmentByContentUser(String rootOrg, String courseId, String userId) throws Exception;

}

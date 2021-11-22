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
	public Map<String, Object> submitAssessment(String rootOrg, AssessmentSubmissionDTO data, String userEmail)
			throws Exception;

	/**
	 * gets assessments given a content id and user id
	 * 
	 * @param course_id
	 * @param user_id
	 * @return
	 * @throws Exception
	 */
	Map<String, Object> getAssessmentByContentUser(String rootOrg, String courseId, String userId) throws Exception;

	/**
	 * submits assessments coming from iframe
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	Map<String, Object> submitAssessmentByIframe(String rootOrg, Map<String, Object> request) throws Exception;

	/**
	 * Get assement question set
	 * 
	 * @param courseId
	 * @param assessmentContentId
	 * @return
	 */
	public Map<String, Object> getAssessmentContent(String courseId, String assessmentContentId);

}

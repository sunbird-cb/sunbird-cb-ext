package org.sunbird.assessment.service;

import java.text.ParseException;
import java.util.Map;

import org.sunbird.assessment.dto.AssessmentSubmissionDTO;

public interface AssessmentService {
	/**
	 * gets assessments given a content id and user id
	 *
	 * @param course_id
	 * @param user_id
	 * @return
	 * @throws Exception
	 */
	Map<String, Object> getAssessmentByContentUser(String rootOrg, String courseId, String userId);

	/**
	 * Get assement question set
	 *
	 * @param courseId
	 * @param assessmentContentId
	 * @return
	 */
	public Map<String, Object> getAssessmentContent(String courseId, String assessmentContentId);

	/**
	 * submits an assessment
	 *
	 * @param data
	 * @return
	 * @throws ParseException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public Map<String, Object> submitAssessment(String rootOrg, AssessmentSubmissionDTO data, String userEmail)
			throws NumberFormatException, ParseException;

	/**
	 * submits assessments coming from iframe
	 *
	 * @param request
	 * @return
	 * @throws Exception
	 */
	Map<String, Object> submitAssessmentByIframe(String rootOrg, Map<String, Object> request);

}

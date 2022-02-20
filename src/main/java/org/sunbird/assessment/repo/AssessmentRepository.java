package org.sunbird.assessment.repo;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.sunbird.assessment.dto.AssessmentSubmissionDTO;

public interface AssessmentRepository {

	/**
	 * gets answer key for the assessment given the url
	 *
	 * @param artifactUrl
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getAssessmentAnswerKey(String artifactUrl);

	/**
	 * gets assessment for a user given a content id
	 *
	 * @param courseId
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getAssessmetbyContentUser(String rootOrg, String courseId, String userId);

	/**
	 * gets answerkey for the quiz submission
	 *
	 * @param quizMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getQuizAnswerKey(AssessmentSubmissionDTO quizMap);

	/**
	 * inserts quiz or assessments for a user
	 *
	 * @param persist
	 * @param isAssessment
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> insertQuizOrAssessment(Map<String, Object> persist, Boolean isAssessment)
			throws NumberFormatException, ParseException;

}

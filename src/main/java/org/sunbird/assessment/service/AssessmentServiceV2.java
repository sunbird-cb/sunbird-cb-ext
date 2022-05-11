package org.sunbird.assessment.service;

import java.util.Map;

import org.sunbird.common.model.SBApiResponse;

public interface AssessmentServiceV2 {
	/**
	 * submits an assessment
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public SBApiResponse submitAssessment(Map<String, Object> data, String userEmail) throws Exception;

	public SBApiResponse readAssessment(String assessmentIdentifier, String token) throws Exception;

	public SBApiResponse readQuestionList(Map<String, Object> requestBody, String authUserToken) throws Exception;
}

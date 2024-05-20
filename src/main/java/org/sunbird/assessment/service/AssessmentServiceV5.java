package org.sunbird.assessment.service;

import org.sunbird.common.model.SBApiResponse;

import java.util.Map;

public interface AssessmentServiceV5 {

	public SBApiResponse readAssessment(String assessmentIdentifier, String token,boolean editMode);

	public SBApiResponse readQuestionList(Map<String, Object> requestBody, String authUserToken,boolean editMode);

	public SBApiResponse retakeAssessment(String assessmentIdentifier, String token,Boolean editMode);

	public SBApiResponse readAssessmentResultV5(Map<String, Object> request, String userAuthToken);

	public SBApiResponse submitAssessmentAsync(Map<String, Object> data, String userAuthToken,boolean editMode);
}

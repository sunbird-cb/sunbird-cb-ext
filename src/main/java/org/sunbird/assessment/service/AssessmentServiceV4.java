package org.sunbird.assessment.service;

import java.util.Map;

import org.sunbird.common.model.SBApiResponse;

public interface AssessmentServiceV4 {
    public SBApiResponse submitAssessment(Map<String, Object> data, String userAuthToken);

	public SBApiResponse readAssessment(String assessmentIdentifier, String token,boolean editMode);

	public SBApiResponse readQuestionList(Map<String, Object> requestBody, String authUserToken,boolean editMode);

	public SBApiResponse retakeAssessment(String assessmentIdentifier, String token,Boolean editMode);

	public SBApiResponse readAssessmentResultV4(Map<String, Object> request, String userAuthToken);

	public SBApiResponse submitAssessmentAsync(Map<String, Object> data, String userAuthToken,boolean editMode);

	public void handleAssessmentSubmitRequest(Map<String, Object> asyncRequest,boolean editMode,String token);
}

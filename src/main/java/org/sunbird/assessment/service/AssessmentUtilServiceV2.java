package org.sunbird.assessment.service;

import java.util.List;
import java.util.Map;

public interface AssessmentUtilServiceV2 {
	public Map<String, Object> validateQumlAssessment(List<String> originalQuestionList,
			List<Map<String, Object>> userQuestionList);

	public String fetchQuestionIdentifierValue(List<String> identifierList, List<Object> questionList, String primaryCategory) throws Exception;

	public Map<String, Object> getReadHierarchyApiResponse(String assessmentIdentifier, String token);
}

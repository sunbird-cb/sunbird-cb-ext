package org.sunbird.assessment.service;

import java.util.List;
import java.util.Map;

public interface AssessmentUtilServiceV2 {
	public Map<String, Object> validateQumlAssessment(List<Object> originalQuestionList,
													  List<Map<String, Object>> userQuestionList);

	public String fetchQuestionIdentifierValue(List<String> identifierList, List<Object> questionList) throws Exception;

	public Map<String, Object> getReadHierarchyApiResponse(String assessmentIdentifier, String token);
}

package org.sunbird.assessment.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface AssessmentUtilServiceV2 {
	public Map<String, Object> validateQumlAssessment(List<String> originalQuestionList,
			List<Map<String, Object>> userQuestionList,Map<String,Object> questionMap);

	public String fetchQuestionIdentifierValue(List<String> identifierList, List<Object> questionList, String primaryCategory) throws Exception;

	Map<String, Object> filterQuestionMapDetail(Map<String, Object> questionMapResponse, String primaryCategory);

	List<Map<String, Object>> readQuestionDetails(List<String> identifiers);

	public Map<String, Object> getReadHierarchyApiResponse(String assessmentIdentifier, String token);

	public Map<String, Object> readAssessmentHierarchyFromDB(String assessmentIdentifier);

	public List<Map<String, Object>> readUserSubmittedAssessmentRecords(String userId, String assessmentId);

	public Map<String, Object> readQuestsOfQSet(List<String> questionIds,String assessmentIdentifier) throws IOException;
}

package org.sunbird.assessment.service;

import java.util.List;
import java.util.Map;

import org.sunbird.assessment.model.QuestionSet;

public interface AssessmentUtilService {

	Map<String, Object> validateAssessment(List<Map<String, Object>> questions);

	Map<String, Object> validateAssessment(List<Map<String, Object>> questions, Map<String, Object> answers);

	Map<String, Object> getAnswerKeyForAssessmentAuthoringPreview(Map<String, Object> contentMeta);

	QuestionSet removeAssessmentAnsKey(Object assessmentContent);
}

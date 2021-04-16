package org.sunbird.assessment.repo;

import java.util.List;
import java.util.Map;

import org.sunbird.assessment.dto.AssessmentSubmissionDTO;

public class AssessmentRepositoryImpl implements AssessmentRepository {

	@Override
	public Map<String, Object> getAssessmentAnswerKey(String artifactUrl) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getQuizAnswerKey(AssessmentSubmissionDTO quizMap) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> insertQuizOrAssessment(Map<String, Object> persist, Boolean isAssessment)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> getAssessmetbyContentUser(String rootOrg, String courseId, String userId)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}

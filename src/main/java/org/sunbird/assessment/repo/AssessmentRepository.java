package org.sunbird.assessment.repo;

import java.sql.Timestamp;
import java.util.Date;
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
    public Map<String, Object> getAssessmentAnswerKey(String artifactUrl) throws Exception;

    /**
     * gets answerkey for the quiz submission
     *
     * @param quizMap
     * @return
     * @throws Exception
     */
    public Map<String, Object> getQuizAnswerKey(AssessmentSubmissionDTO quizMap) throws Exception;

    /**
     * inserts quiz or assessments for a user
     *
     * @param persist
     * @param isAssessment
     * @return
     * @throws Exception
     */
    public Map<String, Object> insertQuizOrAssessment(Map<String, Object> persist, Boolean isAssessment)
            throws Exception;

    /**
     * gets assessment for a user given a content id
     *
     * @param courseId
     * @param userId
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> getAssessmentbyContentUser(String rootOrg, String courseId, String userId)
            throws Exception;

    boolean addUserAssesmentDataToDB(String userId, String assessmentId, Timestamp startTime, Timestamp endTime, Map<String, Object> questionSet, String status);

    List<Map<String, Object>> fetchUserAssessmentDataFromDB(String userId, String assessmentIdentifier);

    Boolean updateUserAssesmentDataToDB(String userId, String assessmentIdentifier, Map<String, Object> submitAssessmentRequest, Map<String, Object> submitAssessmentResponse, String status, Date startTime);
}

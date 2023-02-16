package org.sunbird.assessment.repo;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sunbird.assessment.dto.AssessmentSubmissionDTO;

public interface FlagRepository {

	boolean addFlagDataToDB(String userId, Map<String, Object> request);

	List<Map<String, Object>> getFlaggedData(String userId);

	Boolean updateFlaggedData(String userId, Map<String, Object> request);
}
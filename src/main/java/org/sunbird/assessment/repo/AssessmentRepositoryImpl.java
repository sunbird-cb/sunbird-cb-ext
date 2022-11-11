package org.sunbird.assessment.repo;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.assessment.dto.AssessmentSubmissionDTO;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;

import com.datastax.driver.core.utils.UUIDs;
import com.google.gson.Gson;

@Service
public class AssessmentRepositoryImpl implements AssessmentRepository {

	public static final String ROOT_ORG = "rootOrg";
	public static final String RESULT = "result";
	public static final String SOURCE_ID = "sourceId";
	public static final String USER_ID = "userId";
	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	@Autowired
	UserAssessmentSummaryRepository userAssessmentSummaryRepo;

	@Autowired
	UserAssessmentMasterRepository userAssessmentMasterRepo;

	@Autowired
	UserQuizMasterRepository userQuizMasterRepo;

	@Autowired
	CassandraOperation cassandraOperation;

	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

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
		Map<String, Object> response = new HashMap<>();
		Date date = new Date();

		// insert assessment and assessment summary
		if (Boolean.TRUE.equals(isAssessment)) {
			UserAssessmentMasterModel assessment = new UserAssessmentMasterModel(
					new UserAssessmentMasterPrimaryKeyModel(persist.get(ROOT_ORG).toString(), date,
							persist.get("parent").toString(), BigDecimal.valueOf((Double) persist.get(RESULT)),
							UUIDs.timeBased()),
					Integer.parseInt(persist.get("correct").toString()), formatter.parse(formatter.format(date)),
					Integer.parseInt(persist.get("incorrect").toString()),
					Integer.parseInt(persist.get("blank").toString()), persist.get("parentContentType").toString(),
					new BigDecimal(60), persist.get(SOURCE_ID).toString(), persist.get("title").toString(),
					persist.get(USER_ID).toString());
			UserAssessmentSummaryModel summary = new UserAssessmentSummaryModel();
			UserAssessmentSummaryModel data = userAssessmentSummaryRepo
					.findById(new UserAssessmentSummaryPrimaryKeyModel(persist.get(ROOT_ORG).toString(),
							persist.get(USER_ID).toString(), persist.get(SOURCE_ID).toString()))
					.orElse(null);

			if (persist.get("parentContentType").toString().equalsIgnoreCase("course")) {
				if (data != null) {
					if (data.getFirstMaxScore() < Float.parseFloat(persist.get(RESULT).toString())) {
						summary = new UserAssessmentSummaryModel(
								new UserAssessmentSummaryPrimaryKeyModel(persist.get(ROOT_ORG).toString(),
										persist.get(USER_ID).toString(), persist.get(SOURCE_ID).toString()),
								Float.parseFloat(persist.get(RESULT).toString()), date, data.getFirstPassesScore(),
								data.getFirstPassesScoreDate());
					}
				} else if (Float.parseFloat(persist.get(RESULT).toString()) > Constants.ASSESSMENT_PASS_SCORE) {
					summary = new UserAssessmentSummaryModel(
							new UserAssessmentSummaryPrimaryKeyModel(persist.get(ROOT_ORG).toString(),
									persist.get(USER_ID).toString(), persist.get(SOURCE_ID).toString()),
							Float.parseFloat(persist.get(RESULT).toString()), date,
							Float.parseFloat(persist.get(RESULT).toString()), date);
				} else {
					summary = new UserAssessmentSummaryModel(
							new UserAssessmentSummaryPrimaryKeyModel(persist.get(ROOT_ORG).toString(),
									persist.get(USER_ID).toString(), persist.get(SOURCE_ID).toString()),
							Float.parseFloat(persist.get(RESULT).toString()), date, null, null);
					userAssessmentSummaryRepo.save(summary);

				}
			}
			userAssessmentMasterRepo.updateAssessment(assessment, summary);
		}
		// insert quiz and quiz summary
		else {
			UserQuizMasterModel quiz = new UserQuizMasterModel(
					new UserQuizMasterPrimaryKeyModel(persist.get(ROOT_ORG).toString(), date,
							BigDecimal.valueOf((Double) persist.get(RESULT)), UUIDs.timeBased()),
					Integer.parseInt(persist.get("correct").toString()), formatter.parse(formatter.format(date)),
					Integer.parseInt(persist.get("incorrect").toString()),
					Integer.parseInt(persist.get("blank").toString()), new BigDecimal(60),
					persist.get(SOURCE_ID).toString(), persist.get("title").toString(),
					persist.get(USER_ID).toString());
			UserQuizSummaryModel summary = new UserQuizSummaryModel(
					new UserQuizSummaryPrimaryKeyModel(persist.get(ROOT_ORG).toString(),
							persist.get(USER_ID).toString(), persist.get(SOURCE_ID).toString()),
					date);

			userQuizMasterRepo.updateQuiz(quiz, summary);
		}

		response.put("response", "SUCCESS");
		return response;
	}

	@Override
	public List<Map<String, Object>> getAssessmentbyContentUser(String rootOrg, String courseId, String userId)
			throws Exception {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	public boolean addUserAssesmentDataToDB(String userId, String assessmentIdentifier, Timestamp startTime,
			Timestamp endTime, Map<String, Object> questionSet, String status) {
		Map<String, Object> request = new HashMap<>();
		request.put(Constants.USER_ID, userId);
		request.put(Constants.ASSESSMENT_ID_KEY, assessmentIdentifier);
		request.put(Constants.START_TIME, startTime);
		request.put(Constants.END_TIME, endTime);
		request.put(Constants.ASSESSMENT_READ_RESPONSE, new Gson().toJson(questionSet));
		request.put(Constants.STATUS, status);
		SBApiResponse resp = cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD,
				Constants.TABLE_USER_ASSESSMENT_DATA, request);
		return resp.get(Constants.RESPONSE).equals(Constants.SUCCESS);
	}

	@Override
	public List<Map<String, Object>> fetchUserAssessmentDataFromDB(String userId, String assessmentIdentifier) {
		Map<String, Object> request = new HashMap<>();
		request.put(Constants.USER_ID, userId);
		request.put(Constants.ASSESSMENT_ID_KEY, assessmentIdentifier);
		List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(
				Constants.KEYSPACE_SUNBIRD, Constants.TABLE_USER_ASSESSMENT_DATA, request, null);
		return existingDataList;
	}

	@Override
	public Boolean updateUserAssesmentDataToDB(String userId, String assessmentIdentifier,
			Map<String, Object> submitAssessmentRequest, Map<String, Object> submitAssessmentResponse, String status,
			Date startTime) {
		Map<String, Object> compositeKeys = new HashMap<>();
		compositeKeys.put(Constants.USER_ID, userId);
		compositeKeys.put(Constants.ASSESSMENT_ID_KEY, assessmentIdentifier);
		compositeKeys.put(Constants.START_TIME, startTime);
		Map<String, Object> fieldsToBeUpdated = new HashMap<>();
		if (!submitAssessmentRequest.isEmpty()) {
			fieldsToBeUpdated.put("submitassessmentrequest", new Gson().toJson(submitAssessmentRequest));
		}
		if (!submitAssessmentResponse.isEmpty()) {
			fieldsToBeUpdated.put("submitassessmentresponse", new Gson().toJson(submitAssessmentResponse));
		}
		if (!status.isEmpty()) {
			fieldsToBeUpdated.put(Constants.STATUS, status);
		}
		cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_USER_ASSESSMENT_DATA,
				fieldsToBeUpdated, compositeKeys);
		return true;
	}
}

package org.sunbird.assessment.repo;

import com.datastax.driver.core.utils.UUIDs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.assessment.dto.AssessmentSubmissionDTO;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

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
	public boolean addUserAssesmentStartTime(String userId, String assessmentIdentifier, Timestamp startTime) {
		Map<String, Object> request = new HashMap<>();
		request.put(Constants.USER_ID, userId);
		request.put(Constants.IDENTIFIER, assessmentIdentifier);
		cassandraOperation.deleteRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_USER_ASSESSMENT_TIME, request);
		request.put("starttime", startTime);
		SBApiResponse resp = cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_USER_ASSESSMENT_TIME, request);
		return resp.get(Constants.RESPONSE).equals(Constants.SUCCESS);
	}

    @Override
    public Date fetchUserAssessmentStartTime(String userId, String assessmentIdentifier) {
		Map<String, Object> request = new HashMap<>();
		request.put(Constants.USER_ID, userId);
		request.put(Constants.IDENTIFIER, assessmentIdentifier);
		Map<String, Object> existingDataList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
				Constants.TABLE_USER_ASSESSMENT_TIME, request, null).get(0);
		return (Date) existingDataList.get("starttime");
	}
}

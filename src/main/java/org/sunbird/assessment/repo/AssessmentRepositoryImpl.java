package org.sunbird.assessment.repo;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.assessment.dto.AssessmentSubmissionDTO;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;

import com.datastax.driver.core.utils.UUIDs;

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

	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public Map<String, Object> getAssessmentAnswerKey(String artifactUrl) {

		return new HashMap<>();
	}

	@Override
	public List<Map<String, Object>> getAssessmetbyContentUser(String rootOrg, String courseId, String userId) {
		return Collections.emptyList();
	}

	@Override
	public Map<String, Object> getQuizAnswerKey(AssessmentSubmissionDTO quizMap) {

		return new HashMap<>();
	}

	@Override
	public Map<String, Object> insertQuizOrAssessment(Map<String, Object> persist, Boolean isAssessment)
			throws NumberFormatException, ParseException {
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

			if ("course".equalsIgnoreCase(persist.get("parentContentType").toString())) {
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
}

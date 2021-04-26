package org.sunbird.assessment.repo;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.assessment.dto.AssessmentSubmissionDTO;
import org.sunbird.common.util.Constants;

import com.datastax.driver.core.utils.UUIDs;

@Service
public class AssessmentRepositoryImpl implements AssessmentRepository {

	@Autowired
	UserAssessmentSummaryRepository userAssessmentSummaryRepo;

	@Autowired
	UserAssessmentMasterRepository userAssessmentMasterRepo;

	@Autowired
	UserQuizMasterRepository userQuizMasterRepo;

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
		Date record = new Date();

		// insert assessment and assessment summary
		if (isAssessment) {
			UserAssessmentMasterModel assessment = new UserAssessmentMasterModel(
					new UserAssessmentMasterPrimaryKeyModel(persist.get("rootOrg").toString(), record,
							persist.get("parent").toString(), new BigDecimal((Double) persist.get("result")),
							UUIDs.timeBased()),
					Integer.parseInt(persist.get("correct").toString()), formatter.parse(formatter.format(record)),
					Integer.parseInt(persist.get("incorrect").toString()),
					Integer.parseInt(persist.get("blank").toString()), persist.get("parentContentType").toString(),
					new BigDecimal(60), persist.get("sourceId").toString(), persist.get("title").toString(),
					persist.get("userId").toString());
			UserAssessmentSummaryModel summary = new UserAssessmentSummaryModel();
			UserAssessmentSummaryModel data = userAssessmentSummaryRepo
					.findById(new UserAssessmentSummaryPrimaryKeyModel(persist.get("rootOrg").toString(),
							persist.get("userId").toString(), persist.get("sourceId").toString()))
					.orElse(null);

			if (persist.get("parentContentType").toString().toLowerCase().equals("course")) {
				if (data != null) {
					if (data.getFirstMaxScore() < Float.parseFloat(persist.get("result").toString())) {
						summary = new UserAssessmentSummaryModel(
								new UserAssessmentSummaryPrimaryKeyModel(persist.get("rootOrg").toString(),
										persist.get("userId").toString(), persist.get("sourceId").toString()),
								Float.parseFloat(persist.get("result").toString()), record, data.getFirstPassesScore(),
								data.getFirstPassesScoreDate());
					}
				} else if (Float.parseFloat(persist.get("result").toString()) > Constants.ASSESSMENT_PASS_SCORE) {
					summary = new UserAssessmentSummaryModel(
							new UserAssessmentSummaryPrimaryKeyModel(persist.get("rootOrg").toString(),
									persist.get("userId").toString(), persist.get("sourceId").toString()),
							Float.parseFloat(persist.get("result").toString()), record,
							Float.parseFloat(persist.get("result").toString()), record);
				} else {
					summary = new UserAssessmentSummaryModel(
							new UserAssessmentSummaryPrimaryKeyModel(persist.get("rootOrg").toString(),
									persist.get("userId").toString(), persist.get("sourceId").toString()),
							Float.parseFloat(persist.get("result").toString()), record, null, null);
					userAssessmentSummaryRepo.save(summary);

				}
			}
			userAssessmentMasterRepo.updateAssessment(assessment, summary);
		}
		// insert quiz and quiz summary
		else {
			UserQuizMasterModel quiz = new UserQuizMasterModel(
					new UserQuizMasterPrimaryKeyModel(persist.get("rootOrg").toString(), record,
							new BigDecimal((Double) persist.get("result")), UUIDs.timeBased()),
					Integer.parseInt(persist.get("correct").toString()), formatter.parse(formatter.format(record)),
					Integer.parseInt(persist.get("incorrect").toString()),
					Integer.parseInt(persist.get("blank").toString()), new BigDecimal(60),
					persist.get("sourceId").toString(), persist.get("title").toString(),
					persist.get("userId").toString());
			UserQuizSummaryModel summary = new UserQuizSummaryModel(
					new UserQuizSummaryPrimaryKeyModel(persist.get("rootOrg").toString(),
							persist.get("userId").toString(), persist.get("sourceId").toString()),
					record);

			userQuizMasterRepo.updateQuiz(quiz, summary);
		}

		response.put("response", "SUCCESS");
		return response;
	}

	@Override
	public List<Map<String, Object>> getAssessmetbyContentUser(String rootOrg, String courseId, String userId)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}

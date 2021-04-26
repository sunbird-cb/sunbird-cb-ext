package org.sunbird.assessment.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.assessment.dto.AssessmentSubmissionDTO;
import org.sunbird.assessment.repo.AssessmentRepository;
import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.service.ContentService;
import org.sunbird.common.service.UserUtilityService;
import org.sunbird.core.exception.ApplicationLogicError;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.core.logger.CbExtLogger;

@Service
public class AssessmentServiceImpl implements AssessmentService {

	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	@Autowired
	AssessmentRepository repository;

	@Autowired
	ContentService contentService;

	@Autowired
	UserUtilityService userUtilService;

	@Autowired
	AssessmentUtilService assessUtilServ;

	@Override
	public Map<String, Object> submitAssessment(String rootOrg, AssessmentSubmissionDTO data, String userId)
			throws Exception {
		logger.info("Submit Assessment: rootOrg: " + rootOrg + ", userId: " + userId + ", data: " + data.toString());
		// Check User exists
		if (!userUtilService.validateUser(rootOrg, userId)) {
			throw new BadRequestException("Invalid UserId.");
		}

		Map<String, Object> ret = new HashMap<String, Object>();

		// TODO - Need to get the Assessment ContentMeta Data
		// Get the assessment-key.json file. Current version has both the answers

		Map<String, Object> resultMap = assessUtilServ.validateAssessment(data.getQuestions());
		Double result = (Double) resultMap.get("result");
		Integer correct = (Integer) resultMap.get("correct");
		Integer blank = (Integer) resultMap.get("blank");
		Integer inCorrect = (Integer) resultMap.get("incorrect");

		Map<String, Object> persist = new HashMap<String, Object>();

		// Fetch parent of an assessment with status live
		String parentId = "";
		try {
			SunbirdApiResp contentHierarchy = contentService.getHeirarchyResponse(data.getIdentifier());
			if (contentHierarchy != null) {
				parentId = contentHierarchy.getResult().getContent().getParent();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		if (parentId == null) {
			parentId = "";
		}
		persist.put("parent", parentId);
		persist.put("result", result);
		persist.put("sourceId", data.getIdentifier());
		persist.put("title", data.getTitle());
		persist.put("rootOrg", rootOrg);
		persist.put("userId", userId);
		persist.put("correct", correct);
		persist.put("blank", blank);
		persist.put("incorrect", inCorrect);

		if (data.isAssessment() && !"".equals(parentId)) {
			// get parent data for assessment
			try {
				SunbirdApiResp contentHierarchy = contentService.getHeirarchyResponse(parentId);
				if (contentHierarchy != null) {
					persist.put("parentContentType", contentHierarchy.getResult().getContent().getContentType());
				}
			} catch (Exception e) {
				logger.error(e);
			}
		} else {
			persist.put("parentContentType", "");
		}

		logger.info("Trying to persist assessment data -> " + persist.toString());
		// insert into assessment table
		repository.insertQuizOrAssessment(persist, data.isAssessment());

		ret.put("result", result);
		ret.put("correct", correct);
		ret.put("inCorrect", inCorrect);
		ret.put("blank", blank);
		ret.put("total", blank + inCorrect + correct);
		ret.put("passPercent", 60);

		return ret;
	}

	@Override
	public Map<String, Object> getAssessmentByContentUser(String rootOrg, String courseId, String userId)
			throws Exception {
		Map<String, Object> result = new TreeMap<>();
		try {
			// get all submission data from cassandra
			List<Map<String, Object>> assessmentResults = repository.getAssessmetbyContentUser(rootOrg, courseId,
					userId);
			// retain only those fields that need to be sent to front end
			List<Map<String, Object>> assessments = getAssessments(assessmentResults);

			// initialize variables to calculate first attempt and max score
			Integer noOfAttemptsForPass = 0;
			Integer noOfAttemptsForMaxScore = 0;
			boolean passed = false;
			Object firstPassTs = null;
			BigDecimal max = new BigDecimal(-Double.MIN_VALUE);
			Object maxScoreTs = null;

			/*
			 * Logic to Find The First Time Passed and The Max Score Attained along with
			 * Their No of Attempts and Timestamps
			 */
			for (int i = assessments.size() - 1; i > -1; i--) {
				Map<String, Object> row = assessments.get(i);
				BigDecimal percentage = (BigDecimal) row.get("result");
				/*
				 * Logic to Obtain the First Pass using a Passed flag to attain the Attempts as
				 * well as the first Time passed Time Stamp
				 */
				if (!passed) {
					noOfAttemptsForPass++;
					if (percentage.doubleValue() >= 60.0) {
						passed = true;
						firstPassTs = row.get("takenOn");
					}
				}

				/*
				 * Logic to Obtain the max scored assessment comparison to attain the Attempts
				 * as well as the Max Scored Assessment Time Stamp
				 */
				if (max.compareTo(percentage) < 0) {
					max = (BigDecimal) row.get("result");
					maxScoreTs = row.get("takenOn");
					noOfAttemptsForMaxScore = (assessments.size() - i);
				}
			}

			/* Populating the Response to give Processed Data to Front End */
			if (assessments.size() > 0) {
				if (passed) {
					result.put("firstPassOn", firstPassTs);
					result.put("attemptsToPass", noOfAttemptsForPass);
				}

				result.put("maxScore", max);
				result.put("maxScoreAttainedOn", maxScoreTs);
				result.put("attemptsForMaxScore", noOfAttemptsForMaxScore);
			}
			result.put("pastAssessments", assessments);
		} catch (NullPointerException e) {
			throw new ApplicationLogicError("REQUEST_COULD_NOT_BE_PROCESSED", e);
		}
		return result;
	}

	@Override
	public Map<String, Object> submitAssessmentByIframe(String rootOrg, Map<String, Object> request) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	// A method to Format Data in the FrontEndFormat
	private List<Map<String, Object>> getAssessments(List<Map<String, Object>> result) {
		List<Map<String, Object>> assessments = new ArrayList<>();
		for (Map<String, Object> map : result) {
			Map<String, Object> assessmentData = new HashMap<>();
			String res = map.get("result_percent").toString();
			assessmentData.put("result", new BigDecimal(res).setScale(2, BigDecimal.ROUND_UP));
			assessmentData.put("correctlyAnswered", map.get("correct_count"));
			assessmentData.put("wronglyAnswered", map.get("incorrect_count"));
			assessmentData.put("notAttempted", map.get("not_answered_count"));
			assessmentData.put("takenOn", map.get("ts_created"));
			assessments.add(assessmentData);
		}
		return assessments;
	}

}

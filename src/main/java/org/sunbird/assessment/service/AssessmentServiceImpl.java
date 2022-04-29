package org.sunbird.assessment.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.sunbird.assessment.dto.AssessmentSubmissionDTO;
import org.sunbird.assessment.model.QuestionSet;
import org.sunbird.assessment.repo.AssessmentRepository;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.common.model.SunbirdApiHierarchyResultContent;
import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.service.ContentService;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.exception.ApplicationLogicError;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.user.service.UserUtilityService;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AssessmentServiceImpl implements AssessmentService {

	public static final String RESULT = "result";
	public static final String CORRECT = "correct";
	public static final String BLANK = "blank";
	public static final String TAKEN_ON = "takenOn";

	private CbExtLogger logger = new CbExtLogger(getClass().getName());
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	AssessmentRepository repository;

	@Autowired
	ContentService contentService;

	@Autowired
	UserUtilityService userUtilService;

	@Autowired
	AssessmentUtilService assessUtilServ;

	@Autowired
	OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	@Autowired
	CbExtServerProperties extServerProperties;

	@Autowired
	RedisCacheMgr redisCacheMgr;

	@Override
	public Map<String, Object> submitAssessment(String rootOrg, AssessmentSubmissionDTO data, String userId)
			throws Exception {
		logger.info("Submit Assessment: rootOrg: " + rootOrg + ", userId: " + userId + ", data: " + data.toString());
		// Check User exists
		if (!userUtilService.validateUser(rootOrg, userId)) {
			throw new BadRequestException("Invalid UserId.");
		}

		Map<String, Object> ret = new HashMap<>();

		// TODO - Need to get the Assessment ContentMeta Data
		// Get the assessment-key.json file. Current version has both the answers

		Map<String, Object> resultMap = assessUtilServ.validateAssessment(data.getQuestions());
		Double result = (Double) resultMap.get(RESULT);
		Integer correct = (Integer) resultMap.get(CORRECT);
		Integer blank = (Integer) resultMap.get(BLANK);
		Integer inCorrect = (Integer) resultMap.get("incorrect");

		Map<String, Object> persist = new HashMap<>();

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
		persist.put(RESULT, result);
		persist.put("sourceId", data.getIdentifier());
		persist.put("title", data.getTitle());
		persist.put("rootOrg", rootOrg);
		persist.put("userId", userId);
		persist.put(CORRECT, correct);
		persist.put(BLANK, blank);
		persist.put("incorrect", inCorrect);

		if (Boolean.TRUE.equals(data.isAssessment()) && !"".equals(parentId)) {
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

		ret.put(RESULT, result);
		ret.put(CORRECT, correct);
		ret.put("inCorrect", inCorrect);
		ret.put(BLANK, blank);
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
			List<Map<String, Object>> assessmentResults = repository.getAssessmentbyContentUser(rootOrg, courseId,
					userId);
			// retain only those fields that need to be sent to front end
			List<Map<String, Object>> assessments = getAssessments(assessmentResults);

			// initialize variables to calculate first attempt and max score
			Integer noOfAttemptsForPass = 0;
			Integer noOfAttemptsForMaxScore = 0;
			boolean passed = false;
			Object firstPassTs = null;
			BigDecimal max = BigDecimal.valueOf(-Double.MIN_VALUE);
			Object maxScoreTs = null;

			/*
			 * Logic to Find The First Time Passed and The Max Score Attained along with
			 * Their No of Attempts and Timestamps
			 */
			for (int i = assessments.size() - 1; i > -1; i--) {
				Map<String, Object> row = assessments.get(i);
				BigDecimal percentage = (BigDecimal) row.get(RESULT);
				/*
				 * Logic to Obtain the First Pass using a Passed flag to attain the Attempts as
				 * well as the first Time passed Time Stamp
				 */
				if (!passed) {
					noOfAttemptsForPass++;
					if (percentage.doubleValue() >= 60.0) {
						passed = true;
						firstPassTs = row.get(TAKEN_ON);
					}
				}

				/*
				 * Logic to Obtain the max scored assessment comparison to attain the Attempts
				 * as well as the Max Scored Assessment Time Stamp
				 */
				if (max.compareTo(percentage) < 0) {
					max = (BigDecimal) row.get(RESULT);
					maxScoreTs = row.get(TAKEN_ON);
					noOfAttemptsForMaxScore = (assessments.size() - i);
				}
			}

			/* Populating the Response to give Processed Data to Front End */
			if (!CollectionUtils.isEmpty(assessments)) {
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
			assessmentData.put(RESULT, new BigDecimal(res).setScale(2, BigDecimal.ROUND_UP));
			assessmentData.put("correctlyAnswered", map.get("correct_count"));
			assessmentData.put("wronglyAnswered", map.get("incorrect_count"));
			assessmentData.put("notAttempted", map.get("not_answered_count"));
			assessmentData.put(TAKEN_ON, map.get("ts_created"));
			assessments.add(assessmentData);
		}
		return assessments;
	}

	@Override
	public Map<String, Object> getAssessmentContent(String courseId, String assessmentContentId) {
		Map<String, Object> result = new HashMap<>();
		try {
			Object assessmentQuestionSet = redisCacheMgr.getCache(Constants.ASSESSMENT_QNS_SET + assessmentContentId);

			if (ObjectUtils.isEmpty(assessmentQuestionSet)) {
				String serviceURL = extServerProperties.getKmBaseHost()
						+ extServerProperties.getContentHierarchyDetailEndPoint();
				serviceURL = (serviceURL.replace("{courseId}", courseId)).replace("{hierarchyType}", "detail");
				SunbirdApiResp response = mapper.convertValue(
						outboundRequestHandlerService.fetchUsingGetWithHeaders(serviceURL, new HashMap<>()),
						SunbirdApiResp.class);

				if (response.getResponseCode().equalsIgnoreCase("Ok")) {
					// get course content
					List<SunbirdApiHierarchyResultContent> children = response.getResult().getContent().getChildren();
					for (SunbirdApiHierarchyResultContent child : children) {
						// get assessment content with id
						if (child.getIdentifier().equals(assessmentContentId)
								&& child.getArtifactUrl().endsWith(".json")) {
							// read assessment json file
							QuestionSet assessmentContent = mapper.convertValue(outboundRequestHandlerService
									.fetchUsingGetWithHeaders(child.getArtifactUrl(), new HashMap<>()),
									QuestionSet.class);

							QuestionSet assessmentQnsSet = assessUtilServ.removeAssessmentAnsKey(assessmentContent);
							result.put(Constants.STATUS, Constants.SUCCESSFUL);
							result.put(Constants.QUESTION_SET, assessmentQnsSet);
							// cache the response
							redisCacheMgr.putCache(Constants.ASSESSMENT_QNS_ANS_SET + assessmentContentId,
									assessmentContent);
							redisCacheMgr.putCache(Constants.ASSESSMENT_QNS_SET + assessmentContentId, assessmentQnsSet);
						}
					}
				}

			} else {
				result.put(Constants.STATUS, Constants.SUCCESSFUL);
				result.put(Constants.QUESTION_SET, assessmentQuestionSet);
			}

			return result;
		} catch (Exception e) {
			logger.error(e);
		}
		result.put(Constants.STATUS, Constants.FAILED);
		return result;
	}

}

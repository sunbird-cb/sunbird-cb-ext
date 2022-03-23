package org.sunbird.assessment.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.service.ContentService;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.exception.ApplicationLogicError;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.user.service.UserUtilityService;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@SuppressWarnings("unchecked")
public class AssessmentServiceV2Impl implements AssessmentServiceV2 {

	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	private static final String PRIMARY_CATEGORY = "primaryCategory";
	private static final String OBJECT_TYPE = "objectType";
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	UserUtilityService userUtilService;

	@Autowired
	ContentService contentService;

	@Autowired
	AssessmentUtilServiceV2 assessUtilServ;

	@Autowired
	RedisCacheMgr redisCacheMgr;

	@Autowired
	CbExtServerProperties cbExtServerProperties;

	@Autowired
	OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	public SBApiResponse readAssessment(String assessmentIdentifier, String token) throws Exception {
		try {
			Map<String, Object> assessmentAllDetail = (Map<String, Object>) redisCacheMgr
					.getCache(Constants.ASSESSMENT_ID + assessmentIdentifier);
			boolean isSuccess = true;
			if (ObjectUtils.isEmpty(assessmentAllDetail)) {
				Map<String, Object> hierarcyReadApiResponse = getReadHierarchyApiResponse(assessmentIdentifier, token);
				if (!Constants.OK.equalsIgnoreCase((String) hierarcyReadApiResponse.get(Constants.RESPONSE_CODE))) {
					isSuccess = false;
				} else {
					assessmentAllDetail = (Map<String, Object>) ((Map<String, Object>) hierarcyReadApiResponse
							.get(Constants.RESULT)).get(Constants.QUESTION_SET);
					redisCacheMgr.putCache(Constants.ASSESSMENT_ID + assessmentIdentifier, assessmentAllDetail);
				}
			}

			SBApiResponse response = prepareAssessmentResponse(assessmentAllDetail, isSuccess);
			// TODO - Need to parse the token and get USER_ID
			redisCacheMgr.putCache(Constants.USER_ASSESS_REQ + token, response.getResult().get(Constants.QUESTION_SET));
			return response;
		} catch (Exception e) {
			logger.error(e);
			throw new ApplicationLogicError("REQUEST_COULD_NOT_BE_PROCESSED", e);
		}
	}

	public SBApiResponse readQuestionList(Map<String, Object> requestBody, String authUserToken) throws Exception {
		try {
			List<String> identifierList = getQuestionIdList(requestBody);
			List<Map<String, Object>> updatedQuestionMapList = new ArrayList<Map<String, Object>>();
			boolean isSuccess = true;
			for (String questionId : identifierList) {
				Map<String, Object> questionMap = (Map<String, Object>) redisCacheMgr
						.getCache(Constants.QUESTION_ID + questionId);
				if (ObjectUtils.isEmpty(questionMap)) {
					Map<String, Object> questionMapResponse = readQuestionDetails(questionId);
					if (!Constants.OK.equalsIgnoreCase((String) questionMapResponse.get(Constants.RESPONSE_CODE))) {
						isSuccess = false;
					} else {
						questionMap = ((List<Map<String, Object>>) ((Map<String, Object>) questionMapResponse
								.get(Constants.RESULT)).get("questions")).get(0);
						if (!ObjectUtils.isEmpty(questionMap)) {
							redisCacheMgr.putCache(Constants.QUESTION_ID + questionId, questionMap);
						} else {
							// Log Error
							logger.error(new Exception("Failed to get Question Details for Id: " + questionId));
							isSuccess = false;
						}
					}
				}
				if (isSuccess) {
					updatedQuestionMapList.add(filterQuestionMapDetail(questionMap));
				}
				isSuccess = true;
			}
			isSuccess = updatedQuestionMapList.size() > 0 ? true : false;
			return prepareQuestionResponse(updatedQuestionMapList, isSuccess);
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}

	@Override
	public SBApiResponse submitAssessment(Map<String, Object> data, String authUserToken) throws Exception {
		// logger.info("Submit Assessment: userId: " + userId + ", data: " +
		// data.toString());
		// Check User exists
		// if (!userUtilService.validateUser(userId)) {
		// throw new BadRequestException("Invalid UserId.");
		// }

		SBApiResponse outgoingResponse = new SBApiResponse();
		outgoingResponse.setResponseCode(HttpStatus.OK);

		String assessmentId = (String) data.get(Constants.IDENTIFIER);

		// TODO - Need to get this from RedisCache
		Map<String, Object> assessmentHierarchy = (Map<String, Object>) redisCacheMgr
				.getCache(Constants.ASSESSMENT_ID + assessmentId);
		outgoingResponse.getResult().put(Constants.IDENTIFIER, assessmentId);
		outgoingResponse.getResult().put(OBJECT_TYPE, (String) assessmentHierarchy.get(OBJECT_TYPE));
		outgoingResponse.getResult().put(PRIMARY_CATEGORY, (String) assessmentHierarchy.get(PRIMARY_CATEGORY));

		// Check Sections are available
		if (data.containsKey("children")
				&& !CollectionUtils.isEmpty((List<Map<String, Object>>) data.get("children"))) {
			List<Map<String, Object>> sectionList = (List<Map<String, Object>>) data.get("children");

			for (Map<String, Object> section : sectionList) {
				String id = (String) section.get(Constants.IDENTIFIER);
				String scoreCutOffType = ((String) section.get("scoreCutoffType")).toLowerCase();
				switch (scoreCutOffType) {
				case Constants.ASSESSMENT_LEVEL_SCORE_CUTOFF: {
					if (sectionList.size() > 1) {
						// There should be only one section -- if not -- throw error
					}
					validateAssessmentLevelScore(outgoingResponse, section, assessmentHierarchy);
				}
					break;
				case Constants.SECTION_LEVEL_SCORE_CUTOFF: {
				}
					break;
				default:
					break;
				}
			}
		} else {
			// TODO
			// At least one section details should be available in the submit request...
			// throw error if no section details.
		}

		return outgoingResponse;
	}

	private Map<String, Object> getReadHierarchyApiResponse(String assessmentIdentifier, String token) {
		StringBuilder sbUrl = new StringBuilder(cbExtServerProperties.getAssessmentHost());
		sbUrl.append(cbExtServerProperties.getAssessmentHierarchyReadPath());
		String serviceURL = sbUrl.toString().replace(Constants.IDENTIFIER_REPLACER, assessmentIdentifier);
		Map<String, String> headers = new HashMap<>();
		headers.put(Constants.X_AUTH_TOKEN, token);
		headers.put(Constants.AUTHORIZATION, cbExtServerProperties.getSbApiKey());
		return mapper.convertValue(outboundRequestHandlerService.fetchUsingGetWithHeaders(serviceURL, headers),
				Map.class);
	}

	private SBApiResponse prepareAssessmentResponse(Map<String, Object> hierarchyResponse, boolean isSuccess) {
		SBApiResponse outgoingResponse = new SBApiResponse();
		outgoingResponse.setId("api.questionset.hierarchy.get");
		outgoingResponse.setVer("3.0");
		outgoingResponse.getParams().setResmsgid(UUID.randomUUID().toString());
		if (isSuccess) {
			outgoingResponse.getParams().setStatus("Success");
			outgoingResponse.setResponseCode(HttpStatus.OK);
			readAssessmentLevelData(hierarchyResponse, outgoingResponse);
		} else {
			outgoingResponse.getParams().setStatus("Failed");
			outgoingResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return outgoingResponse;
	}

	private void readAssessmentLevelData(Map<String, Object> assessmentAllDetail, SBApiResponse outgoingResponse) {
		List<String> assessmentParams = cbExtServerProperties.getAssessmentLevelParams();
		Map<String, Object> assessmentFilteredDetail = new HashMap<>();
		for (String assessmentParam : assessmentParams) {
			if ((assessmentAllDetail.containsKey(assessmentParam))) {
				assessmentFilteredDetail.put(assessmentParam, assessmentAllDetail.get(assessmentParam));
			}
		}
		readSectionLevelParams(assessmentAllDetail, assessmentFilteredDetail);
		outgoingResponse.getResult().put(Constants.QUESTION_SET, assessmentFilteredDetail);
	}

	private void readSectionLevelParams(Map<String, Object> assessmentAllDetail,
			Map<String, Object> assessmentFilteredDetail) {
		List<Map<String, Object>> sectionResponse = new ArrayList<>();
		List<String> sectionParams = cbExtServerProperties.getAssessmentSectionParams();
		List<Map<String, Object>> sections = (List<Map<String, Object>>) assessmentAllDetail.get(Constants.CHILDREN);
		List<String> sectionIdList = new ArrayList<String>();
		for (Map<String, Object> section : sections) {
			sectionIdList.add((String) section.get(Constants.IDENTIFIER));
			Map<String, Object> newSection = new HashMap<>();
			for (String sectionParam : sectionParams) {
				if (section.containsKey(sectionParam)) {
					newSection.put(sectionParam, section.get(sectionParam));
				}
			}

			// Process the children of section (i.e. question) and create a list of
			// questionId.
			// From this list, get random of maxQuestions
			List<String> allQuestionIdList = new ArrayList<String>();
			List<Map<String, Object>> questions = (List<Map<String, Object>>) section.get(Constants.CHILDREN);
			for (Map<String, Object> question : questions) {
				allQuestionIdList.add((String) question.get(Constants.IDENTIFIER));
			}
			Collections.shuffle(allQuestionIdList);
			int maxQuestions = (int) section.get(Constants.MAX_QUESTIONS);
			newSection.put(Constants.CHILD_NODES,
					allQuestionIdList.stream().limit(maxQuestions).collect(Collectors.toList()));

			sectionResponse.add(newSection);
		}
		assessmentFilteredDetail.put(Constants.CHILDREN, sectionResponse);
		assessmentFilteredDetail.put(Constants.CHILD_NODES, sectionIdList);
	}

	private List<String> getQuestionIdList(Map<String, Object> questionListReqeust) throws Exception {
		if (questionListReqeust.containsKey(Constants.REQUEST)) {
			Map<String, Object> request = (Map<String, Object>) questionListReqeust.get(Constants.REQUEST);
			if ((!ObjectUtils.isEmpty(request)) && request.containsKey("search")) {
				Map<String, Object> searchObj = (Map<String, Object>) request.get("search");
				if (!ObjectUtils.isEmpty(searchObj) && searchObj.containsKey(Constants.IDENTIFIER)) {
					List<String> identifierList = (List<String>) searchObj.get(Constants.IDENTIFIER);
					if (!CollectionUtils.isEmpty(identifierList)) {
						return identifierList;
					}
				}
			}
		}
		throw new Exception("Failed to process the questionList request body.");
	}

	private Map<String, Object> readQuestionDetails(String questionId) {
		StringBuilder sbUrl = new StringBuilder(cbExtServerProperties.getAssessmentHost());
		sbUrl.append(cbExtServerProperties.getAssessmentQuestionListPath());
		Map<String, String> headers = new HashMap<>();
		headers.put(Constants.AUTHORIZATION, cbExtServerProperties.getSbApiKey());
		Map<String, Object> requestBody = new HashMap<String, Object>();
		Map<String, Object> requestData = new HashMap<String, Object>();
		Map<String, Object> searchData = new HashMap<String, Object>();
		searchData.put(Constants.IDENTIFIER, Arrays.asList(questionId));
		requestData.put("search", searchData);
		requestBody.put("request", requestData);
		return mapper.convertValue(
				outboundRequestHandlerService.fetchResultUsingPost(sbUrl.toString(), requestBody, headers), Map.class);
	}

	private Map<String, Object> filterQuestionMapDetail(Map<String, Object> questionMapResponse) {
		List<String> questionParams = cbExtServerProperties.getAssessmentQuestionParams();
		Map<String, Object> updatedQuestionMap = new HashMap<String, Object>();
		for (String questionParam : questionParams) {
			if (questionMapResponse.containsKey(questionParam)) {
				updatedQuestionMap.put(questionParam, questionMapResponse.get(questionParam));
			}
		}
		if (questionMapResponse.containsKey(Constants.EDITOR_STATE)) {
			// We need to strip the answer from editorState
			Map<String, Object> editorStateObj = (Map<String, Object>) questionMapResponse.get(Constants.EDITOR_STATE);
			Map<String, Object> updatedEditorStateMap = new HashMap<String, Object>();

			if (editorStateObj.containsKey(Constants.QUESTION)) {
				updatedEditorStateMap.put(Constants.QUESTION, editorStateObj.get(Constants.QUESTION));
			}
			if (editorStateObj.containsKey(Constants.OPTIONS)) {
				List<Map<String, Object>> updatedOptionMapList = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> optionsMapList = (List<Map<String, Object>>) editorStateObj
						.get(Constants.OPTIONS);
				for (Map<String, Object> optionMap : optionsMapList) {
					Map<String, Object> updatedOptionMap = new HashMap<String, Object>();
					if (optionMap.containsKey(Constants.VALUE)) {
						updatedOptionMap.put(Constants.VALUE, optionMap.get(Constants.VALUE));
					}
					updatedOptionMapList.add(updatedOptionMap);
				}
				updatedEditorStateMap.put(Constants.OPTIONS, updatedOptionMapList);
			}
			updatedQuestionMap.put(Constants.EDITOR_STATE, updatedEditorStateMap);
		}
		return updatedQuestionMap;
	}

	private SBApiResponse prepareQuestionResponse(List<Map<String, Object>> updatedQuestionList, boolean isSuccess) {
		SBApiResponse outgoingResponse = new SBApiResponse();
		outgoingResponse.setId("api.questions.list");
		outgoingResponse.setVer("3.0");
		if (isSuccess) {
			outgoingResponse.setResponseCode(HttpStatus.OK);
			outgoingResponse.getResult().put("questions", updatedQuestionList);
		} else {
			outgoingResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return outgoingResponse;
	}

	private void validateSectionLevelScore(SBApiResponse outgoingResponse, Map<String, Object> userSectionData,
			SunbirdApiResp assessmentHierarchy) {
	}

	private void validateAssessmentLevelScore(SBApiResponse outgoingResponse, Map<String, Object> userSectionData,
			Map<String, Object> assessmentHierarchy) {
		// First Get the Hierarchy of given AssessmentId
		List<Map<String, Object>> hierarchySectionList = (List<Map<String, Object>>) assessmentHierarchy
				.get(Constants.CHILDREN);
		if (CollectionUtils.isEmpty(hierarchySectionList)) {
			logger.error(new Exception("There are no section details in Assessment hierarchy."));
			// TODO Throw error
			return;
		}
		String userSectionId = (String) userSectionData.get(Constants.IDENTIFIER);

		Map<String, Object> hierarchySection = null;
		for (Map<String, Object> section : hierarchySectionList) {
			String hierarchySectionId = (String) section.get(Constants.IDENTIFIER);
			if (userSectionId.equalsIgnoreCase(hierarchySectionId)) {
				hierarchySection = section;
				break;
			}
		}

		if (ObjectUtils.isEmpty(hierarchySection)) {
			// TODO - throw error
			return;
		}

		// We have both hierarchySection and userSection
		// Get the list of question Identifier's from userSectionData
		List<String> questionIdList = new ArrayList<String>();
		List<Map<String, Object>> userQuestionList = (List<Map<String, Object>>) hierarchySection.get("children");
		for (Map<String, Object> question : userQuestionList) {
			questionIdList.add((String) question.get(Constants.IDENTIFIER));
		}

		// We have both answer and user given data. This needs to be compared and result
		// should be return.
		Map<String, Object> resultMap = assessUtilServ.validateQumlAssessment(questionIdList,
				(List<Map<String, Object>>) userSectionData.get("children"));

		Double result = (Double) resultMap.get("result");
		int passPercentage = (Integer) hierarchySection.get("minimumPassPercentage");
		Map<String, Object> sectionLevelResult = new HashMap<String, Object>();
		sectionLevelResult.put(Constants.IDENTIFIER, hierarchySection.get(Constants.IDENTIFIER));
		sectionLevelResult.put(OBJECT_TYPE, hierarchySection.get(OBJECT_TYPE));
		sectionLevelResult.put(PRIMARY_CATEGORY, hierarchySection.get(PRIMARY_CATEGORY));
		sectionLevelResult.put("scoreCutoffType", hierarchySection.get("scoreCutoffType"));
		sectionLevelResult.put("passPercentage", passPercentage);
		sectionLevelResult.put("result", result);
		sectionLevelResult.put("total", resultMap.get("total"));
		sectionLevelResult.put("blank", resultMap.get("blank"));
		sectionLevelResult.put("correct", resultMap.get("correct"));
		sectionLevelResult.put("passPercentage", hierarchySection.get("minimumPassPercentage"));
		sectionLevelResult.put("incorrect", resultMap.get("incorrect"));
		sectionLevelResult.put("pass", (result >= passPercentage) ? true : false);

		List<Map<String, Object>> sectionChildren = new ArrayList<Map<String, Object>>();
		sectionChildren.add(sectionLevelResult);
		outgoingResponse.getResult().put("children", sectionChildren);

		outgoingResponse.getResult().put("overallResult", result);
		outgoingResponse.getResult().put("total", resultMap.get("total"));
		outgoingResponse.getResult().put("blank", resultMap.get("blank"));
		outgoingResponse.getResult().put("correct", resultMap.get("correct"));
		outgoingResponse.getResult().put("passPercentage", hierarchySection.get("minimumPassPercentage"));
		outgoingResponse.getResult().put("incorrect", resultMap.get("incorrect"));
		outgoingResponse.getResult().put("pass", (result >= passPercentage) ? true : false);
	}

}

package org.sunbird.assessment.service;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.exception.ApplicationLogicError;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
public class AssessmentUtilServiceV2Impl implements AssessmentUtilServiceV2 {
	@Autowired
	RedisCacheMgr redisCacheMgr;

	@Autowired
	CbExtServerProperties serverProperties;

	@Autowired
	OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	private Logger logger = LoggerFactory.getLogger(AssessmentUtilServiceV2Impl.class);

	public Map<String, Object> validateQumlAssessment(List<String> originalQuestionList,
													  List<Map<String, Object>> userQuestionList) {
		try {
			Integer correct = 0;
			Integer blank = 0;
			Integer inCorrect = 0;
			Double result;
			Integer total = 0;
			Map<String, Object> resultMap = new HashMap<>();
			Map<String, Object> answers = getQumlAnswers(originalQuestionList);
			for (Map<String, Object> question : userQuestionList) {
				List<String> marked = new ArrayList<>();
				if (question.containsKey(Constants.QUESTION_TYPE)) {
					String questionType = ((String) question.get(Constants.QUESTION_TYPE)).toLowerCase();
					Map<String, Object> editorStateObj = (Map<String, Object>) question.get(Constants.EDITOR_STATE);
					List<Map<String, Object>> options = (List<Map<String, Object>>) editorStateObj.get(Constants.OPTIONS);
					switch (questionType) {
						case Constants.MTF:
							for (Map<String, Object> option : options) {
								marked.add(option.get(Constants.INDEX).toString() + "-"
										+ option.get(Constants.SELECTED_ANSWER).toString().toLowerCase());
							}
							break;
						case Constants.FTB:
							for (Map<String, Object> option : options) {
								marked.add((String) option.get(Constants.SELECTED_ANSWER));
							}
							break;
						case Constants.MCQ_SCA:
						case Constants.MCQ_MCA:
							for (Map<String, Object> option : options) {
								if ((boolean) option.get(Constants.SELECTED_ANSWER)) {
									marked.add((String) option.get(Constants.INDEX));
								}
							}
							break;
						default:
							break;
					}
				}
				if (CollectionUtils.isEmpty(marked))
					blank++;
				else {
					List<String> answer = (List<String>) answers.get(question.get(Constants.IDENTIFIER));
					if (answer.size() > 1)
						Collections.sort(answer);
					if (marked.size() > 1)
						Collections.sort(marked);
					if (answer.equals(marked))
						correct++;
					else
						inCorrect++;
				}
			}
			// Increment the blank counter for skipped question objects
			if (answers.size() > userQuestionList.size()) {
				blank += answers.size() - userQuestionList.size();
			}
			total = correct + blank + inCorrect;
			resultMap.put(Constants.RESULT, ((correct * 100d) / total));
			resultMap.put(Constants.INCORRECT, inCorrect);
			resultMap.put(Constants.BLANK, blank);
			resultMap.put(Constants.CORRECT, correct);
			resultMap.put(Constants.TOTAL, total);
			return resultMap;

		} catch (Exception ex) {
			logger.error("Error when verifying assessment. Error : ");
		}
		return new HashMap<>();
	}


	private Map<String, Object> getQumlAnswers(List<String> questions) throws Exception {
		Map<String, Object> ret = new HashMap<>();
		for (String questionId : questions) {
			List<String> correctOption = new ArrayList<>();

			Map<String, Object> question = (Map<String, Object>) redisCacheMgr
					.getCache(Constants.QUESTION_ID + questionId);
			if (ObjectUtils.isEmpty(question)) {
				logger.error("Failed to get the answer for question: " + questionId);
				//call the assessment question list api
			}
			if (question.containsKey(Constants.QUESTION_TYPE)) {
				String questionType = ((String) question.get(Constants.QUESTION_TYPE)).toLowerCase();
				Map<String, Object> editorStateObj = (Map<String, Object>) question.get(Constants.EDITOR_STATE);
				List<Map<String, Object>> options = (List<Map<String, Object>>) editorStateObj.get(Constants.OPTIONS);
				switch (questionType) {
					case Constants.MTF:
						for (Map<String, Object> option : options) {
							Map<String, Object> valueObj = (Map<String, Object>) option.get(Constants.VALUE);
							correctOption.add(
									valueObj.get(Constants.VALUE).toString() + "-" + option.get(Constants.ANSWER).toString().toLowerCase());
						}
						break;
					case Constants.FTB:
						for (Map<String, Object> option : options) {
							if ((boolean) option.get(Constants.ANSWER)) {
								Map<String, Object> valueObj = (Map<String, Object>) option.get(Constants.VALUE);
								correctOption.add(
										valueObj.get(Constants.BODY).toString());
							}
						}
						break;
					case Constants.MCQ_SCA:
					case Constants.MCQ_MCA:
						for (Map<String, Object> option : options) {
							if ((boolean) option.get(Constants.ANSWER)) {
								Map<String, Object> valueObj = (Map<String, Object>) option.get(Constants.VALUE);
								correctOption.add(valueObj.get(Constants.VALUE).toString());
							}
						}
						break;
					default:
						break;
				}
			} else {
				for (Map<String, Object> options : (List<Map<String, Object>>) question.get(Constants.OPTIONS)) {
					if ((boolean) options.get(Constants.IS_CORRECT))
						correctOption.add(options.get(Constants.OPTION_ID).toString());
				}
			}
			ret.put(question.get(Constants.IDENTIFIER).toString(), correctOption);
		}

		return ret;
	}

	@Override
	public String fetchQuestionIdentifierValue(List<String> identifierList, List<Object> questionList) throws Exception {
		List<String> newIdentifierList = new ArrayList<>();
		List<Object> map = redisCacheMgr.mget(identifierList);
		for (int i = 0; i < map.size(); i++) {
			if (ObjectUtils.isEmpty(map.get(i))) {
				//Adding the not found keys as a seperate list
				newIdentifierList.add(identifierList.get(i));
			} else {
				//Filtering the details of the questions which are found in redis and adding them to another list
				questionList.add(filterQuestionMapDetail((Map<String, Object>) map.get(i)));
			}
		}
		//Taking the list which was formed with the not found values in Redis, we are making an internal POST call to Question List API to fetch the details
		if (!newIdentifierList.isEmpty()) {
			Map<String, Object> questionMapResponse = readQuestionDetails(newIdentifierList);
			if (!ObjectUtils.isEmpty(questionMapResponse) && Constants.OK.equalsIgnoreCase((String) questionMapResponse.get(Constants.RESPONSE_CODE))) {
				List<Map<String, Object>> questionMap = ((List<Map<String, Object>>) ((Map<String, Object>) questionMapResponse
						.get(Constants.RESULT)).get(Constants.QUESTIONS));
				for (Map<String, Object> question : questionMap) {
					if (!ObjectUtils.isEmpty(questionMap)) {
						Boolean isInsertedToRedis = redisCacheMgr.putCache(Constants.QUESTION_ID + question.get(Constants.IDENTIFIER), question);
						if (!isInsertedToRedis)
							return "Failed to insert question data into redis cache/Please check your connection";
						questionList.add(filterQuestionMapDetail(question));
					} else {
						logger.error(String.format("Failed to get Question Details for Id: %s", question.get(Constants.IDENTIFIER).toString()));
						return "Failed to get Question Details for Id: %s";
					}
				}
			} else {
				logger.error(String.format("Failed to get Question Details from the Question List API for the IDs: %s", newIdentifierList.toString()));
				return "Failed to get Question Details from the Question List API for the IDs";
			}
		}
		return "";
	}

	private Map<String, Object> filterQuestionMapDetail(Map<String, Object> questionMapResponse) {
		List<String> questionParams = serverProperties.getAssessmentQuestionParams();
		Map<String, Object> updatedQuestionMap = new HashMap<>();
		for (String questionParam : questionParams) {
			if (questionMapResponse.containsKey(questionParam)) {
				updatedQuestionMap.put(questionParam, questionMapResponse.get(questionParam));
			}
		}
		if (questionMapResponse.containsKey(Constants.CHOICES) && updatedQuestionMap.containsKey(Constants.PRIMARY_CATEGORY) && !updatedQuestionMap.get(Constants.PRIMARY_CATEGORY).toString().equalsIgnoreCase(Constants.FTB_QUESTION)) {
			Map<String, Object> choicesObj = (Map<String, Object>) questionMapResponse.get(Constants.CHOICES);
			Map<String, Object> updatedChoicesMap = new HashMap<>();
			if (choicesObj.containsKey(Constants.OPTIONS)) {
				List<Map<String, Object>> optionsMapList = (List<Map<String, Object>>) choicesObj
						.get(Constants.OPTIONS);
				updatedChoicesMap.put(Constants.OPTIONS, optionsMapList);
			}
			updatedQuestionMap.put(Constants.CHOICES, updatedChoicesMap);
		}
		if (questionMapResponse.containsKey(Constants.RHS_CHOICES) && updatedQuestionMap.containsKey(Constants.PRIMARY_CATEGORY) && updatedQuestionMap.get(Constants.PRIMARY_CATEGORY).toString().equalsIgnoreCase(Constants.MTF_QUESTION)) {
			List<Object> rhsChoicesObj = (List<Object>) questionMapResponse.get(Constants.RHS_CHOICES);
			updatedQuestionMap.put(Constants.RHS_CHOICES, rhsChoicesObj);
		}

		return updatedQuestionMap;
	}

	private Map<String, Object> readQuestionDetails(List<String> identifiers) {
		try {
			StringBuilder sbUrl = new StringBuilder(serverProperties.getAssessmentHost());
			sbUrl.append(serverProperties.getAssessmentQuestionListPath());
			Map<String, String> headers = new HashMap<>();
			headers.put(Constants.AUTHORIZATION, serverProperties.getSbApiKey());
			Map<String, Object> requestBody = new HashMap<>();
			Map<String, Object> requestData = new HashMap<>();
			Map<String, Object> searchData = new HashMap<>();
			searchData.put(Constants.IDENTIFIER, identifiers);
			requestData.put(Constants.SEARCH, searchData);
			requestBody.put(Constants.REQUEST, requestData);
			return outboundRequestHandlerService.fetchResultUsingPost(sbUrl.toString(), requestBody, headers);
		} catch (Exception e) {
			logger.info(String.format("Failed to process the readQuestionDetails. %s", e.getMessage()));
		}
		return Collections.emptyMap();
	}

	@Override
	public Map<String, Object> getReadHierarchyApiResponse(String assessmentIdentifier, String token) {
		try {
			StringBuilder sbUrl = new StringBuilder(serverProperties.getAssessmentHost());
			sbUrl.append(serverProperties.getAssessmentHierarchyReadPath());
			String serviceURL = sbUrl.toString().replace(Constants.IDENTIFIER_REPLACER, assessmentIdentifier);
			Map<String, String> headers = new HashMap<>();
			headers.put(Constants.X_AUTH_TOKEN, token);
			headers.put(Constants.AUTHORIZATION, serverProperties.getSbApiKey());
			Object o = outboundRequestHandlerService.fetchUsingGetWithHeaders(serviceURL, headers);
			return new ObjectMapper().convertValue(o, Map.class);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return new HashMap<>();
	}
}

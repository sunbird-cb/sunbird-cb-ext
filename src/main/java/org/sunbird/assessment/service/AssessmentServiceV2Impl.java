package org.sunbird.assessment.service;

import static java.util.stream.Collectors.toList;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.assessment.repo.AssessmentRepository;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.RequestInterceptor;
import org.sunbird.core.producer.Producer;

import com.beust.jcommander.internal.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
@SuppressWarnings("unchecked")
public class AssessmentServiceV2Impl implements AssessmentServiceV2 {

	private final Logger logger = LoggerFactory.getLogger(AssessmentServiceV2Impl.class);

	@Autowired
	AssessmentUtilServiceV2 assessUtilServ;

	@Autowired
	CbExtServerProperties serverProperties;

	@Autowired
	Producer kafkaProducer;

	@Autowired
	AssessmentRepository assessmentRepository;
	
	@Autowired
	RequestInterceptor requestInterceptor;

	public SBApiResponse readAssessment(String assessmentIdentifier, String token) {
		logger.info("AssessmentServiceV2Impl::readAssessment... Started");
		SBApiResponse response = createDefaultResponse(Constants.API_QUESTIONSET_HIERARCHY_GET);
		String errMsg;
		try {
			String userId = validateAuthTokenAndFetchUserId(token);
			if (userId != null) {
				logger.info("readAssessment.. userId :" + userId);
				Map<String, Object> assessmentAllDetail = new HashMap<>();
				errMsg = fetchReadHierarchyDetails(assessmentAllDetail, token, assessmentIdentifier);
				if (errMsg.isEmpty() && !((String) assessmentAllDetail.get(Constants.PRIMARY_CATEGORY))
						.equalsIgnoreCase(Constants.PRACTICE_QUESTION_SET)) {
					logger.info("Fetched assessment Details... for : " + assessmentIdentifier);
					List<Map<String, Object>> existingDataList = assessmentRepository
							.fetchUserAssessmentDataFromDB(userId, assessmentIdentifier);
					Timestamp assessmentStartTime = new Timestamp(new Date().getTime());
					if (existingDataList.isEmpty()) {
						logger.info("Assessment read first time for user.");
						response.getResult().put(Constants.QUESTION_SET, readAssessmentLevelData(assessmentAllDetail));
						int expectedDuration = (Integer) assessmentAllDetail.get(Constants.EXPECTED_DURATION);
						Boolean isAssessmentUpdatedToDB = assessmentRepository.addUserAssesmentDataToDB(userId,
								assessmentIdentifier, assessmentStartTime,
								calculateAssessmentSubmitTime(expectedDuration, assessmentStartTime),
								(Map<String, Object>) (response.getResult().get(Constants.QUESTION_SET)),
								Constants.NOT_SUBMITTED);
						if (Boolean.FALSE.equals(isAssessmentUpdatedToDB)) {
							errMsg = Constants.ASSESSMENT_DATA_START_TIME_NOT_UPDATED;
						}
					} else {
						logger.info("Assessment read... user has details... ");
						Date existingAssessmentEndTime = (Date) (existingDataList.get(0).get(Constants.END_TIME));
						int time = assessmentStartTime.compareTo(existingAssessmentEndTime);
						if (time < 0 && ((String) existingDataList.get(0).get(Constants.STATUS))
								.equalsIgnoreCase(Constants.NOT_SUBMITTED)) {
							String questionSetFromAssessmentString = (String) existingDataList.get(0)
									.get(Constants.ASSESSMENT_READ_RESPONSE);
							Map<String, Object> questionSetFromAssessment = new Gson().fromJson(
									questionSetFromAssessmentString, new TypeToken<HashMap<String, Object>>() {
									}.getType());
							response.getResult().put(Constants.QUESTION_SET, questionSetFromAssessment);
						} else {
							logger.info("Assessment read... adding user data to db...");
							response.getResult().put(Constants.QUESTION_SET,
									readAssessmentLevelData(assessmentAllDetail));
							int expectedDuration = (Integer) assessmentAllDetail.get(Constants.EXPECTED_DURATION);
							Boolean isAssessmentUpdatedToDB = assessmentRepository.addUserAssesmentDataToDB(userId,
									assessmentIdentifier, assessmentStartTime,
									calculateAssessmentSubmitTime(expectedDuration, assessmentStartTime),
									(Map<String, Object>) (response.getResult().get(Constants.QUESTION_SET)),
									Constants.NOT_SUBMITTED);
							if (Boolean.FALSE.equals(isAssessmentUpdatedToDB)) {
								errMsg = Constants.ASSESSMENT_DATA_START_TIME_NOT_UPDATED;
							}
						}
					}
				} else if (errMsg.isEmpty() && ((String) assessmentAllDetail.get(Constants.PRIMARY_CATEGORY))
						.equalsIgnoreCase(Constants.PRACTICE_QUESTION_SET)) {
					response.getResult().put(Constants.QUESTION_SET, readAssessmentLevelData(assessmentAllDetail));
				}
			} else {
				errMsg = Constants.USER_ID_DOESNT_EXIST;
			}
		} catch (Exception e) {
			logger.error(String.format("Exception in %s : %s", "read Assessment", e.getMessage()), e);
			errMsg = "Failed to read Assessment. Exception: " + e.getMessage();
		}
		if (StringUtils.isNotBlank(errMsg)) {
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	public SBApiResponse readQuestionList(Map<String, Object> requestBody, String authUserToken) {
		SBApiResponse response = createDefaultResponse(Constants.API_SUBMIT_ASSESSMENT);
		String errMsg;
		String primaryCategory = "";
		Map<String, String> result = new HashMap<>();
		try {
			List<String> identifierList = new ArrayList<>();
			List<Object> questionList = new ArrayList<>();
			result = validateQuestionListAPI(requestBody, authUserToken, identifierList);
			errMsg = result.get(Constants.ERROR_MESSAGE);
			if (errMsg.isEmpty()) {
				if(result.containsKey(Constants.PRIMARY_CATEGORY) && result.get(Constants.PRIMARY_CATEGORY).equalsIgnoreCase(Constants.PRACTICE_QUESTION_SET))
					primaryCategory = result.get(Constants.PRIMARY_CATEGORY);
				errMsg = assessUtilServ.fetchQuestionIdentifierValue(identifierList, questionList, primaryCategory);
				if (errMsg.isEmpty() && identifierList.size() == questionList.size()) {
					response.getResult().put(Constants.QUESTIONS, questionList);
				}
			}
		} catch (Exception e) {
			logger.error(String.format("Exception in %s : %s", "get Question List", e.getMessage()), e);
			errMsg = "Failed to fetch the question list. Exception: " + e.getMessage();
		}
		if (StringUtils.isNotBlank(errMsg)) {
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
		}
		return response;

	}

	private String validateAuthTokenAndFetchUserId(String authUserToken) {
		return requestInterceptor.fetchUserIdFromAccessToken(authUserToken);
	}

	private String fetchReadHierarchyDetails(Map<String, Object> assessmentAllDetail, String token,
			String assessmentIdentifier) {
		Map<String, Object> readHierarchyApiResponse = assessUtilServ.getReadHierarchyApiResponse(assessmentIdentifier,
				token);
		if (readHierarchyApiResponse.isEmpty()
				|| !Constants.OK.equalsIgnoreCase((String) readHierarchyApiResponse.get(Constants.RESPONSE_CODE))) {
			return Constants.ASSESSMENT_HIERARCHY_READ_FAILED;
		}
		assessmentAllDetail
				.putAll((Map<String, Object>) ((Map<String, Object>) readHierarchyApiResponse.get(Constants.RESULT))
						.get(Constants.QUESTION_SET));

		return StringUtils.EMPTY;
	}

	private Map<String, String> validateQuestionListAPI(Map<String, Object> requestBody, String authUserToken,
			List<String> identifierList) {
		Map<String, String> result = new HashMap<>();
		String userId = validateAuthTokenAndFetchUserId(authUserToken);
		if (StringUtils.isBlank(userId)) {
			result.put(Constants.ERROR_MESSAGE, Constants.USER_ID_DOESNT_EXIST);
			return result;
		}

		if (StringUtils.isBlank((String) requestBody.get(Constants.ASSESSMENT_ID_KEY))) {
			result.put(Constants.ERROR_MESSAGE, Constants.ASSESSMENT_ID_KEY_IS_NOT_PRESENT_IS_EMPTY);
			return result;
		}

		identifierList.addAll(getQuestionIdList(requestBody));
		if (identifierList.isEmpty()) {
			result.put(Constants.ERROR_MESSAGE, Constants.IDENTIFIER_LIST_IS_EMPTY);
			return result;
		}

		Map<String, Object> assessmentDetail = new HashMap<>();
		fetchReadHierarchyDetails(assessmentDetail, authUserToken,
				(String) requestBody.get(Constants.ASSESSMENT_ID_KEY));

		if (ObjectUtils.isEmpty(assessmentDetail)) {
			result.put(Constants.ERROR_MESSAGE, Constants.ASSESSMENT_HIERARCHY_READ_FAILED);
			return result;
		}

		if (!((String) assessmentDetail.get(Constants.PRIMARY_CATEGORY))
				.equalsIgnoreCase(Constants.PRACTICE_QUESTION_SET)) {
			List<Map<String, Object>> existingDataList = assessmentRepository.fetchUserAssessmentDataFromDB(userId,
					(String) requestBody.get(Constants.ASSESSMENT_ID_KEY));
			String questionSetFromAssessmentString = (!existingDataList.isEmpty())
					? (String) existingDataList.get(0).get(Constants.ASSESSMENT_READ_RESPONSE)
					: "";
			if (!questionSetFromAssessmentString.isEmpty()) {
				Map<String, Object> questionSetFromAssessment = new Gson().fromJson(questionSetFromAssessmentString,
						new TypeToken<HashMap<String, Object>>() {
						}.getType());
				List<String> questionsFromAssessment = new ArrayList<>();
				List<Map<String, Object>> sections = (List<Map<String, Object>>) questionSetFromAssessment
						.get(Constants.CHILDREN);
				for (Map<String, Object> section : sections) {
					questionsFromAssessment.addAll((List<String>) section.get(Constants.CHILD_NODES));
				}
				// Out of the list of questions received in the payload, checking if the request
				// has only those ids which are a part of the user's latest assessment
				// Fetching all the remaining questions details from the Redis
				if (Boolean.FALSE.equals(validateQuestionListRequest(identifierList, questionsFromAssessment))) {
					result.put(Constants.ERROR_MESSAGE, Constants.THE_QUESTIONS_IDS_PROVIDED_DONT_MATCH);
					return result;
				}
			} else {
				result.put(Constants.ERROR_MESSAGE, Constants.ASSESSMENT_ID_INVALID_SESSION_EXPIRED);
				return result;
			}
		}
		else
		{
			result.put(Constants.PRIMARY_CATEGORY, Constants.PRACTICE_QUESTION_SET);
		}
		result.put(Constants.ERROR_MESSAGE, "");
		return result;
	}

	@Override
	public SBApiResponse submitAssessment(Map<String, Object> submitRequest, String authUserToken) {
		SBApiResponse outgoingResponse = createDefaultResponse(Constants.API_SUBMIT_ASSESSMENT);
		String errMsg;
		List<Map<String, Object>> sectionListFromSubmitRequest = new ArrayList<>();
		List<Map<String, Object>> hierarchySectionList = new ArrayList<>();
		Map<String, Object> allHierarchy = new HashMap<>();
		List<String> questionsListFromAssessmentHierarchy = new ArrayList<>();
		errMsg = validateSubmitAssessmentRequest(submitRequest, authUserToken, hierarchySectionList,
				sectionListFromSubmitRequest, allHierarchy);
		if (errMsg.isEmpty()) {
			String userId = validateAuthTokenAndFetchUserId(authUserToken);
			String scoreCutOffType = ((String) allHierarchy.get(Constants.SCORE_CUTOFF_TYPE)).toLowerCase();
			List<Map<String, Object>> existingDataList = new ArrayList<>();
			List<Map<String, Object>> sectionLevelsResults = new ArrayList<>();
			for (Map<String, Object> hierarchySection : hierarchySectionList) {
				String hierarchySectionId = (String) hierarchySection.get(Constants.IDENTIFIER);
				String userSectionId = "";
				Map<String, Object> userSectionData = new HashMap<>();
				for (Map<String, Object> sectionFromSubmitRequest : sectionListFromSubmitRequest) {
					userSectionId = (String) sectionFromSubmitRequest.get(Constants.IDENTIFIER);
					if (userSectionId.equalsIgnoreCase(hierarchySectionId)) {
						userSectionData = sectionFromSubmitRequest;
						break;
					}
				}
				if (!((String) (allHierarchy.get(Constants.PRIMARY_CATEGORY)))
						.equalsIgnoreCase(Constants.PRACTICE_QUESTION_SET)) {
					existingDataList = assessmentRepository.fetchUserAssessmentDataFromDB(userId,
							(String) submitRequest.get(Constants.IDENTIFIER));
					String questionSetFromAssessmentString = (!existingDataList.isEmpty())
							? (String) existingDataList.get(0).get(Constants.ASSESSMENT_READ_RESPONSE)
							: "";
					if (!questionSetFromAssessmentString.isEmpty()) {
						Map<String, Object> questionSetFromAssessment = new Gson()
								.fromJson(questionSetFromAssessmentString, new TypeToken<HashMap<String, Object>>() {
								}.getType());
						if (questionSetFromAssessment != null
								&& questionSetFromAssessment.get(Constants.CHILDREN) != null) {
							List<Map<String, Object>> sections = (List<Map<String, Object>>) questionSetFromAssessment
									.get(Constants.CHILDREN);
							for (Map<String, Object> section : sections) {
								String sectionId = (String) section.get(Constants.IDENTIFIER);
								if (userSectionId.equalsIgnoreCase(sectionId)) {
									questionsListFromAssessmentHierarchy = (List<String>) section
											.get(Constants.CHILD_NODES);
									break;
								}
							}
						} else {
							errMsg = "Question Set From The Database returns Null";
							outgoingResponse.getResult().clear();
							break;
						}

						hierarchySection.put(Constants.SCORE_CUTOFF_TYPE, scoreCutOffType);
						List<Map<String, Object>> questionsListFromSubmitRequest = new ArrayList<>();
						if (userSectionData.containsKey(Constants.CHILDREN)
								&& !ObjectUtils.isEmpty(userSectionData.get(Constants.CHILDREN))) {
							questionsListFromSubmitRequest = (List<Map<String, Object>>) userSectionData
									.get(Constants.CHILDREN);
						}
						Map<String, Object> result = new HashMap<>();
						switch (scoreCutOffType) {
						case Constants.ASSESSMENT_LEVEL_SCORE_CUTOFF: {
							result.putAll(createResponseMapWithProperStructure(hierarchySection,
									assessUtilServ.validateQumlAssessment(questionsListFromAssessmentHierarchy,
											questionsListFromSubmitRequest)));
							outgoingResponse.getResult().putAll(calculateAssessmentFinalResults(result));
							writeDataToDatabaseAndTriggerKafkaEvent(submitRequest, userId, existingDataList, result,
									(String) allHierarchy.get(Constants.PRIMARY_CATEGORY));
							return outgoingResponse;
						}
						case Constants.SECTION_LEVEL_SCORE_CUTOFF: {
							result.putAll(createResponseMapWithProperStructure(hierarchySection,
									assessUtilServ.validateQumlAssessment(questionsListFromAssessmentHierarchy,
											questionsListFromSubmitRequest)));
							sectionLevelsResults.add(result);
						}
							break;
						default:
							break;
						}
					}
				} else {
					hierarchySection.put(Constants.SCORE_CUTOFF_TYPE, scoreCutOffType);
					List<Map<String, Object>> questionsListFromSubmitRequest = new ArrayList<>();
					if (userSectionData.containsKey(Constants.CHILDREN)
							&& !ObjectUtils.isEmpty(userSectionData.get(Constants.CHILDREN))) {
						questionsListFromSubmitRequest = (List<Map<String, Object>>) userSectionData
								.get(Constants.CHILDREN);
					}
					List<String> desiredKeys = Lists.newArrayList(Constants.IDENTIFIER);
					List<Object> questionsList = questionsListFromSubmitRequest.stream()
							.flatMap(x -> desiredKeys.stream().filter(x::containsKey).map(x::get)).collect(toList());
					questionsListFromAssessmentHierarchy = questionsList.stream()
							.map(object -> Objects.toString(object, null)).collect(Collectors.toList());
					Map<String, Object> result = new HashMap<>();
					switch (scoreCutOffType) {
					case Constants.ASSESSMENT_LEVEL_SCORE_CUTOFF: {
						result.putAll(createResponseMapWithProperStructure(hierarchySection,
								assessUtilServ.validateQumlAssessment(questionsListFromAssessmentHierarchy,
										questionsListFromSubmitRequest)));
						outgoingResponse.getResult().putAll(calculateAssessmentFinalResults(result));
						return outgoingResponse;
					}
					case Constants.SECTION_LEVEL_SCORE_CUTOFF: {
						result.putAll(createResponseMapWithProperStructure(hierarchySection,
								assessUtilServ.validateQumlAssessment(questionsListFromAssessmentHierarchy,
										questionsListFromSubmitRequest)));
						sectionLevelsResults.add(result);
					}
						break;
					default:
						break;
					}
				}
			}
			if (errMsg.isEmpty() && !ObjectUtils.isEmpty(scoreCutOffType)
					&& scoreCutOffType.equalsIgnoreCase(Constants.SECTION_LEVEL_SCORE_CUTOFF)) {
				Map<String, Object> result = calculateSectionFinalResults(sectionLevelsResults);
				outgoingResponse.getResult().putAll(result);
				writeDataToDatabaseAndTriggerKafkaEvent(submitRequest, userId, existingDataList, result,
						(String) allHierarchy.get(Constants.PRIMARY_CATEGORY));
				return outgoingResponse;
			}
		}
		if (StringUtils.isNotBlank(errMsg)) {
			outgoingResponse.getParams().setStatus(Constants.FAILED);
			outgoingResponse.getParams().setErrmsg(errMsg);
			outgoingResponse.setResponseCode(HttpStatus.BAD_REQUEST);
		}
		return outgoingResponse;
	}

	private void writeDataToDatabaseAndTriggerKafkaEvent(Map<String, Object> submitRequest, String userId,
			List<Map<String, Object>> existingDataList, Map<String, Object> result, String primaryCategory) {
		Date startTime = (!existingDataList.isEmpty()) ? (Date) existingDataList.get(0).get(Constants.START_TIME)
				: null;
		Boolean isAssessmentUpdatedToDB = assessmentRepository.updateUserAssesmentDataToDB(userId,
				(String) submitRequest.get(Constants.IDENTIFIER), submitRequest, result, Constants.SUBMITTED,
				startTime);
		if (Boolean.TRUE.equals(isAssessmentUpdatedToDB)) {
			Map<String, Object> kafkaResult = new HashMap<>();
			kafkaResult.put(Constants.CONTENT_ID_KEY, submitRequest.get(Constants.IDENTIFIER));
			kafkaResult.put(Constants.COURSE_ID, submitRequest.get(Constants.COURSE_ID));
			kafkaResult.put(Constants.BATCH_ID, submitRequest.get(Constants.BATCH_ID));
			kafkaResult.put(Constants.USER_ID, submitRequest.get(Constants.USER_ID));
			kafkaResult.put(Constants.ASSESSMENT_ID_KEY, submitRequest.get(Constants.IDENTIFIER));
			kafkaResult.put(Constants.PRIMARY_CATEGORY, primaryCategory);
			kafkaProducer.push(serverProperties.getAssessmentSubmitTopic(), kafkaResult);
		}
	}

	private String validateSubmitAssessmentRequest(Map<String, Object> submitRequest, String authUserToken,
			List<Map<String, Object>> hierarchySectionList, List<Map<String, Object>> sectionListFromSubmitRequest,
			Map<String, Object> assessmentHierarchy) {
		String userId = validateAuthTokenAndFetchUserId(authUserToken);
		if (ObjectUtils.isEmpty(userId)) {
			return Constants.USER_ID_DOESNT_EXIST;
		}
		submitRequest.put(Constants.USER_ID, userId);
		if (StringUtils.isEmpty((String) submitRequest.get(Constants.IDENTIFIER))) {
			return Constants.INVALID_ASSESSMENT_ID;
		}
		String assessmentIdFromRequest = (String) submitRequest.get(Constants.IDENTIFIER);
		String errMsg = fetchReadHierarchyDetails(assessmentHierarchy, authUserToken, assessmentIdFromRequest);
		if (!errMsg.isEmpty()) {
			return errMsg;
		}
		if (ObjectUtils.isEmpty(assessmentHierarchy)) {
			return Constants.READ_ASSESSMENT_FAILED;
		}
		hierarchySectionList.addAll((List<Map<String, Object>>) assessmentHierarchy.get(Constants.CHILDREN));
		sectionListFromSubmitRequest.addAll((List<Map<String, Object>>) submitRequest.get(Constants.CHILDREN));
		if (((String) (assessmentHierarchy.get(Constants.PRIMARY_CATEGORY)))
				.equalsIgnoreCase(Constants.PRACTICE_QUESTION_SET))
			return "";
		List<Map<String, Object>> existingDataList = assessmentRepository.fetchUserAssessmentDataFromDB(userId,
				assessmentIdFromRequest);
		Date assessmentStartTime = (!existingDataList.isEmpty())
				? (Date) existingDataList.get(0).get(Constants.START_TIME)
				: null;
		if (assessmentStartTime == null) {
			return Constants.READ_ASSESSMENT_START_TIME_FAILED;
		}
		int expectedDuration = (Integer) assessmentHierarchy.get(Constants.EXPECTED_DURATION);
		Timestamp later = calculateAssessmentSubmitTime(expectedDuration, assessmentStartTime);
		Timestamp submissionTime = new Timestamp(new Date().getTime());
		int time = submissionTime.compareTo(later);
		if (time <= 0) {
			List<String> desiredKeys = Lists.newArrayList(Constants.IDENTIFIER);
			List<Object> hierarchySectionIds = hierarchySectionList.stream()
					.flatMap(x -> desiredKeys.stream().filter(x::containsKey).map(x::get)).collect(toList());
			List<Object> submitSectionIds = sectionListFromSubmitRequest.stream()
					.flatMap(x -> desiredKeys.stream().filter(x::containsKey).map(x::get)).collect(toList());
			if (!new HashSet<>(hierarchySectionIds).containsAll(submitSectionIds)) {
				return Constants.WRONG_SECTION_DETAILS;
			} else {
				String areQuestionIdsSame = validateIfQuestionIdsAreSame(submitRequest, sectionListFromSubmitRequest,
						desiredKeys, userId);
				if (!areQuestionIdsSame.isEmpty())
					return areQuestionIdsSame;
			}
		} else {
			return Constants.ASSESSMENT_SUBMIT_EXPIRED;
		}
		return "";
	}

	private String validateIfQuestionIdsAreSame(Map<String, Object> submitRequest,
			List<Map<String, Object>> sectionListFromSubmitRequest, List<String> desiredKeys, String userId) {
		List<Map<String, Object>> existingDataList = assessmentRepository.fetchUserAssessmentDataFromDB(userId,
				(String) submitRequest.get(Constants.IDENTIFIER));
		String questionSetFromAssessmentString = (!existingDataList.isEmpty())
				? (String) existingDataList.get(0).get(Constants.ASSESSMENT_READ_RESPONSE)
				: "";
		if (!questionSetFromAssessmentString.isEmpty()) {
			Map<String, Object> questionSetFromAssessment = new Gson().fromJson(questionSetFromAssessmentString,
					new TypeToken<HashMap<String, Object>>() {
					}.getType());
			if (questionSetFromAssessment != null && questionSetFromAssessment.get(Constants.CHILDREN) != null) {
				List<Map<String, Object>> sections = (List<Map<String, Object>>) questionSetFromAssessment
						.get(Constants.CHILDREN);
				List<String> desiredKey = Lists.newArrayList(Constants.CHILD_NODES);
				List<Object> questionList = sections.stream()
						.flatMap(x -> desiredKey.stream().filter(x::containsKey).map(x::get)).collect(toList());
				List<Object> questionIdsFromAssessmentHierarchy = new ArrayList<>();
				List<Map<String, Object>> questionsListFromSubmitRequest = new ArrayList<>();
				for (Object question : questionList) {
					questionIdsFromAssessmentHierarchy.addAll((List<String>) question);
				}
				for (Map<String, Object> userSectionData : sectionListFromSubmitRequest) {
					if (userSectionData.containsKey(Constants.CHILDREN)
							&& !ObjectUtils.isEmpty(userSectionData.get(Constants.CHILDREN))) {
						questionsListFromSubmitRequest
								.addAll((List<Map<String, Object>>) userSectionData.get(Constants.CHILDREN));
					}
				}
				List<Object> userQuestionIdsFromSubmitRequest = questionsListFromSubmitRequest.stream()
						.flatMap(x -> desiredKeys.stream().filter(x::containsKey).map(x::get))
						.collect(Collectors.toList());
				if (!new HashSet<>(questionIdsFromAssessmentHierarchy).containsAll(userQuestionIdsFromSubmitRequest)) {
					return Constants.ASSESSMENT_SUBMIT_INVALID_QUESTION;
				}
			}
		} else {
			return Constants.ASSESSMENT_SUBMIT_QUESTION_READ_FAILED;
		}
		return "";
	}

	private Timestamp calculateAssessmentSubmitTime(int expectedDuration, Date assessmentStartTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(new Timestamp(assessmentStartTime.getTime()).getTime());
		if (serverProperties.getUserAssessmentSubmissionDuration().isEmpty()) {
			serverProperties.setUserAssessmentSubmissionDuration("120");
		}
		cal.add(Calendar.SECOND,
				expectedDuration + Integer.parseInt(serverProperties.getUserAssessmentSubmissionDuration()));
		return new Timestamp(cal.getTime().getTime());
	}

	private Map<String, Object> calculateAssessmentFinalResults(Map<String, Object> assessmentLevelResult) {
		Map<String, Object> res = new HashMap<>();
		try {
			res.put(Constants.CHILDREN, Collections.singletonList(assessmentLevelResult));
			Double result = (Double) assessmentLevelResult.get(Constants.RESULT);
			res.put(Constants.OVERALL_RESULT, result);
			res.put(Constants.TOTAL, assessmentLevelResult.get(Constants.TOTAL));
			res.put(Constants.BLANK, assessmentLevelResult.get(Constants.BLANK));
			res.put(Constants.CORRECT, assessmentLevelResult.get(Constants.CORRECT));
			res.put(Constants.PASS_PERCENTAGE, assessmentLevelResult.get(Constants.PASS_PERCENTAGE));
			res.put(Constants.INCORRECT, assessmentLevelResult.get(Constants.INCORRECT));
			Integer minimumPassPercentage = (Integer) assessmentLevelResult.get(Constants.PASS_PERCENTAGE);
			res.put(Constants.PASS, result >= minimumPassPercentage);
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return res;
	}

	private Map<String, Object> calculateSectionFinalResults(List<Map<String, Object>> sectionLevelResults) {
		Map<String, Object> res = new HashMap<>();
		Double result;
		Integer correct = 0;
		Integer blank = 0;
		Integer inCorrect = 0;
		Integer total = 0;
		int pass = 0;
		Double totalResult = 0.0;
		try {
			for (Map<String, Object> sectionChildren : sectionLevelResults) {
				res.put(Constants.CHILDREN, sectionLevelResults);
				result = (Double) sectionChildren.get(Constants.RESULT);
				totalResult += result;
				total += (Integer) sectionChildren.get(Constants.TOTAL);
				blank += (Integer) sectionChildren.get(Constants.BLANK);
				correct += (Integer) sectionChildren.get(Constants.CORRECT);
				inCorrect += (Integer) sectionChildren.get(Constants.INCORRECT);
				Integer minimumPassPercentage = (Integer) sectionChildren.get(Constants.PASS_PERCENTAGE);
				if (result >= minimumPassPercentage) {
					pass++;
				}
			}
			res.put(Constants.OVERALL_RESULT, totalResult / sectionLevelResults.size());
			res.put(Constants.TOTAL, total);
			res.put(Constants.BLANK, blank);
			res.put(Constants.CORRECT, correct);
			res.put(Constants.INCORRECT, inCorrect);
			res.put(Constants.PASS, (pass == sectionLevelResults.size()));
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return res;
	}

	private Map<String, Object> readAssessmentLevelData(Map<String, Object> assessmentAllDetail) {
		List<String> assessmentParams = serverProperties.getAssessmentLevelParams();
		Map<String, Object> assessmentFilteredDetail = new HashMap<>();
		for (String assessmentParam : assessmentParams) {
			if ((assessmentAllDetail.containsKey(assessmentParam))) {
				assessmentFilteredDetail.put(assessmentParam, assessmentAllDetail.get(assessmentParam));
			}
		}
		readSectionLevelParams(assessmentAllDetail, assessmentFilteredDetail);
		return assessmentFilteredDetail;
	}

	private void readSectionLevelParams(Map<String, Object> assessmentAllDetail,
			Map<String, Object> assessmentFilteredDetail) {
		List<Map<String, Object>> sectionResponse = new ArrayList<>();
		List<String> sectionIdList = new ArrayList<>();
		List<String> sectionParams = serverProperties.getAssessmentSectionParams();
		List<Map<String, Object>> sections = (List<Map<String, Object>>) assessmentAllDetail.get(Constants.CHILDREN);
		for (Map<String, Object> section : sections) {
			sectionIdList.add((String) section.get(Constants.IDENTIFIER));
			Map<String, Object> newSection = new HashMap<>();
			for (String sectionParam : sectionParams) {
				if (section.containsKey(sectionParam)) {
					newSection.put(sectionParam, section.get(sectionParam));
				}
			}
			List<String> allQuestionIdList = new ArrayList<>();
			List<Map<String, Object>> questions = (List<Map<String, Object>>) section.get(Constants.CHILDREN);
			for (Map<String, Object> question : questions) {
				allQuestionIdList.add((String) question.get(Constants.IDENTIFIER));
			}
			Collections.shuffle(allQuestionIdList);
			List<String> childNodeList = new ArrayList<>();
			if (!ObjectUtils.isEmpty(section.get(Constants.MAX_QUESTIONS))) {
				int maxQuestions = (int) section.get(Constants.MAX_QUESTIONS);
				childNodeList = allQuestionIdList.stream().limit(maxQuestions).collect(toList());
			}
			newSection.put(Constants.CHILD_NODES, childNodeList);
			sectionResponse.add(newSection);
		}
		assessmentFilteredDetail.put(Constants.CHILDREN, sectionResponse);
		assessmentFilteredDetail.put(Constants.CHILD_NODES, sectionIdList);
	}

	private List<String> getQuestionIdList(Map<String, Object> questionListRequest) {
		try {
			if (questionListRequest.containsKey(Constants.REQUEST)) {
				Map<String, Object> request = (Map<String, Object>) questionListRequest.get(Constants.REQUEST);
				if ((!ObjectUtils.isEmpty(request)) && request.containsKey(Constants.SEARCH)) {
					Map<String, Object> searchObj = (Map<String, Object>) request.get(Constants.SEARCH);
					if (!ObjectUtils.isEmpty(searchObj) && searchObj.containsKey(Constants.IDENTIFIER)
							&& !CollectionUtils.isEmpty((List<String>) searchObj.get(Constants.IDENTIFIER))) {
						return (List<String>) searchObj.get(Constants.IDENTIFIER);
					}
				}
			}
		} catch (Exception e) {
			logger.error(String.format("Failed to process the questionList request body. %s", e.getMessage()));
		}
		return Collections.emptyList();
	}

	public Map<String, Object> createResponseMapWithProperStructure(Map<String, Object> hierarchySection,
			Map<String, Object> resultMap) {
		Map<String, Object> sectionLevelResult = new HashMap<>();
		sectionLevelResult.put(Constants.IDENTIFIER, hierarchySection.get(Constants.IDENTIFIER));
		sectionLevelResult.put(Constants.OBJECT_TYPE, hierarchySection.get(Constants.OBJECT_TYPE));
		sectionLevelResult.put(Constants.PRIMARY_CATEGORY, hierarchySection.get(Constants.PRIMARY_CATEGORY));
		sectionLevelResult.put(Constants.PASS_PERCENTAGE, hierarchySection.get(Constants.MINIMUM_PASS_PERCENTAGE));
		Double result;
		if (!ObjectUtils.isEmpty(resultMap)) {
			result = (Double) resultMap.get(Constants.RESULT);
			sectionLevelResult.put(Constants.RESULT, result);
			sectionLevelResult.put(Constants.TOTAL, resultMap.get(Constants.TOTAL));
			sectionLevelResult.put(Constants.BLANK, resultMap.get(Constants.BLANK));
			sectionLevelResult.put(Constants.CORRECT, resultMap.get(Constants.CORRECT));
			sectionLevelResult.put(Constants.INCORRECT, resultMap.get(Constants.INCORRECT));
		} else {
			result = 0.0;
			sectionLevelResult.put(Constants.RESULT, result);
			List<String> childNodes = (List<String>) hierarchySection.get(Constants.CHILDREN);
			sectionLevelResult.put(Constants.TOTAL, childNodes.size());
			sectionLevelResult.put(Constants.BLANK, childNodes.size());
			sectionLevelResult.put(Constants.CORRECT, 0);
			sectionLevelResult.put(Constants.INCORRECT, 0);
		}
		sectionLevelResult.put(Constants.PASS,
				result >= ((Integer) hierarchySection.get(Constants.MINIMUM_PASS_PERCENTAGE)));
		sectionLevelResult.put(Constants.OVERALL_RESULT, result);
		return sectionLevelResult;
	}

	private SBApiResponse createDefaultResponse(String api) {
		SBApiResponse response = new SBApiResponse();
		response.setId(api);
		response.setVer(Constants.VER);
		response.getParams().setResmsgid(UUID.randomUUID().toString());
		response.getParams().setStatus(Constants.SUCCESS);
		response.setResponseCode(HttpStatus.OK);
		response.setTs(DateTime.now().toString());
		return response;
	}

	private Boolean validateQuestionListRequest(List<String> identifierList, List<String> questionsFromAssessment) {
		return (new HashSet<>(questionsFromAssessment).containsAll(identifierList)) ? Boolean.TRUE : Boolean.FALSE;
	}
}
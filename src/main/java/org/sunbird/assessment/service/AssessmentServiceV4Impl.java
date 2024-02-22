package org.sunbird.assessment.service;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.mortbay.util.ajax.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.assessment.repo.AssessmentRepository;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.*;
import org.sunbird.core.producer.Producer;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
@SuppressWarnings("unchecked")
public class AssessmentServiceV4Impl implements AssessmentServiceV4 {

    private final Logger logger = LoggerFactory.getLogger(AssessmentServiceV4Impl.class);
    @Autowired
    CbExtServerProperties serverProperties;

    @Autowired
    Producer kafkaProducer;

    @Autowired
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    @Autowired
    AssessmentUtilServiceV2 assessUtilServ;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    AssessmentRepository assessmentRepository;

    @Autowired
    AccessTokenValidator accessTokenValidator;

    @Override
    public SBApiResponse retakeAssessment(String assessmentIdentifier, String token,Boolean editMode) {
        logger.info("AssessmentServiceV4Impl::retakeAssessment... Started");
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_RETAKE_ASSESSMENT_GET);
        String errMsg = "";
        int retakeAttemptsAllowed = 0;
        int retakeAttemptsConsumed = 0;
        try {
            String userId = accessTokenValidator.fetchUserIdFromAccessToken(token);
            if (StringUtils.isBlank(userId)) {
                updateErrorDetails(response, Constants.USER_ID_DOESNT_EXIST, HttpStatus.INTERNAL_SERVER_ERROR);
                return response;
            }

            Map<String, Object> assessmentAllDetail = assessUtilServ
                    .readAssessmentHierarchyFromCache(assessmentIdentifier,editMode,token);
            if (MapUtils.isEmpty(assessmentAllDetail)) {
                updateErrorDetails(response, Constants.ASSESSMENT_HIERARCHY_READ_FAILED,
                        HttpStatus.INTERNAL_SERVER_ERROR);
                return response;
            }
            if (assessmentAllDetail.get(Constants.MAX_ASSESSMENT_RETAKE_ATTEMPTS) != null) {
                retakeAttemptsAllowed = (int) assessmentAllDetail.get(Constants.MAX_ASSESSMENT_RETAKE_ATTEMPTS);
            }
            
            if (serverProperties.isAssessmentRetakeCountVerificationEnabled()) {
                retakeAttemptsConsumed = calculateAssessmentRetakeCount(userId, assessmentIdentifier);
            }
        } catch (Exception e) {
            errMsg = String.format("Error while calculating retake assessment. Exception: %s", e.getMessage());
            logger.error(errMsg, e);
        }
        if (StringUtils.isNotBlank(errMsg)) {
            updateErrorDetails(response, errMsg, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            response.getResult().put(Constants.TOTAL_RETAKE_ATTEMPTS_ALLOWED, retakeAttemptsAllowed);
            response.getResult().put(Constants.RETAKE_ATTEMPTS_CONSUMED, retakeAttemptsConsumed);
        }
        logger.info("AssessmentServiceV4Impl::retakeAssessment... Completed");
        return response;
    }

    @Override
    public SBApiResponse readAssessment(String assessmentIdentifier, String token,boolean editMode) {
        logger.info("AssessmentServiceV4Impl::readAssessment... Started");
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_READ_ASSESSMENT);
        String errMsg = "";
        try {
            String userId = accessTokenValidator.fetchUserIdFromAccessToken(token);
            if (StringUtils.isBlank(userId)) {
                updateErrorDetails(response, Constants.USER_ID_DOESNT_EXIST, HttpStatus.INTERNAL_SERVER_ERROR);
                return response;
            }
            logger.info(String.format("ReadAssessment... UserId: %s, AssessmentIdentifier: %s", userId, assessmentIdentifier));

            Map<String, Object> assessmentAllDetail = null ;

            if(editMode) {
                assessmentAllDetail = assessUtilServ.fetchHierarchyFromAssessServc(assessmentIdentifier,token);
            }
            else {
                assessmentAllDetail = assessUtilServ
                        .readAssessmentHierarchyFromCache(assessmentIdentifier,editMode,token);
            }

            if (MapUtils.isEmpty(assessmentAllDetail)) {
                updateErrorDetails(response, Constants.ASSESSMENT_HIERARCHY_READ_FAILED,
                        HttpStatus.INTERNAL_SERVER_ERROR);
                return response;
            }

            if (Constants.PRACTICE_QUESTION_SET
                    .equalsIgnoreCase((String) assessmentAllDetail.get(Constants.PRIMARY_CATEGORY))||editMode) {
                response.getResult().put(Constants.QUESTION_SET, readAssessmentLevelData(assessmentAllDetail));
                return response;
            }

            List<Map<String, Object>> existingDataList = assessUtilServ.readUserSubmittedAssessmentRecords(
                    userId, assessmentIdentifier);
            Timestamp assessmentStartTime = new Timestamp(new java.util.Date().getTime());
            if (existingDataList.isEmpty()) {
                logger.info("Assessment read first time for user.");
                // Add Null check for expectedDuration.throw bad questionSet Assessment Exam
                if(null == assessmentAllDetail.get(Constants.EXPECTED_DURATION)){
                    errMsg = Constants.ASSESSMENT_INVALID; }
                else {
                    int expectedDuration = (Integer) assessmentAllDetail.get(Constants.EXPECTED_DURATION);
                    Timestamp assessmentEndTime = calculateAssessmentSubmitTime(expectedDuration,
                            assessmentStartTime, 0);
                    Map<String, Object> assessmentData = readAssessmentLevelData(assessmentAllDetail);
                    assessmentData.put(Constants.START_TIME, assessmentStartTime.getTime());
                    assessmentData.put(Constants.END_TIME, assessmentEndTime.getTime());
                    response.getResult().put(Constants.QUESTION_SET, assessmentData);
                    Boolean isAssessmentUpdatedToDB = assessmentRepository.addUserAssesmentDataToDB(userId,
                            assessmentIdentifier, assessmentStartTime, assessmentEndTime,
                            (Map<String, Object>) (response.getResult().get(Constants.QUESTION_SET)),
                            Constants.NOT_SUBMITTED);
                    if (Boolean.FALSE.equals(isAssessmentUpdatedToDB)) {
                        errMsg = Constants.ASSESSMENT_DATA_START_TIME_NOT_UPDATED;
                    }
                }
            } else {
                logger.info("Assessment read... user has details... ");
                java.util.Date existingAssessmentEndTime = (java.util.Date) (existingDataList.get(0)
                        .get(Constants.END_TIME));
                Timestamp existingAssessmentEndTimeTimestamp = new Timestamp(
                        existingAssessmentEndTime.getTime());
                if (assessmentStartTime.compareTo(existingAssessmentEndTimeTimestamp) < 0
                        && Constants.NOT_SUBMITTED.equalsIgnoreCase((String) existingDataList.get(0).get(Constants.STATUS))) {
                    String questionSetFromAssessmentString = (String) existingDataList.get(0)
                            .get(Constants.ASSESSMENT_READ_RESPONSE_KEY);
                    Map<String, Object> questionSetFromAssessment = new Gson().fromJson(
                            questionSetFromAssessmentString, new TypeToken<HashMap<String, Object>>() {
                            }.getType());
                    questionSetFromAssessment.put(Constants.START_TIME, assessmentStartTime.getTime());
                    questionSetFromAssessment.put(Constants.END_TIME,
                            existingAssessmentEndTimeTimestamp.getTime());
                    response.getResult().put(Constants.QUESTION_SET, questionSetFromAssessment);
                } else if ((assessmentStartTime.compareTo(existingAssessmentEndTime) < 0
                        && ((String) existingDataList.get(0).get(Constants.STATUS))
                                .equalsIgnoreCase(Constants.SUBMITTED))
                        || assessmentStartTime.compareTo(existingAssessmentEndTime) > 0) {
                    logger.info(
                            "Incase the assessment is submitted before the end time, or the endtime has exceeded, read assessment freshly ");
                    Map<String, Object> assessmentData = readAssessmentLevelData(assessmentAllDetail);
                    int expectedDuration = (Integer) assessmentAllDetail.get(Constants.EXPECTED_DURATION);
                    assessmentStartTime = new Timestamp(new java.util.Date().getTime());
                    Timestamp assessmentEndTime = calculateAssessmentSubmitTime(expectedDuration,
                            assessmentStartTime, 0);
                    assessmentData.put(Constants.START_TIME, assessmentStartTime.getTime());
                    assessmentData.put(Constants.END_TIME, assessmentEndTime.getTime());
                    response.getResult().put(Constants.QUESTION_SET, assessmentData);

                    Boolean isAssessmentUpdatedToDB = assessmentRepository.addUserAssesmentDataToDB(userId,
                            assessmentIdentifier, assessmentStartTime, assessmentEndTime,
                            assessmentData, Constants.NOT_SUBMITTED);
                    if (Boolean.FALSE.equals(isAssessmentUpdatedToDB)) {
                        errMsg = Constants.ASSESSMENT_DATA_START_TIME_NOT_UPDATED;
                    }
                }
            }
        } catch (Exception e) {
            errMsg = String.format("Error while reading assessment. Exception: %s", e.getMessage());
            logger.error(errMsg, e);
        }
        if (StringUtils.isNotBlank(errMsg)) {
            updateErrorDetails(response, errMsg, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public SBApiResponse readQuestionList(Map<String, Object> requestBody, String authUserToken,boolean editMode) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_QUESTIONS_LIST);
        String errMsg;
        Map<String, String> result = new HashMap<>();
        try {
            List<String> identifierList = new ArrayList<>();
            List<Object> questionList = new ArrayList<>();
            result = validateQuestionListAPI(requestBody, authUserToken, identifierList,editMode);
            errMsg = result.get(Constants.ERROR_MESSAGE);
            if (StringUtils.isNotBlank(errMsg)) {
                updateErrorDetails(response, errMsg, HttpStatus.BAD_REQUEST);
                return response;
            }

            String assessmentIdFromRequest = (String) requestBody.get(Constants.ASSESSMENT_ID_KEY);
            Map<String, Object> questionsMap = assessUtilServ.readQListfromCache(identifierList,assessmentIdFromRequest,editMode,authUserToken);
            for (String questionId : identifierList) {
                questionList.add(assessUtilServ.filterQuestionMapDetail((Map<String, Object>) questionsMap.get(questionId),
                        result.get(Constants.PRIMARY_CATEGORY)));
            }
            if (errMsg.isEmpty() && identifierList.size() == questionList.size()) {
                response.getResult().put(Constants.QUESTIONS, questionList);
            }
        } catch (Exception e) {
            errMsg = String.format("Failed to fetch the question list. Exception: %s", e.getMessage());
            logger.error(errMsg, e);
        }
        if (StringUtils.isNotBlank(errMsg)) {
            updateErrorDetails(response, errMsg, HttpStatus.BAD_REQUEST);
        }
        return response;
    }

    @Override
    public SBApiResponse submitAssessment(Map<String, Object> data, String userAuthToken) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_SUBMIT_ASSESSMENT);
        updateErrorDetails(response, "Method not supported", HttpStatus.NOT_IMPLEMENTED);
        return response;
    }

    public SBApiResponse readAssessmentResultV4(Map<String, Object> request, String userAuthToken) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_READ_ASSESSMENT_RESULT);
        try {
            String userId = accessTokenValidator.fetchUserIdFromAccessToken(userAuthToken);
            if (StringUtils.isBlank(userId)) {
                updateErrorDetails(response, Constants.USER_ID_DOESNT_EXIST, HttpStatus.INTERNAL_SERVER_ERROR);
                return response;
            }

            String errMsg = validateAssessmentReadResult(request);
            if (StringUtils.isNotBlank(errMsg)) {
                updateErrorDetails(response, errMsg, HttpStatus.BAD_REQUEST);
                return response;
            }

            Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
            String assessmentIdentifier = (String) requestBody.get(Constants.ASSESSMENT_ID_KEY);

            List<Map<String, Object>> existingDataList = assessUtilServ.readUserSubmittedAssessmentRecords(
                    userId, assessmentIdentifier);

            if (existingDataList.isEmpty()) {
                updateErrorDetails(response, Constants.USER_ASSESSMENT_DATA_NOT_PRESENT, HttpStatus.BAD_REQUEST);
                return response;
            }

            String statusOfLatestObject = (String) existingDataList.get(0).get(Constants.STATUS);
            if (!Constants.SUBMITTED.equalsIgnoreCase(statusOfLatestObject)) {
                response.getResult().put(Constants.STATUS_IS_IN_PROGRESS, true);
                return response;
            }

            String latestResponse = (String) existingDataList.get(0).get(Constants.SUBMIT_ASSESSMENT_RESPONSE_KEY);
            if (StringUtils.isNotBlank(latestResponse)) {
                response.putAll(mapper.readValue(latestResponse, new TypeReference<Map<String, Object>>() {
                }));
            }
        } catch (Exception e) {
            String errMsg = String.format("Failed to process Assessment read response. Excption: %s", e.getMessage());
            updateErrorDetails(response, errMsg, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    public SBApiResponse submitAssessmentAsync(Map<String, Object> submitRequest, String userAuthToken,boolean editMode) {
        logger.info("AssessmentServiceV4Impl::submitAssessmentAsync.. started");
        SBApiResponse outgoingResponse = ProjectUtil.createDefaultResponse(Constants.API_SUBMIT_ASSESSMENT);
        try {
            String userId = accessTokenValidator.fetchUserIdFromAccessToken(userAuthToken);
            if (ObjectUtils.isEmpty(userId)) {
                updateErrorDetails(outgoingResponse, Constants.USER_ID_DOESNT_EXIST, HttpStatus.BAD_REQUEST);
                return outgoingResponse;
            }
            String assessmentIdFromRequest = (String) submitRequest.get(Constants.IDENTIFIER);
            String errMsg;
            List<Map<String, Object>> sectionListFromSubmitRequest = new ArrayList<>();
            List<Map<String, Object>> hierarchySectionList = new ArrayList<>();
            Map<String, Object> assessmentHierarchy = new HashMap<>();
            Map<String, Object> existingAssessmentData = new HashMap<>();

            errMsg = validateSubmitAssessmentRequest(submitRequest, userId, hierarchySectionList,
                    sectionListFromSubmitRequest, assessmentHierarchy, existingAssessmentData,userAuthToken,editMode);

            if (StringUtils.isNotBlank(errMsg)) {
                updateErrorDetails(outgoingResponse, errMsg, HttpStatus.BAD_REQUEST);
                return outgoingResponse;
            }
            String assessmentPrimaryCategory = (String) assessmentHierarchy.get(Constants.PRIMARY_CATEGORY);

                String scoreCutOffType = ((String) assessmentHierarchy.get(Constants.SCORE_CUTOFF_TYPE)).toLowerCase();
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
                    List<String> questionsListFromAssessmentHierarchy = questionsList.stream()
                            .map(object -> Objects.toString(object, null)).collect(Collectors.toList());
                    Map<String, Object> result = new HashMap<>();
                    switch (scoreCutOffType) {
                        case Constants.ASSESSMENT_LEVEL_SCORE_CUTOFF: {
                            result.putAll(createResponseMapWithProperStructure(hierarchySection,
                                    assessUtilServ.validateQumlAssessment(questionsListFromAssessmentHierarchy,
                                            questionsListFromSubmitRequest,assessUtilServ.readQListfromCache(questionsListFromAssessmentHierarchy,assessmentIdFromRequest,editMode,userAuthToken))));
                            Map<String, Object> finalRes= calculateAssessmentFinalResults(result);
                            outgoingResponse.getResult().putAll(finalRes);
                            outgoingResponse.getResult().put(Constants.PRIMARY_CATEGORY, assessmentPrimaryCategory);
                            if (!Constants.PRACTICE_QUESTION_SET.equalsIgnoreCase(assessmentPrimaryCategory) && !editMode) {

                                String questionSetFromAssessmentString = (String) existingAssessmentData
                                        .get(Constants.ASSESSMENT_READ_RESPONSE_KEY);
                                Map<String,Object> questionSetFromAssessment = null;
                                if (StringUtils.isNotBlank(questionSetFromAssessmentString)) {
                                    questionSetFromAssessment = mapper.readValue(questionSetFromAssessmentString,
                                            new TypeReference<Map<String, Object>>() {
                                            });
                                }
                                writeDataToDatabaseAndTriggerKafkaEvent(submitRequest, userId, questionSetFromAssessment, finalRes,
                                        (String) assessmentHierarchy.get(Constants.PRIMARY_CATEGORY));
                            }

                            return outgoingResponse;
                        }
                        case Constants.SECTION_LEVEL_SCORE_CUTOFF: {
                            result.putAll(createResponseMapWithProperStructure(hierarchySection,
                                    assessUtilServ.validateQumlAssessment(questionsListFromAssessmentHierarchy,
                                            questionsListFromSubmitRequest,assessUtilServ.readQListfromCache(questionsListFromAssessmentHierarchy,assessmentIdFromRequest,editMode,userAuthToken))));
                            sectionLevelsResults.add(result);
                        }
                            break;
                        default:
                            break;
                    }
                }
                if (Constants.SECTION_LEVEL_SCORE_CUTOFF.equalsIgnoreCase(scoreCutOffType)) {
                    Map<String, Object> result = calculateSectionFinalResults(sectionLevelsResults);
                    outgoingResponse.getResult().putAll(result);
                    outgoingResponse.getParams().setStatus(Constants.SUCCESS);
                    outgoingResponse.setResponseCode(HttpStatus.OK);
                    outgoingResponse.getResult().put(Constants.PRIMARY_CATEGORY, assessmentPrimaryCategory);
                    if (!Constants.PRACTICE_QUESTION_SET.equalsIgnoreCase(assessmentPrimaryCategory) && !editMode) {
                        String questionSetFromAssessmentString = (String) existingAssessmentData
                                .get(Constants.ASSESSMENT_READ_RESPONSE_KEY);
                        Map<String,Object> questionSetFromAssessment = null;
                        if (StringUtils.isNotBlank(questionSetFromAssessmentString)) {
                            questionSetFromAssessment = mapper.readValue(questionSetFromAssessmentString,
                                    new TypeReference<Map<String, Object>>() {
                                    });
                        }
                        writeDataToDatabaseAndTriggerKafkaEvent(submitRequest, userId, questionSetFromAssessment, result,
                                (String) assessmentHierarchy.get(Constants.PRIMARY_CATEGORY));
                    }
                    return outgoingResponse;
                }

        } catch (Exception e) {
            String errMsg = String.format("Failed to process assessment submit request. Exception: %s", e.getMessage());
            logger.error(errMsg, e);
            updateErrorDetails(outgoingResponse, errMsg, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return outgoingResponse;
    }

    public void handleAssessmentSubmitRequest(Map<String, Object> asyncRequest,boolean editMode,String token) {
        String userId = (String) asyncRequest.get(Constants.USER_ID_CONSTANT);
        Map<String, Object> submitRequest = (Map<String, Object>) asyncRequest.get(Constants.REQUEST);

        String errMsg = "";
        try {
            String assessmentIdFromRequest = (String) submitRequest.get(Constants.IDENTIFIER);
            Map<String, Object> assessmentHierarchy = assessUtilServ
                    .readAssessmentHierarchyFromCache(assessmentIdFromRequest,editMode,token);
            if (MapUtils.isEmpty(assessmentHierarchy)) {
                logger.error(Constants.READ_ASSESSMENT_FAILED, new Exception(Constants.READ_ASSESSMENT_FAILED));
                return;
            }

            List<Map<String, Object>> hierarchySectionList = (List<Map<String, Object>>) assessmentHierarchy
                    .get(Constants.CHILDREN);
            List<Map<String, Object>> sectionListFromSubmitRequest = (List<Map<String, Object>>) submitRequest
                    .get(Constants.CHILDREN);

            List<Map<String, Object>> existingDataList = assessUtilServ.readUserSubmittedAssessmentRecords(
                    userId, assessmentIdFromRequest);
            if (existingDataList.isEmpty()) {
                errMsg = Constants.USER_ASSESSMENT_DATA_NOT_PRESENT;
                logger.error(errMsg, new Exception(errMsg));
                return;
            }

            if (Constants.SUBMITTED.equalsIgnoreCase((String) existingDataList.get(0).get(Constants.STATUS))) {
                errMsg = Constants.ASSESSMENT_ALREADY_SUBMITTED;
                logger.error(errMsg, new Exception(errMsg));
                return;
            }
            Map<String, Object> existingAssessmentData = existingDataList.get(0);

            List<String> questionsListFromAssessmentHierarchy = new ArrayList<>();

            String scoreCutOffType = ((String) assessmentHierarchy.get(Constants.SCORE_CUTOFF_TYPE)).toLowerCase();
            List<Map<String, Object>> sectionLevelsResults = new ArrayList<>();
            Map<String, Object> questionSetFromAssessment = new HashMap<>();
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
                if (!((String) (assessmentHierarchy.get(Constants.PRIMARY_CATEGORY)))
                        .equalsIgnoreCase(Constants.PRACTICE_QUESTION_SET)) {

                    String questionSetFromAssessmentString = (String) existingAssessmentData
                            .get(Constants.ASSESSMENT_READ_RESPONSE_KEY);
                    if (StringUtils.isNotBlank(questionSetFromAssessmentString)) {
                        questionSetFromAssessment = mapper.readValue(questionSetFromAssessmentString,
                                new TypeReference<Map<String, Object>>() {
                                });
                    }

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
                        errMsg = "Question Set From The Database returns Null. Failed to calculate assessment score.";
                        logger.error(errMsg, new Exception(errMsg));
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
                                            questionsListFromSubmitRequest,assessUtilServ.readQListfromCache(questionsListFromAssessmentHierarchy,assessmentIdFromRequest,editMode,token))));
                            Map<String, Object> finalResult = calculateAssessmentFinalResults(result);
                            finalResult.put(Constants.STATUS_IS_IN_PROGRESS, false);
                            writeDataToDatabaseAndTriggerKafkaEvent(submitRequest, userId, questionSetFromAssessment,
                                    finalResult, (String) assessmentHierarchy.get(Constants.PRIMARY_CATEGORY));
                        }
                        case Constants.SECTION_LEVEL_SCORE_CUTOFF: {
                            result.putAll(createResponseMapWithProperStructure(hierarchySection,
                                    assessUtilServ.validateQumlAssessment(questionsListFromAssessmentHierarchy,
                                            questionsListFromSubmitRequest,assessUtilServ.readQListfromCache(questionsListFromAssessmentHierarchy,assessmentIdFromRequest,editMode,token))));
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
                result.put(Constants.STATUS_IS_IN_PROGRESS, false);
                writeDataToDatabaseAndTriggerKafkaEvent(submitRequest, userId, questionSetFromAssessment, result,
                        (String) assessmentHierarchy.get(Constants.PRIMARY_CATEGORY));
            }
        } catch (Exception e) {
            errMsg = String.format("Failed to process assessent submit request. Exception: %s", e.getMessage());
            logger.error(errMsg, e);
        }
    }

    private void updateErrorDetails(SBApiResponse response, String errMsg, HttpStatus responseCode) {
        response.getParams().setStatus(Constants.FAILED);
        response.getParams().setErrmsg(errMsg);
        response.setResponseCode(responseCode);
    }

    private int calculateAssessmentRetakeCount(String userId, String assessmentId) {
        List<Map<String, Object>> userAssessmentDataList = assessUtilServ.readUserSubmittedAssessmentRecords(userId,
                assessmentId);
        return (int) userAssessmentDataList.stream()
                .filter(userData -> userData.containsKey(Constants.SUBMIT_ASSESSMENT_RESPONSE_KEY)
                        && null != userData.get(Constants.SUBMIT_ASSESSMENT_RESPONSE_KEY))
                .count();
    }

    private Timestamp calculateAssessmentSubmitTime(int expectedDuration, Timestamp assessmentStartTime,
            int bufferTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(assessmentStartTime.getTime());
        if (bufferTime > 0) {
            cal.add(Calendar.SECOND,
                    expectedDuration + Integer.parseInt(serverProperties.getUserAssessmentSubmissionDuration()));
        } else {
            cal.add(Calendar.SECOND, expectedDuration);
        }
        return new Timestamp(cal.getTime().getTime());
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
            List<Map<String, Object>> questions = (List<Map<String, Object>>) section.get(Constants.CHILDREN);
            int maxQuestions = (int) section.getOrDefault(Constants.MAX_QUESTIONS, questions.size());
            List<String> childNodeList = questions.stream()
                    .map(question -> (String) question.get(Constants.IDENTIFIER))
                    .limit(maxQuestions)
                    .collect(Collectors.toList());
            Collections.shuffle(childNodeList);
            newSection.put(Constants.CHILD_NODES, childNodeList);
            sectionResponse.add(newSection);
        }
        assessmentFilteredDetail.put(Constants.CHILDREN, sectionResponse);
        assessmentFilteredDetail.put(Constants.CHILD_NODES, sectionIdList);
    }

    private Map<String, String> validateQuestionListAPI(Map<String, Object> requestBody, String authUserToken,
            List<String> identifierList,boolean editMode) throws IOException {
        Map<String, String> result = new HashMap<>();
        String userId = accessTokenValidator.fetchUserIdFromAccessToken(authUserToken);
        if (StringUtils.isBlank(userId)) {
            result.put(Constants.ERROR_MESSAGE, Constants.USER_ID_DOESNT_EXIST);
            return result;
        }
        String assessmentIdFromRequest = (String) requestBody.get(Constants.ASSESSMENT_ID_KEY);
        if (StringUtils.isBlank(assessmentIdFromRequest)) {
            result.put(Constants.ERROR_MESSAGE, Constants.ASSESSMENT_ID_KEY_IS_NOT_PRESENT_IS_EMPTY);
            return result;
        }
        identifierList.addAll(getQuestionIdList(requestBody));
        if (identifierList.isEmpty()) {
            result.put(Constants.ERROR_MESSAGE, Constants.IDENTIFIER_LIST_IS_EMPTY);
            return result;
        }

        Map<String, Object> assessmentAllDetail = assessUtilServ
                .readAssessmentHierarchyFromCache(assessmentIdFromRequest,editMode,authUserToken);

        if (MapUtils.isEmpty(assessmentAllDetail)) {
            result.put(Constants.ERROR_MESSAGE, Constants.ASSESSMENT_HIERARCHY_READ_FAILED);
            return result;
        }
        String primaryCategory = (String) assessmentAllDetail.get(Constants.PRIMARY_CATEGORY);
        if (Constants.PRACTICE_QUESTION_SET
                .equalsIgnoreCase(primaryCategory)||editMode) {
            result.put(Constants.PRIMARY_CATEGORY, primaryCategory);
            result.put(Constants.ERROR_MESSAGE, StringUtils.EMPTY);
            return result;
        }

        Map<String, Object> userAssessmentAllDetail = new HashMap<>();

        List<Map<String, Object>> existingDataList = assessUtilServ.readUserSubmittedAssessmentRecords(
                userId, assessmentIdFromRequest);
        String questionSetFromAssessmentString = (!existingDataList.isEmpty())
                ? (String) existingDataList.get(0).get(Constants.ASSESSMENT_READ_RESPONSE_KEY)
                : "";
        if (StringUtils.isNotBlank(questionSetFromAssessmentString)) {
            userAssessmentAllDetail.putAll(mapper.readValue(questionSetFromAssessmentString,
                    new TypeReference<Map<String, Object>>() {
                    }));
        } else {
            result.put(Constants.ERROR_MESSAGE, Constants.USER_ASSESSMENT_DATA_NOT_PRESENT);
            return result;
        }

        if (!MapUtils.isEmpty(userAssessmentAllDetail)) {
            result.put(Constants.PRIMARY_CATEGORY, (String) userAssessmentAllDetail.get(Constants.PRIMARY_CATEGORY));
            List<String> questionsFromAssessment = new ArrayList<>();
            List<Map<String, Object>> sections = (List<Map<String, Object>>) userAssessmentAllDetail
                    .get(Constants.CHILDREN);
            for (Map<String, Object> section : sections) {
                // Out of the list of questions received in the payload, checking if the request
                // has only those ids which are a part of the user's latest assessment
                // Fetching all the remaining questions details from the Redis
                questionsFromAssessment.addAll((List<String>) section.get(Constants.CHILD_NODES));
            }
            if (validateQuestionListRequest(identifierList, questionsFromAssessment)) {
                result.put(Constants.ERROR_MESSAGE, StringUtils.EMPTY);
            } else {
                result.put(Constants.ERROR_MESSAGE, Constants.THE_QUESTIONS_IDS_PROVIDED_DONT_MATCH);
            }
            return result;
        } else {
            result.put(Constants.ERROR_MESSAGE, Constants.ASSESSMENT_ID_INVALID);
            return result;
        }
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

    private Boolean validateQuestionListRequest(List<String> identifierList, List<String> questionsFromAssessment) {
        return (new HashSet<>(questionsFromAssessment).containsAll(identifierList)) ? Boolean.TRUE : Boolean.FALSE;
    }

    private String validateSubmitAssessmentRequest(Map<String, Object> submitRequest, String userId,
            List<Map<String, Object>> hierarchySectionList, List<Map<String, Object>> sectionListFromSubmitRequest,
            Map<String, Object> assessmentHierarchy, Map<String, Object> existingAssessmentData,String token,boolean editMode) throws Exception {
        submitRequest.put(Constants.USER_ID, userId);
        if (StringUtils.isEmpty((String) submitRequest.get(Constants.IDENTIFIER))) {
            return Constants.INVALID_ASSESSMENT_ID;
        }
        String assessmentIdFromRequest = (String) submitRequest.get(Constants.IDENTIFIER);
        assessmentHierarchy.putAll(assessUtilServ.readAssessmentHierarchyFromCache(assessmentIdFromRequest,editMode,token));
        if (MapUtils.isEmpty(assessmentHierarchy)) {
            return Constants.READ_ASSESSMENT_FAILED;
        }

        hierarchySectionList.addAll((List<Map<String, Object>>) assessmentHierarchy.get(Constants.CHILDREN));
        sectionListFromSubmitRequest.addAll((List<Map<String, Object>>) submitRequest.get(Constants.CHILDREN));
        if (((String) (assessmentHierarchy.get(Constants.PRIMARY_CATEGORY)))
                .equalsIgnoreCase(Constants.PRACTICE_QUESTION_SET) || editMode)
            return "";

        List<Map<String, Object>> existingDataList = assessUtilServ.readUserSubmittedAssessmentRecords(
                userId, (String) submitRequest.get(Constants.IDENTIFIER));
        if (existingDataList.isEmpty()) {
            return Constants.USER_ASSESSMENT_DATA_NOT_PRESENT;
        } else {
            existingAssessmentData.putAll(existingDataList.get(0));
        }
        Date assessmentStartTime = (Date) existingAssessmentData.get(Constants.START_TIME);
        if (assessmentStartTime == null) {
            return Constants.READ_ASSESSMENT_START_TIME_FAILED;
        }
        int expectedDuration = (Integer) assessmentHierarchy.get(Constants.EXPECTED_DURATION);
        Timestamp later = calculateAssessmentSubmitTime(expectedDuration,
                new Timestamp(assessmentStartTime.getTime()),
                Integer.parseInt(serverProperties.getUserAssessmentSubmissionDuration()));
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
                String areQuestionIdsSame = validateIfQuestionIdsAreSame(submitRequest,
                        sectionListFromSubmitRequest, desiredKeys, userId, existingAssessmentData);
                if (!areQuestionIdsSame.isEmpty())
                    return areQuestionIdsSame;
            }
        } else {
            return Constants.ASSESSMENT_SUBMIT_EXPIRED;
        }

        return "";
    }

    private String validateIfQuestionIdsAreSame(Map<String, Object> submitRequest,
            List<Map<String, Object>> sectionListFromSubmitRequest, List<String> desiredKeys, String userId,
            Map<String, Object> existingAssessmentData) throws Exception {
        String questionSetFromAssessmentString = (String) existingAssessmentData
                .get(Constants.ASSESSMENT_READ_RESPONSE_KEY);
        if (StringUtils.isNotBlank(questionSetFromAssessmentString)) {
            Map<String, Object> questionSetFromAssessment = mapper.readValue(questionSetFromAssessmentString,
                    new TypeReference<Map<String, Object>>() {
                    });
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
            sectionLevelResult.put(Constants.CHILDREN,resultMap.get(Constants.CHILDREN));
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
            logger.error("Failed to calculate Assessment final results. Exception: ", e);
        }
        return res;
    }

    private void writeDataToDatabaseAndTriggerKafkaEvent(Map<String, Object> submitRequest, String userId,
            Map<String, Object> questionSetFromAssessment, Map<String, Object> result, String primaryCategory) {
        try {
            if (questionSetFromAssessment.get(Constants.START_TIME) != null) {
                Long existingAssessmentStartTime = (Long) questionSetFromAssessment.get(Constants.START_TIME);
                Timestamp startTime = new Timestamp(existingAssessmentStartTime);
                Boolean isAssessmentUpdatedToDB = assessmentRepository.updateUserAssesmentDataToDB(userId,
                        (String) submitRequest.get(Constants.IDENTIFIER), submitRequest, result, Constants.SUBMITTED,
                        startTime);
                if (Boolean.TRUE.equals(isAssessmentUpdatedToDB)) {
                    Map<String, Object> kafkaResult = new HashMap<>();
                    kafkaResult.put(Constants.CONTENT_ID_KEY, submitRequest.get(Constants.IDENTIFIER));
                    kafkaResult.put(Constants.COURSE_ID,
                            submitRequest.get(Constants.COURSE_ID) != null ? submitRequest.get(Constants.COURSE_ID)
                                    : "");
                    kafkaResult.put(Constants.BATCH_ID,
                            submitRequest.get(Constants.BATCH_ID) != null ? submitRequest.get(Constants.BATCH_ID) : "");
                    kafkaResult.put(Constants.USER_ID, submitRequest.get(Constants.USER_ID));
                    kafkaResult.put(Constants.ASSESSMENT_ID_KEY, submitRequest.get(Constants.IDENTIFIER));
                    kafkaResult.put(Constants.PRIMARY_CATEGORY, primaryCategory);
                    kafkaResult.put(Constants.TOTAL_SCORE, result.get(Constants.OVERALL_RESULT));
                    if ((primaryCategory.equalsIgnoreCase("Competency Assessment")
                            && submitRequest.containsKey(Constants.COMPETENCIES_V3)
                            && submitRequest.get(Constants.COMPETENCIES_V3) != null)) {
                        Object[] obj = (Object[]) JSON.parse((String) submitRequest.get(Constants.COMPETENCIES_V3));
                        if (obj != null) {
                            Object map = obj[0];
                            ObjectMapper m = new ObjectMapper();
                            Map<String, Object> props = m.convertValue(map, Map.class);
                            kafkaResult.put(Constants.COMPETENCY, props.isEmpty() ? "" : props);
                        }
                    }
                    kafkaProducer.push(serverProperties.getAssessmentSubmitTopic(), kafkaResult);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to write data for assessment submit response. Exception: ", e);
        }
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
            logger.error("Failed to calculate assessment score. Exception: ", e);
        }
        return res;
    }

    private String validateAssessmentReadResult(Map<String, Object> request) {
        String errMsg = "";
        if (MapUtils.isEmpty(request) || !request.containsKey(Constants.REQUEST)) {
            return Constants.INVALID_REQUEST;
        }

        Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
        if (MapUtils.isEmpty(requestBody)) {
            return Constants.INVALID_REQUEST;
        }
        List<String> missingAttribs = new ArrayList<>();
        if (!requestBody.containsKey(Constants.ASSESSMENT_ID_KEY)
                || StringUtils.isBlank((String) requestBody.get(Constants.ASSESSMENT_ID_KEY))) {
            missingAttribs.add(Constants.ASSESSMENT_ID_KEY);
        }

        if (!requestBody.containsKey(Constants.BATCH_ID)
                || StringUtils.isBlank((String) requestBody.get(Constants.BATCH_ID))) {
            missingAttribs.add(Constants.BATCH_ID);
        }

        if (!requestBody.containsKey(Constants.COURSE_ID)
                || StringUtils.isBlank((String) requestBody.get(Constants.COURSE_ID))) {
            missingAttribs.add(Constants.COURSE_ID);
        }

        if (!missingAttribs.isEmpty()) {
            errMsg = "One or more mandatory fields are missing in Request. Mandatory fields are : "
                    + missingAttribs.toString();
        }

        return errMsg;
    }
}

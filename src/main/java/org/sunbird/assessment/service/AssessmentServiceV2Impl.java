package org.sunbird.assessment.service;

import com.beust.jcommander.internal.Lists;
import com.google.gson.Gson;
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
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.RequestInterceptor;
import org.sunbird.core.producer.Producer;

import java.sql.Timestamp;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
@SuppressWarnings("unchecked")
public class AssessmentServiceV2Impl implements AssessmentServiceV2 {

    private Logger logger = LoggerFactory.getLogger(AssessmentServiceV2Impl.class);

    @Autowired
    AssessmentUtilServiceV2 assessUtilServ;

    @Autowired
    RedisCacheMgr redisCacheMgr;

    @Autowired
    CbExtServerProperties serverProperties;

    @Autowired
    Producer kafkaProducer;

    @Autowired
    AssessmentRepository assessmentRepository;


    public SBApiResponse readAssessment(String assessmentIdentifier, String token) {
        SBApiResponse response = createDefaultResponse(Constants.API_QUESTIONSET_HIERARCHY_GET);
        String errMsg = "";
        try {
            String userId = validateAuthTokenAndFetchUserId(token);
            if (userId != null) {
                Map<String, Object> assessmentAllDetail = new HashMap<>();
                errMsg = fetchReadHierarchyDetails(assessmentAllDetail, token, assessmentIdentifier);
                if (errMsg.isEmpty()) {
                    response.getResult().put(Constants.QUESTION_SET, readAssessmentLevelData(assessmentAllDetail));
                    Boolean isInsertedToRedis = redisCacheMgr.putCache(Constants.USER_ASSESS_REQ + assessmentIdentifier + token, response.getResult().get(Constants.QUESTION_SET));
                    if (!isInsertedToRedis) {
                        response.getResult().clear();
                        errMsg = "Data couldn't be inserted to Redis, Please check!";
                    } else {
                        Boolean isStartTimeUpdated = assessmentRepository.addUserAssesmentStartTime(userId, Constants.ASSESSMENT_ID + assessmentIdentifier, new Timestamp(new Date().getTime()));
                        if (!isStartTimeUpdated) {
                            errMsg = "Assessment Start Time not updated! Please check!";
                        }
                    }
                }
            } else {
                errMsg = "User Id doesn't exist! Please supply a valid auth token";
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
        String errMsg = "";
        try {
            List<String> identifierList = new ArrayList<>();
            List<Object> questionList = new ArrayList<>();
            errMsg = validateQuestionListAPI(requestBody, authUserToken, identifierList);
            if (errMsg.isEmpty()) {
                errMsg = assessUtilServ.fetchQuestionIdentifierValue(identifierList, questionList);
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
        return RequestInterceptor.fetchUserIdFromAccessToken(authUserToken);
    }

    private String fetchReadHierarchyDetails(Map<String, Object> assessmentHierarchy, String token, String assessmentIdentifier) {
        Map<String, Object> assessmentDetail = (Map<String, Object>) redisCacheMgr
                .getCache(Constants.ASSESSMENT_ID + assessmentIdentifier);
        if (!ObjectUtils.isEmpty(assessmentDetail)) {
            assessmentHierarchy.putAll(assessmentDetail);
        } else {
            Map<String, Object> hierarcyReadApiResponse = assessUtilServ.getReadHierarchyApiResponse(assessmentIdentifier, token);
            if (hierarcyReadApiResponse.isEmpty() || !Constants.OK.equalsIgnoreCase((String) hierarcyReadApiResponse.get(Constants.RESPONSE_CODE))) {
                return "Assessment hierarchy read failed, failed to process Assessment Read Request";
            }
            assessmentHierarchy.putAll((Map<String, Object>) ((Map<String, Object>) hierarcyReadApiResponse
                    .get(Constants.RESULT)).get(Constants.QUESTION_SET));
            redisCacheMgr.putCache(Constants.ASSESSMENT_ID + assessmentIdentifier, assessmentHierarchy);
        }
        return "";
    }

    private String validateQuestionListAPI(Map<String, Object> requestBody, String authUserToken, List<String> identifierList) {
        String userId = validateAuthTokenAndFetchUserId(authUserToken);
        if (userId != null) {
            if (requestBody.containsKey(Constants.ASSESSMENT_ID_KEY) && !StringUtils.isEmpty((String) requestBody.get(Constants.ASSESSMENT_ID_KEY))) {
                identifierList.addAll(getQuestionIdList(requestBody));
                if (!identifierList.isEmpty()) {
                    String key = Constants.USER_ASSESS_REQ + requestBody.get(Constants.ASSESSMENT_ID_KEY).toString() + authUserToken;
                    Map<String, Object> questionSetFromAssessment = (Map<String, Object>) redisCacheMgr.getCache(key);
                    if (!ObjectUtils.isEmpty(questionSetFromAssessment)) {
                        List<String> questionsFromAssessment = new ArrayList<>();
                        List<Map<String, Object>> sections = (List<Map<String, Object>>) questionSetFromAssessment.get(Constants.CHILDREN);
                        for (Map<String, Object> section : sections) {
                            questionsFromAssessment.addAll((List<String>) section.get(Constants.CHILD_NODES));
                        }
                        //Out of the list of questions received in the payload, checking if the request has only those ids which are a part of the user's latest assessment
                        //Fetching all the remaining questions details from the Redis
                        if (!validateQuestionListRequest(identifierList, questionsFromAssessment)) {
                            return "The Questions Ids Provided don't match the active user assessment session";
                        }
                    } else {
                        return "Assessment Id Invalid/Session Expired/Redis Cache doesn't have this question list details";
                    }
                } else {
                    return "Identifier List is Empty";
                }
            } else {
                return "Assessment Id Key is not present/is empty";
            }
        } else {
            return "User Id doesn't exist! Please supply a valid auth token";
        }
        return "";
    }

    @Override
    public SBApiResponse submitAssessment(Map<String, Object> submitRequest, String authUserToken) {
        SBApiResponse outgoingResponse = createDefaultResponse(Constants.API_SUBMIT_ASSESSMENT);
        String errMsg = "";
        List<Map<String, Object>> sectionListFromSubmitRequest = new ArrayList<>();
        List<Map<String, Object>> hierarchySectionList = new ArrayList<>();
        Map<String, Object> allHierarchy = new HashMap<>();
        List<String> questionsListFromAssessmentHierarchy = new ArrayList<>();
        errMsg = validateSubmitAssessmentRequest(submitRequest, authUserToken, hierarchySectionList, sectionListFromSubmitRequest, allHierarchy);
        if (errMsg.isEmpty()) {
            String scoreCutOffType = ((String) allHierarchy.get(Constants.SCORE_CUTOFF_TYPE)).toLowerCase();
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
                Map<String, Object> questionSetFromAssessment = (Map<String, Object>) redisCacheMgr.getCache(Constants.USER_ASSESS_REQ + (String) submitRequest.get(Constants.IDENTIFIER) + authUserToken);
                if (questionSetFromAssessment != null && questionSetFromAssessment.get(Constants.CHILDREN) != null) {
                    List<Map<String, Object>> sections = (List<Map<String, Object>>) questionSetFromAssessment.get(Constants.CHILDREN);
                    for (Map<String, Object> section : sections) {
                        String sectionId = (String) section.get(Constants.IDENTIFIER);
                        if (userSectionId.equalsIgnoreCase(sectionId)) {
                            questionsListFromAssessmentHierarchy = (List<String>) section.get(Constants.CHILD_NODES);
                            break;
                        }
                    }
                } else {
                    errMsg = "Question Set From The Redis returns Null";
                    outgoingResponse.getResult().clear();
                    break;
                }
                if (errMsg.isEmpty()) {
                    hierarchySection.put(Constants.SCORE_CUTOFF_TYPE, scoreCutOffType);
                    List<Map<String, Object>> questionsListFromSubmitRequest = new ArrayList<>();
                    if (userSectionData.containsKey(Constants.CHILDREN) && !ObjectUtils.isEmpty(userSectionData.get(Constants.CHILDREN))) {
                        questionsListFromSubmitRequest = (List<Map<String, Object>>) userSectionData.get(Constants.CHILDREN);
                    }
                    Map<String, Object> result = new HashMap<>();
                    switch (scoreCutOffType) {
                        case Constants.ASSESSMENT_LEVEL_SCORE_CUTOFF: {
                            result.putAll(createResponseMapWithProperStructure(hierarchySection, assessUtilServ.validateQumlAssessment(questionsListFromAssessmentHierarchy,
                                    questionsListFromSubmitRequest)));
                            outgoingResponse.getResult().putAll(calculateAssessmentFinalResults(result));
                            return outgoingResponse;
                        }
                        case Constants.SECTION_LEVEL_SCORE_CUTOFF: {
                            result.putAll(createResponseMapWithProperStructure(hierarchySection, assessUtilServ.validateQumlAssessment(questionsListFromAssessmentHierarchy,
                                    questionsListFromSubmitRequest)));
                            sectionLevelsResults.add(result);
                        }
                        break;
                        default:
                            break;
                    }
                }
            }
            if (errMsg.isEmpty() && !ObjectUtils.isEmpty(scoreCutOffType) && scoreCutOffType.equalsIgnoreCase(Constants.SECTION_LEVEL_SCORE_CUTOFF)) {
                Map<String, Object> result = calculateSectionFinalResults(sectionLevelsResults);
                outgoingResponse.getResult().putAll(result);
                Map<String, Object> kafkaResult = new HashMap<>();
                kafkaResult.put(Constants.CONTENT_ID, (String) submitRequest.get(Constants.IDENTIFIER));
                kafkaResult.put(Constants.COURSE_ID, submitRequest.get(Constants.COURSE_ID));
                kafkaResult.put(Constants.BATCH_ID, submitRequest.get(Constants.BATCH_ID));
                kafkaResult.put(Constants.USER_ID, submitRequest.get(Constants.USER_ID));
                kafkaResult.put(Constants.TOTAL_MAX_SCORE, 100.0);
                kafkaResult.put(Constants.TOTAL_SCORE, result.get(Constants.OVERALL_RESULT));
//                    String resultJson = gson.toJson(kafkaResult);
//                    try (Jedis jedis = new Jedis(Constants.HOST, 6379, 30000)) {
//                        jedis.set(Constants.USER_ASSESS_SUBMIT_REQ + authUserToken, resultJson);
//                    }
//                    JSONObject j = new JSONObject();
//                    j.put(Constants.REDIS_CACHE_ID, Constants.USER_ASSESS_SUBMIT_REQ + authUserToken);
//                    kafkaProducer.push(serverProperties.getUserAssessmentSubmitTopic(), j);
//                    logger.info(j.toJSONString());
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

    private String validateSubmitAssessmentRequest(Map<String, Object> submitRequest, String authUserToken, List<Map<String, Object>> hierarchySectionList, List<Map<String, Object>> sectionListFromSubmitRequest, Map<String, Object> assessmentHierarchy) {
        String userId = validateAuthTokenAndFetchUserId(authUserToken);
        if (ObjectUtils.isEmpty(userId)) {
            return "User Id doesn't exist! Please supply a valid auth token";
        }
        submitRequest.put(Constants.USER_ID, userId);
        if (StringUtils.isEmpty((String) submitRequest.get(Constants.IDENTIFIER))) {
            return "Invalid Assessment Id";
        }
        String assessmentIdFromRequest = (String) submitRequest.get(Constants.IDENTIFIER);
        String errMsg = fetchReadHierarchyDetails(assessmentHierarchy, authUserToken, assessmentIdFromRequest);
        if (!errMsg.isEmpty()) {
            return errMsg;
        }
        if (ObjectUtils.isEmpty(assessmentHierarchy)) {
            return "Error fetching the read assessment details from the redis cache/Wrong Assessment Id";
        }
        Date assessmentStartTime = assessmentRepository.fetchUserAssessmentStartTime(userId, Constants.ASSESSMENT_ID + assessmentIdFromRequest);
        if (assessmentStartTime == null) {
            return "Failed to communicate with the db to get the assessment start time";
        }
        int time = calculateAssessmentSubmitTime(assessmentHierarchy, assessmentStartTime);
        if (time <= 0) {
            hierarchySectionList.addAll((List<Map<String, Object>>) assessmentHierarchy
                    .get(Constants.CHILDREN));
            sectionListFromSubmitRequest.addAll((List<Map<String, Object>>) submitRequest.get(Constants.CHILDREN));
            List<String> desiredKeys = Lists.newArrayList(Constants.IDENTIFIER);
            List<Object> hierarchySectionIds = hierarchySectionList.stream()
                    .flatMap(x -> desiredKeys.stream()
                            .filter(x::containsKey)
                            .map(x::get)
                    ).collect(toList());
            List<Object> submitSectionIds = sectionListFromSubmitRequest.stream()
                    .flatMap(x -> desiredKeys.stream()
                            .filter(x::containsKey)
                            .map(x::get)
                    ).collect(toList());
            if (!hierarchySectionIds.containsAll(submitSectionIds)) {
                return "Wrong section details.";
            } else {
                String areQuestionIdsSame = validateIfQuestionIdsAreSame(submitRequest, authUserToken, sectionListFromSubmitRequest, desiredKeys);
                if (!areQuestionIdsSame.isEmpty()) return areQuestionIdsSame;
            }
        } else {
            return "The Assessment submission time-period is over! Assessment can't be submitted";
        }
        return "";
    }

    private String validateIfQuestionIdsAreSame(Map<String, Object> submitRequest, String authUserToken, List<Map<String, Object>> sectionListFromSubmitRequest, List<String> desiredKeys) {
        String key = Constants.USER_ASSESS_REQ + (String) submitRequest.get(Constants.IDENTIFIER) + authUserToken;
        Map<String, Object> questionSetFromAssessment = (Map<String, Object>) redisCacheMgr.getCache(key);

        if (questionSetFromAssessment != null && questionSetFromAssessment.get(Constants.CHILDREN) != null) {
            List<Object> userQuestionIdsFromSubmitRequest = new ArrayList<>();
            List<Map<String, Object>> sections = (List<Map<String, Object>>) questionSetFromAssessment.get(Constants.CHILDREN);
            List<String> desiredKey = Lists.newArrayList(Constants.CHILD_NODES);
            List<Object> questionList = sections.stream()
                    .flatMap(x -> desiredKey.stream()
                            .filter(x::containsKey)
                            .map(x::get)
                    ).collect(toList());
            List<Object> questionIdsFromAssessmentHierarchy = new ArrayList<>();
            List<Map<String, Object>> questionsListFromSubmitRequest = new ArrayList<>();
            for (int i = 0; i < questionList.size(); i++) {
                questionIdsFromAssessmentHierarchy.addAll((List<String>) questionList.get(i));
            }
            for (Map<String, Object> userSectionData : sectionListFromSubmitRequest) {
                if (userSectionData.containsKey(Constants.CHILDREN) && !ObjectUtils.isEmpty(userSectionData.get(Constants.CHILDREN))) {
                    questionsListFromSubmitRequest.addAll((List<Map<String, Object>>) userSectionData.get(Constants.CHILDREN));
                }
            }
            userQuestionIdsFromSubmitRequest.addAll(questionsListFromSubmitRequest.stream()
                    .flatMap(x -> desiredKeys.stream()
                            .filter(x::containsKey)
                            .map(x::get)
                    ).collect(toList()));
            if (!questionIdsFromAssessmentHierarchy.containsAll(userQuestionIdsFromSubmitRequest)) {
                return "The answers provided don't match to the Questions";
            }
        } else {
            return "Question Set From The Redis returns Null";
        }
        return "";
    }

    private int calculateAssessmentSubmitTime(Map<String, Object> assessmentHierarchy, Date assessmentStartTime) {
        Timestamp submissionTime = new Timestamp(new Date().getTime());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Timestamp(assessmentStartTime.getTime()).getTime());
        if (serverProperties.getUserAssessmentSubmissionDuration().isEmpty()) {
            serverProperties.setUserAssessmentSubmissionDuration("120");
        }
        cal.add(Calendar.SECOND, ((Integer) assessmentHierarchy.get(Constants.EXPECTED_DURATION)).intValue() + Integer.valueOf(serverProperties.getUserAssessmentSubmissionDuration()));
        Timestamp later = new Timestamp(cal.getTime().getTime());
        return submissionTime.compareTo(later);
    }

    private Map<String, Object> calculateAssessmentFinalResults(Map<String, Object> assessmentLevelResult) {
        Map<String, Object> res = new HashMap<>();
        try {
            Map<String, Object> sectionChildren = assessmentLevelResult;
            res.put(Constants.CHILDREN, Collections.singletonList(sectionChildren));
            Double result = (Double) sectionChildren.get(Constants.RESULT);
            res.put(Constants.OVERALL_RESULT, result);
            res.put(Constants.TOTAL, sectionChildren.get(Constants.TOTAL));
            res.put(Constants.BLANK, sectionChildren.get(Constants.BLANK));
            res.put(Constants.CORRECT, sectionChildren.get(Constants.CORRECT));
            res.put(Constants.PASS_PERCENTAGE, sectionChildren.get(Constants.PASS_PERCENTAGE));
            res.put(Constants.INCORRECT, sectionChildren.get(Constants.INCORRECT));
            Integer minimumPassPercentage = (Integer) sectionChildren.get(Constants.PASS_PERCENTAGE);
            res.put(Constants.PASS, result >= minimumPassPercentage);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return res;
    }

    private Map<String, Object> calculateSectionFinalResults(List<Map<String, Object>> sectionLevelResults) {
        Map<String, Object> res = new HashMap<>();
        Double result = 0.0;
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
                    if (!ObjectUtils.isEmpty(searchObj) && searchObj.containsKey(Constants.IDENTIFIER)) {
                        if (!CollectionUtils.isEmpty((List<String>) searchObj.get(Constants.IDENTIFIER))) {
                            return (List<String>) searchObj.get(Constants.IDENTIFIER);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(String.format("Failed to process the questionList request body. %s", e.getMessage()));
        }
        return Collections.emptyList();
    }

    public Map<String, Object> createResponseMapWithProperStructure(Map<String, Object> hierarchySection, Map<String, Object> resultMap) {
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
        sectionLevelResult.put(Constants.PASS, result >= ((Integer) hierarchySection.get(Constants.MINIMUM_PASS_PERCENTAGE)));
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
        return (questionsFromAssessment.containsAll(identifierList)) ? Boolean.TRUE : Boolean.FALSE;
    }
}

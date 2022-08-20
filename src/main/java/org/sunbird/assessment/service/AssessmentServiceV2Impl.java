package org.sunbird.assessment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.assessment.repo.AssessmentRepository;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.RequestInterceptor;
import org.sunbird.core.producer.Producer;
import redis.clients.jedis.Jedis;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("unchecked")
public class AssessmentServiceV2Impl implements AssessmentServiceV2 {

    private Logger logger = LoggerFactory.getLogger(AssessmentServiceV2Impl.class);

    private final ObjectMapper mapper = new ObjectMapper();

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

    @Autowired
    Gson gson;

    public SBApiResponse readAssessment(String assessmentIdentifier, String token) {
        SBApiResponse response = createDefaultResponse(Constants.API_QUESTIONSET_HIERARCHY_GET);
        String errMsg = "";
        try {
            String userId = validateAuthTokenAndFetchUserId(token);
            if (userId != null) {
                Map<String, Object> assessmentAllDetail = (Map<String, Object>) redisCacheMgr
                        .getCache(Constants.ASSESSMENT_ID + assessmentIdentifier);
                if (ObjectUtils.isEmpty(assessmentAllDetail)) {
                    Map<String, Object> hierarcyReadApiResponse = assessUtilServ.getReadHierarchyApiResponse(assessmentIdentifier, token);
                    if (hierarcyReadApiResponse.isEmpty() || !Constants.OK.equalsIgnoreCase((String) hierarcyReadApiResponse.get(Constants.RESPONSE_CODE))) {
                        errMsg = "Assessment hierarchy read failed, failed to process Assessment Read Request";
                    } else {
                        assessmentAllDetail = (Map<String, Object>) ((Map<String, Object>) hierarcyReadApiResponse
                                .get(Constants.RESULT)).get(Constants.QUESTION_SET);
                        redisCacheMgr.putCache(Constants.ASSESSMENT_ID + assessmentIdentifier, assessmentAllDetail);
                    }
                }
                if(errMsg.isEmpty()) {
                    response.getResult().put(Constants.QUESTION_SET, readAssessmentLevelData(assessmentAllDetail));
                    Boolean isInsertedToRedis = redisCacheMgr.putCache(Constants.USER_ASSESS_REQ + assessmentIdentifier + token, response.getResult().get(Constants.QUESTION_SET));
                    if (!isInsertedToRedis) {
                        response.getResult().clear();
                        errMsg = "Data couldn't be inserted to Redis, Please check!";
                    }
                    else {
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
                if(errMsg.isEmpty() && identifierList.size()==questionList.size())
                {
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

    private String validateQuestionListAPI(Map<String, Object> requestBody, String authUserToken, List<String> identifierList) {
        String userId = validateAuthTokenAndFetchUserId(authUserToken);
        if (userId != null) {
            if(requestBody.containsKey(Constants.ASSESSMENT_ID_KEY) && !StringUtils.isEmpty((String)requestBody.get(Constants.ASSESSMENT_ID_KEY))) {
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
        String scoreCutOffType = null;
        List<Map<String, Object>> sectionListFromSubmitRequest = new ArrayList<>();
        List<Map<String, Object>> hierarchySectionList = new ArrayList<>();
        Map<String, Object> assessmentHierarchy = new HashMap<>();
        errMsg = validateSubmitAssessmentRequest(submitRequest, authUserToken, outgoingResponse, hierarchySectionList, sectionListFromSubmitRequest, assessmentHierarchy);
        if (errMsg.isEmpty()) {
            List<Map<String, Object>> sectionLevelsResults = new ArrayList<>();
            List<String> questionsFromAssessment = new ArrayList<>();
                for (Map<String, Object> hierarchySection : hierarchySectionList) {
                    String hierarchySectionId = (String) hierarchySection.get(Constants.IDENTIFIER);
                    String userSectionId = null;
                    Map<String, Object> userSectionData = null;
                    for (Map<String, Object> sectionFromSubmitRequest : sectionListFromSubmitRequest) {
                        userSectionId = (String) sectionFromSubmitRequest.get(Constants.IDENTIFIER);
                        if (userSectionId.equalsIgnoreCase(hierarchySectionId)) {
                            userSectionData = sectionFromSubmitRequest;
                            break;
                        }
                    }
                    if (userSectionData == null) {
                        Map<String, Object> sectionLevelResult = createResponseMapWithProperStructure(hierarchySection, null);
                        sectionLevelsResults.add(sectionLevelResult);
                        continue;
                    }

                    // We have both userSectiondata and userSection
                    // Get the list of question Identifier's from userSectionData
                    Map<String, Object> questionSetFromAssessment = (Map<String, Object>) redisCacheMgr.getCache(Constants.USER_ASSESS_REQ +(String) submitRequest.get(Constants.IDENTIFIER)+ authUserToken);
                    if (questionSetFromAssessment != null && questionSetFromAssessment.get(Constants.CHILDREN) != null) {
                        List<Map<String, Object>> sections = (List<Map<String, Object>>) questionSetFromAssessment.get(Constants.CHILDREN);
                        if (!ObjectUtils.isEmpty(sections)) {
                            for (Map<String, Object> section : sections) {
                                String sectionId = (String) section.get(Constants.IDENTIFIER);
                                if (userSectionId.equalsIgnoreCase(sectionId)) {
                                    questionsFromAssessment.addAll((List<String>) section.get(Constants.CHILD_NODES));
                                    break;
                                }
                            }
                        } else {
                            errMsg = "Question List (Children) From The Redis returns Null/Empty";
                            outgoingResponse.getResult().clear();
                            break;
                        }
                    } else {
                        errMsg = "Question Set From The Redis returns Null";
                        outgoingResponse.getResult().clear();
                        break;
                    }
                    if (errMsg.isEmpty()) {
                        scoreCutOffType = ((String) hierarchySection.get(Constants.SCORE_CUTOFF_TYPE)).toLowerCase();
                        switch (scoreCutOffType) {
                            case Constants.ASSESSMENT_LEVEL_SCORE_CUTOFF: {
                                if (hierarchySectionList.size() > 1) {
                                    errMsg = "Hierarchy cannot have more than 1 section for assessment level cutoff";
                                } else {
                                    Map<String, Object> result = validateScores(userSectionData, hierarchySection, questionsFromAssessment);
                                    outgoingResponse.getResult().putAll(calculateAssessmentFinalResults(result));
                                    return outgoingResponse;
                                }
                            }
                            break;
                            case Constants.SECTION_LEVEL_SCORE_CUTOFF: {
                                Map<String, Object> result = validateScores(userSectionData, hierarchySection, questionsFromAssessment);
                                sectionLevelsResults.add(result);
                            }
                            break;
                            default:
                                break;
                        }
                    }
                }
                if (!ObjectUtils.isEmpty(scoreCutOffType) && scoreCutOffType.equalsIgnoreCase(Constants.SECTION_LEVEL_SCORE_CUTOFF)) {
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

    private String validateSubmitAssessmentRequest(Map<String, Object> submitRequest, String authUserToken, SBApiResponse outgoingResponse, List<Map<String, Object>> hierarchySectionList, List<Map<String, Object>> sectionListFromSubmitRequest, Map<String, Object> assessmentHierarchy) {
        String userId = validateAuthTokenAndFetchUserId(authUserToken);
        if (userId != null) {
            submitRequest.put(Constants.USER_ID, userId);
            if (submitRequest.containsKey(Constants.IDENTIFIER) && !StringUtils.isEmpty((String) submitRequest.get(Constants.IDENTIFIER))) {
                String assessmentIdFromRequest = (String) submitRequest.get(Constants.IDENTIFIER);
                assessmentHierarchy.putAll((Map<String, Object>) redisCacheMgr
                        .getCache(Constants.ASSESSMENT_ID + assessmentIdFromRequest));
                // fail if the user is null
                if (!ObjectUtils.isEmpty(assessmentHierarchy)) {
                    Date assessmentStartTime = assessmentRepository.fetchUserAssessmentStartTime(userId, Constants.ASSESSMENT_ID + assessmentIdFromRequest);
                    if (assessmentStartTime != null) {
                        int time = calculateAssessmentSubmitTime(assessmentHierarchy, assessmentStartTime);
                        if (time <= 0) {
                            hierarchySectionList.addAll((List<Map<String, Object>>) assessmentHierarchy
                                    .get(Constants.CHILDREN));
                            // Check Sections are available in the submit request or not
                            if (!CollectionUtils.isEmpty(hierarchySectionList)) {
                                if (submitRequest.containsKey(Constants.CHILDREN)
                                        && !CollectionUtils.isEmpty((List<Map<String, Object>>) submitRequest.get(Constants.CHILDREN))) {
                                    sectionListFromSubmitRequest.addAll((List<Map<String, Object>>) submitRequest.get(Constants.CHILDREN));
                                }
                                if(hierarchySectionList.size()!=sectionListFromSubmitRequest.size())
                                {
                                    return "The no of sections in the Submitted Assessment doesn't match the number of sections in Read Assessment Cache";
                                }
                            } else {
                                return "There are no sub section details in Assessment hierarchy.";
                            }
                        } else {
                            return "The Assessment submission time-period is over! Assessment can't be submitted";
                        }
                    } else {
                        return "Start Time of the Assessment For the User is Missing";
                    }
                } else {
                    return "Error fetching the read assessment details from the redis cache/Wrong Assessment Id";
                }
            } else {
                return "Assessment Id Key is not present/is empty";
            }
        } else {
            return "User Id doesn't exist! Please supply a valid auth token";
        }
        return "";
    }

    private int calculateAssessmentSubmitTime(Map<String, Object> assessmentHierarchy, Date assessmentStartTime) {
        Timestamp submissionTime = new Timestamp(new Date().getTime());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Timestamp(assessmentStartTime.getTime()).getTime());
        cal.add(Calendar.SECOND, ((Integer) assessmentHierarchy.get(Constants.EXPECTED_DURATION)).intValue() + Integer.valueOf(serverProperties.getUserAssessmentSubmissionDuration()));
        Timestamp later = new Timestamp(cal.getTime().getTime());
        return submissionTime.compareTo(later);
    }

    private Map<String, Object> calculateAssessmentFinalResults(Map<String, Object> assessmentLevelResult) {
        Map<String, Object> res = new HashMap<>();
        try {
                Map<String, Object> sectionChildren = assessmentLevelResult;
                res.put(Constants.CHILDREN, sectionChildren);
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
            for(Map<String, Object> sectionChildren : sectionLevelResults) {
                res.put(Constants.CHILDREN, sectionLevelResults);
                result = (Double) sectionChildren.get(Constants.RESULT);
                totalResult += result;
                total += (Integer) sectionChildren.get(Constants.TOTAL);
                blank += (Integer) sectionChildren.get(Constants.BLANK);
                correct += (Integer) sectionChildren.get(Constants.CORRECT);
                inCorrect += (Integer) sectionChildren.get(Constants.INCORRECT);
                Integer minimumPassPercentage = (Integer) sectionChildren.get(Constants.PASS_PERCENTAGE);
                if( result >= minimumPassPercentage)
                {
                    pass++;
                }
            }
            res.put(Constants.OVERALL_RESULT, totalResult/sectionLevelResults.size());
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
                childNodeList = allQuestionIdList.stream().limit(maxQuestions).collect(Collectors.toList());
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

    private Map<String, Object> validateScores(Map<String, Object> userSectionData,
                                               Map<String, Object> hierarchySection, List<String> questionsFromAssessment) {
            // We have both answer and user given data. This needs to be compared and result
            // should be return.
        Map<String, Object> resultMap = new HashMap<>();
        if(userSectionData.containsKey(Constants.CHILDREN) && !ObjectUtils.isEmpty(userSectionData.get(Constants.CHILDREN))) {
            resultMap = assessUtilServ.validateQumlAssessment(questionsFromAssessment,
                    (List<Map<String, Object>>) userSectionData.get(Constants.CHILDREN));
        }
        return createResponseMapWithProperStructure(hierarchySection, resultMap);
        }

    public Map<String, Object> createResponseMapWithProperStructure(Map<String, Object> hierarchySection, Map<String, Object> resultMap) {
        Map<String, Object> sectionLevelResult = new HashMap<>();
        sectionLevelResult.put(Constants.IDENTIFIER, hierarchySection.get(Constants.IDENTIFIER));
        sectionLevelResult.put(Constants.OBJECT_TYPE, hierarchySection.get(Constants.OBJECT_TYPE));
        sectionLevelResult.put(Constants.PRIMARY_CATEGORY, hierarchySection.get(Constants.PRIMARY_CATEGORY));
        sectionLevelResult.put(Constants.SCORE_CUTOFF_TYPE, hierarchySection.get(Constants.SCORE_CUTOFF_TYPE));
        sectionLevelResult.put(Constants.PASS_PERCENTAGE, hierarchySection.get(Constants.MINIMUM_PASS_PERCENTAGE));
        Double result;
        if (!resultMap.isEmpty()) {
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
        List<String> identifierListCopy = new ArrayList<>();
        identifierListCopy.addAll(identifierList);
        List<String> questionsFromAssessmentCopy = new ArrayList<>();
        questionsFromAssessmentCopy.addAll(questionsFromAssessment);
        identifierListCopy.removeAll(questionsFromAssessment);
        questionsFromAssessmentCopy.removeAll(identifierList);
        return (identifierListCopy.isEmpty() && questionsFromAssessmentCopy.isEmpty()) ? Boolean.TRUE : Boolean.FALSE;
    }
}

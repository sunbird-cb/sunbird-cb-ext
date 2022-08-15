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
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

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
                    Map<String, Object> hierarcyReadApiResponse = getReadHierarchyApiResponse(assessmentIdentifier, token);
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
                        errMsg = "Data couldn't be inserted to Redis, Please check!";
                    }
                    Boolean isStartTimeUpdated = assessmentRepository.addUserAssesmentStartTime(userId, Constants.ASSESSMENT_ID + assessmentIdentifier, new Timestamp(new Date().getTime()));
                    if (!isStartTimeUpdated) {
                        errMsg = "Assessment Start Time not updated! Please check!";
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
            List<String> newIdentifierList = new ArrayList<>();
            errMsg = validateQuestionListAPI(requestBody, authUserToken, errMsg, identifierList);
            if(!errMsg.isEmpty() && !identifierList.isEmpty()) {
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
                                questionList.add(filterQuestionMapDetail(question));
                            } else {
                                errMsg = "Failed to get Question Details for Id: %s";
                                logger.error(String.format("Failed to get Question Details for Id: %s", question.get(Constants.IDENTIFIER).toString()));
                                break;
                            }
                        }
                    } else {
                        logger.error(String.format("Failed to get Question Details from the Question List API for the IDs: %s", newIdentifierList.toString()));
                    }
                }
                if (!questionList.isEmpty() && errMsg.isEmpty()) {
                    response.getResult().put(Constants.QUESTIONS, questionList);
                } else {
                    errMsg = "The list of questions are Empty/Invalid";
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

    private String validateQuestionListAPI(Map<String, Object> requestBody, String authUserToken, String errMsg, List<String> identifierList) {
        String userId = validateAuthTokenAndFetchUserId(authUserToken);
        if (userId != null && requestBody.containsKey(Constants.ASSESSMENT_ID_KEY) && !StringUtils.isEmpty((String)requestBody.get(Constants.ASSESSMENT_ID_KEY))) {
            identifierList = getQuestionIdList(requestBody);
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
                        errMsg = "The Questions Ids Provided are not a part of the active user assessment session";
                    }
                } else {
                    errMsg = "Please provide a valid assessment Id/Session Expired";
                }
            }
        } else {
            errMsg = "User Id doesn't exist! Please supply a valid auth token";
        }
        return errMsg;
    }

    @Override
    public SBApiResponse submitAssessment(Map<String, Object> submitRequest, String authUserToken) {
        SBApiResponse outgoingResponse = createDefaultResponse(Constants.API_SUBMIT_ASSESSMENT);
        String errMsg = "";
        String scoreCutOffType = null;
        String assessmentIdFromRequest = (String) submitRequest.get(Constants.IDENTIFIER);
        Map<String, Object> assessmentHierarchy = (Map<String, Object>) redisCacheMgr
                .getCache(Constants.ASSESSMENT_ID + assessmentIdFromRequest);
        String userId = validateAuthTokenAndFetchUserId(authUserToken);
        if (userId != null) { // fail if the user is null
            Date assessmentStartTime = assessmentRepository.fetchUserAssessmentStartTime(userId, Constants.ASSESSMENT_ID + assessmentIdFromRequest);
            if (assessmentStartTime != null) {
                Timestamp submissionTime = new Timestamp(new Date().getTime());
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(new Timestamp(assessmentStartTime.getTime()).getTime());
                cal.add(Calendar.SECOND, ((Integer) assessmentHierarchy.get(Constants.EXPECTED_DURATION)).intValue() + Integer.valueOf(serverProperties.getUserAssessmentSubmissionDuration()));
                Timestamp later = new Timestamp(cal.getTime().getTime());
                int time = submissionTime.compareTo(later);
                if (time <= 0) {
                    List<String> questionsFromAssessment = new ArrayList<>();
                    outgoingResponse.getResult().put(Constants.IDENTIFIER, assessmentIdFromRequest);
                    outgoingResponse.getResult().put(Constants.OBJECT_TYPE, assessmentHierarchy.get(Constants.OBJECT_TYPE));
                    outgoingResponse.getResult().put(Constants.PRIMARY_CATEGORY, assessmentHierarchy.get(Constants.PRIMARY_CATEGORY));
                    List<Map<String, Object>> hierarchySectionList = (List<Map<String, Object>>) assessmentHierarchy
                            .get(Constants.CHILDREN);
                    // Check Sections are available in the submit request or not
                    if (!CollectionUtils.isEmpty(hierarchySectionList)) {
                        if (submitRequest.containsKey(Constants.CHILDREN)
                                && !CollectionUtils.isEmpty((List<Map<String, Object>>) submitRequest.get(Constants.CHILDREN))) {
                            List<Map<String, Object>> sectionListFromSubmitRequest = (List<Map<String, Object>>) submitRequest.get(Constants.CHILDREN);
                            List<Map<String, Object>> sectionLevelsResults = new ArrayList<>();
                            for (Map<String, Object> hierarchySection : hierarchySectionList) {
                                String hierarchySectionId = (String) hierarchySection.get(Constants.IDENTIFIER);
                                String userSectionId = null;
                                Map<String, Object> userSectionData = null;
                                for (Map<String, Object> sectionFromSubmitRequest : sectionListFromSubmitRequest) {
                                    userSectionId = (String) sectionFromSubmitRequest.get(Constants.IDENTIFIER);
                                    if (userSectionId.equalsIgnoreCase(hierarchySectionId)) {
                                        scoreCutOffType = ((String) hierarchySection.get(Constants.SCORE_CUTOFF_TYPE)).toLowerCase();
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
                                Map<String, Object> questionSetFromAssessment = (Map<String, Object>) redisCacheMgr.getCache(Constants.USER_ASSESS_REQ + authUserToken);
                                if (questionSetFromAssessment != null && questionSetFromAssessment.get(Constants.CHILDREN) != null) {
                                    List<Map<String, Object>> sections = (List<Map<String, Object>>) questionSetFromAssessment.get(Constants.CHILDREN);
                                    for (Map<String, Object> section : sections) {
                                        String sectionId = (String) section.get(Constants.IDENTIFIER);
                                        if (userSectionId.equalsIgnoreCase(sectionId)) {
                                            questionsFromAssessment.addAll((List<String>) section.get(Constants.CHILD_NODES));
                                            break;
                                        }
                                    }
                                } else {
                                    errMsg = "Question Set From The Redis returns Null";
                                }
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
                            if (!scoreCutOffType.isEmpty() && scoreCutOffType.equalsIgnoreCase(Constants.SECTION_LEVEL_SCORE_CUTOFF) && hierarchySectionList.size() - sectionLevelsResults.size() == 0) {
                                    Map<String, Object> result = calculateSectionFinalResults(sectionLevelsResults);
                                    outgoingResponse.getResult().putAll(result);
                                    Map<String, Object> kafkaResult = new HashMap<>();
                                    kafkaResult.put("contentId", assessmentIdFromRequest);
                                    kafkaResult.put(Constants.COURSE_ID, submitRequest.get(Constants.COURSE_ID));
                                    kafkaResult.put(Constants.BATCH_ID, submitRequest.get(Constants.BATCH_ID));
                                    kafkaResult.put(Constants.USER_ID, userId);
                                    kafkaResult.put("totalMaxScore", 100.0);
                                    kafkaResult.put("totalScore", result.get(Constants.OVERALL_RESULT));
                                    String resultJson = gson.toJson(kafkaResult);
                                    try (Jedis jedis = new Jedis("127.0.0.1", 6379, 30000)) {
                                        jedis.set(Constants.USER_ASSESS_SUBMIT_REQ + authUserToken, resultJson);
                                    }
                                    JSONObject j = new JSONObject();
                                    j.put("redis_cache_id", Constants.USER_ASSESS_SUBMIT_REQ + authUserToken);
                                    kafkaProducer.push(serverProperties.getUserAssessmentSubmitTopic(), j);
                                    logger.info(j.toJSONString());
                                    return outgoingResponse;
                                }
                        }
                    } else {
                        errMsg = "There are no section details in Assessment hierarchy.";
                    }
                } else {
                    errMsg = "The Assessment submission time-period is over! Assessment can't be submitted";
                }
            } else {
                errMsg = "Start Time of the Assessment For the User is Missing";
            }
        } else {
            errMsg = "User Id doesn't exist! Please supply a valid auth token";
        }
        if (StringUtils.isNotBlank(errMsg)) {
            outgoingResponse.getParams().setStatus(Constants.FAILED);
            outgoingResponse.getParams().setErrmsg(errMsg);
            outgoingResponse.setResponseCode(HttpStatus.BAD_REQUEST);
        }
        return outgoingResponse;
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

    private Map<String, Object> getReadHierarchyApiResponse(String assessmentIdentifier, String token) {
        try {
            StringBuilder sbUrl = new StringBuilder(serverProperties.getAssessmentHost());
            sbUrl.append(serverProperties.getAssessmentHierarchyReadPath());
            String serviceURL = sbUrl.toString().replace(Constants.IDENTIFIER_REPLACER, assessmentIdentifier);
            Map<String, String> headers = new HashMap<>();
            headers.put(Constants.X_AUTH_TOKEN, token);
            headers.put(Constants.AUTHORIZATION, serverProperties.getSbApiKey());
            Object o = outboundRequestHandlerService.fetchUsingGetWithHeaders(serviceURL, headers);
            return mapper.convertValue(o, Map.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return new HashMap<>();
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
                        List<String> identifierList = (List<String>) searchObj.get(Constants.IDENTIFIER);
                        if (!CollectionUtils.isEmpty(identifierList)) {
                            return identifierList;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(String.format("Failed to process the questionList request body. %s", e.getMessage()));
        }
        return Collections.emptyList();
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
            logger.info(String.format("Failed to process the readQuestionDetails. %s" , e.getMessage()));
        }
        return Collections.emptyMap();
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

    private Map<String, Object> validateScores(Map<String, Object> userSectionData,
                                               Map<String, Object> hierarchySection, List<String> questionsFromAssessment) {
            // We have both answer and user given data. This needs to be compared and result
            // should be return.
        Map<String, Object> resultMap = assessUtilServ.validateQumlAssessment(questionsFromAssessment,
                (List<Map<String, Object>>) userSectionData.get(Constants.CHILDREN));
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
        if (resultMap != null) {
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
        List<String> identifierListCopy = identifierList;
        identifierListCopy.removeAll(questionsFromAssessment);
        return identifierListCopy.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
    }
}

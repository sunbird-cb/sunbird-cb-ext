package org.sunbird.assessment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
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
import org.sunbird.core.exception.ApplicationLogicError;
import org.sunbird.core.logger.CbExtLogger;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("unchecked")
public class AssessmentServiceV2Impl implements AssessmentServiceV2 {
    private final CbExtLogger logger = new CbExtLogger(getClass().getName());

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    AssessmentUtilServiceV2 assessUtilServ;

    @Autowired
    RedisCacheMgr redisCacheMgr;

    @Autowired
    CbExtServerProperties cbExtServerProperties;

    @Autowired
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    @Autowired
    AssessmentRepository assessmentRepository;

    public SBApiResponse readAssessment(String assessmentIdentifier, String token) throws Exception {
        SBApiResponse response = new SBApiResponse();
        try {
            String userId = RequestInterceptor.fetchUserIdFromAccessToken(token);
            if (userId != null) {
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
                response = prepareAssessmentResponse(assessmentAllDetail, isSuccess);
                redisCacheMgr.putCache(Constants.USER_ASSESS_REQ + token, response.getResult().get(Constants.QUESTION_SET));
                logger.info("Adding the user assessment start time 1");
                if (assessmentAllDetail.get(Constants.EXPECTED_DURATION) != null) {
                    boolean resp = assessmentRepository.addUserAssesmentStartTime(userId, Constants.ASSESSMENT_ID + assessmentIdentifier, new Timestamp(new Date().getTime()));
                    logger.info(String.valueOf(resp));
                    return response;
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw new ApplicationLogicError("REQUEST_COULD_NOT_BE_PROCESSED", e);
        }
        return response;
    }

    public SBApiResponse readQuestionList(Map<String, Object> requestBody, String authUserToken) throws Exception {
        try {
            List<String> identifierList = getQuestionIdList(requestBody);
            List<Object> questionList = new ArrayList<>();
            List<String> newIdentifierList = new ArrayList<>();
            Map<String, Object> questionSetFromAssessment = (Map<String, Object>) redisCacheMgr.getCache(Constants.USER_ASSESS_REQ + authUserToken);
            if (questionSetFromAssessment != null && questionSetFromAssessment.get(Constants.CHILDREN) != null) {
                List<String> questionsFromAssessment = new ArrayList<>();
                List<Map<String, Object>> sections = (List<Map<String, Object>>) questionSetFromAssessment.get(Constants.CHILDREN);
                for(Map<String, Object> section :sections){
                    questionsFromAssessment.addAll((List<String>) section.get(Constants.CHILD_NODES));
                }
                identifierList.retainAll(questionsFromAssessment);
            }
            List<Object> map = redisCacheMgr.mget(identifierList);
            for (int i = 0; i < map.size(); i++) {
                if (ObjectUtils.isEmpty(map.get(i))) {
                    newIdentifierList.add(identifierList.get(i));
                } else {
                    questionList.add(filterQuestionMapDetail((Map<String, Object>) map.get(i)));
                }
            }
            if (newIdentifierList.size() > 0) {
                Map<String, Object> questionMapResponse = readQuestionDetails(newIdentifierList);
                if (questionMapResponse != null && Constants.OK.equalsIgnoreCase((String) questionMapResponse.get(Constants.RESPONSE_CODE))) {
                    List<Map<String, Object>> questionMap = ((List<Map<String, Object>>) ((Map<String, Object>) questionMapResponse
                            .get(Constants.RESULT)).get(Constants.QUESTIONS));
                    for (Map<String, Object> qmap : questionMap) {
                        if (!ObjectUtils.isEmpty(questionMap)) {
                            redisCacheMgr.putCache(Constants.QUESTION_ID + qmap.get(Constants.IDENTIFIER), qmap);
                            questionList.add(filterQuestionMapDetail(qmap));
                        } else {
                            logger.error(new Exception("Failed to get Question Details for Id: " + qmap.get(Constants.IDENTIFIER)));
                        }
                    }
                }
            }
            return prepareQuestionResponse(questionList, questionList.size() > 0);
        } catch (Exception e) {
            logger.error(e);
            throw new ApplicationLogicError("REQUEST_COULD_NOT_BE_PROCESSED", e);
        }

    }

    @Override
    public SBApiResponse submitAssessment(Map<String, Object> submitRequest, String authUserToken) throws Exception {
        SBApiResponse outgoingResponse = new SBApiResponse();
        String assessmentIdFromRequest = (String) submitRequest.get(Constants.IDENTIFIER);
        Map<String, Object> assessmentHierarchy = (Map<String, Object>) redisCacheMgr
                .getCache(Constants.ASSESSMENT_ID + assessmentIdFromRequest);
        // logger.info("Submit Assessment: userId: " + userId + ", data: " +
        // data.toString());
        // Check User exists
        // if (!userUtilService.validateUser(userId)) {
        // throw new BadRequestException("Invalid UserId.");
        // }
        String userId = RequestInterceptor.fetchUserIdFromAccessToken(authUserToken);
        if (userId != null) {
            Date assessmentStartTime = assessmentRepository.fetchUserAssessmentStartTime(userId, Constants.ASSESSMENT_ID + assessmentIdFromRequest);
            if (assessmentStartTime != null) {
                Timestamp submissionTime = new Timestamp(new Date().getTime());
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(new Timestamp(assessmentStartTime.getTime()).getTime());
                cal.add(Calendar.SECOND, ((Integer) assessmentHierarchy.get(Constants.EXPECTED_DURATION)).intValue() + Integer.valueOf(cbExtServerProperties.getUserAssessmentSubmissionDuration()));
                Timestamp later = new Timestamp(cal.getTime().getTime());
                int time = submissionTime.compareTo(later);
                if (time <= 0) {
                    outgoingResponse.setResponseCode(HttpStatus.OK);
                    outgoingResponse.getResult().put(Constants.IDENTIFIER, assessmentIdFromRequest);
                    outgoingResponse.getResult().put(Constants.OBJECT_TYPE, assessmentHierarchy.get(Constants.OBJECT_TYPE));
                    outgoingResponse.getResult().put(Constants.PRIMARY_CATEGORY, assessmentHierarchy.get(Constants.PRIMARY_CATEGORY));
                    List<Map<String, Object>> hierarchySectionList = (List<Map<String, Object>>) assessmentHierarchy
                            .get(Constants.CHILDREN);
                    if (CollectionUtils.isEmpty(hierarchySectionList)) {
                        logger.error(new Exception("There are no section details in Assessment hierarchy."));
                        // TODO Throw error
                    } else {
                        // Check Sections are available in the submit request or not
                        if (submitRequest.containsKey(Constants.CHILDREN)
                                && !CollectionUtils.isEmpty((List<Map<String, Object>>) submitRequest.get(Constants.CHILDREN))) {
                            List<Map<String, Object>> sectionListFromSubmitRequest = (List<Map<String, Object>>) submitRequest.get(Constants.CHILDREN);
                            List<Map<String, Object>> sectionLevelsResults = new ArrayList<>();
                            for (Map<String, Object> hierarchySection : hierarchySectionList) {
                                String scoreCutOffType = null;
                                String hierarchySectionId = (String) hierarchySection.get(Constants.IDENTIFIER);
                                String userSectionId = null;
                                Map<String, Object> userSectionData = null;
                                for (Map<String, Object> sectionFromSubmitRequest : sectionListFromSubmitRequest) {
                                    userSectionId = (String) sectionFromSubmitRequest.get(Constants.IDENTIFIER);
                                    if (userSectionId.equalsIgnoreCase(hierarchySectionId)) {
                                        scoreCutOffType = ((String) sectionFromSubmitRequest.get(Constants.SCORE_CUTOFF_TYPE)).toLowerCase();
                                        userSectionData = sectionFromSubmitRequest;
                                        break;
                                    }
                                }
                                if(userSectionData == null)
                                {
                                    Map<String, Object> sectionLevelResult = createResponseMapWithProperStructure(hierarchySection, null);
                                    sectionLevelsResults.add(sectionLevelResult);
                                    continue;
                                }

                            // We have both userSectiondata and userSection
                            // Get the list of question Identifier's from userSectionData
                            List<String> questionsFromAssessment = new ArrayList<>();
                            Map<String, Object> questionSetFromAssessment = (Map<String, Object>) redisCacheMgr.getCache(Constants.USER_ASSESS_REQ + authUserToken);
                            if (questionSetFromAssessment != null && questionSetFromAssessment.get(Constants.CHILDREN) != null) {
                                List<Map<String, Object>> sections = (List<Map<String, Object>>) questionSetFromAssessment.get(Constants.CHILDREN);
                                for(Map<String, Object> section : sections) {
                                    String sectionId = (String) section.get(Constants.IDENTIFIER);
                                    if (userSectionId.equalsIgnoreCase(sectionId)) {
                                        questionsFromAssessment.addAll((List<String>) section.get(Constants.CHILD_NODES));
                                        break;
                                    }
                                }
                            }
                            switch (scoreCutOffType) {
                                case Constants.ASSESSMENT_LEVEL_SCORE_CUTOFF: {
                                    if (hierarchySectionList.size() > 1) {
                                       throw new Exception("Hierarchy cannot have more than 1 section for assessment level cutoff");
                                    }
                                    Map<String, Object> result = validateScores(userSectionData, hierarchySection, questionsFromAssessment, authUserToken);
                                    outgoingResponse.getResult().putAll(calculateAssessmentFinalResults(result));
                                    return outgoingResponse;
                                }
                                case Constants.SECTION_LEVEL_SCORE_CUTOFF: {
                                    Map<String, Object> result = validateScores(userSectionData, hierarchySection, questionsFromAssessment, authUserToken);
                                    sectionLevelsResults.add(result);
                                }
                                break;
                                default:
                                    break;
                            }
                        }
                            if(hierarchySectionList.size()-sectionLevelsResults.size()==0) {
                                outgoingResponse.getResult().putAll(calculateSectionFinalResults(sectionLevelsResults));
                            }
                            else {

                            }

                    }}
                }
            }
            else
            {
                outgoingResponse.setResponseCode(HttpStatus.NOT_IMPLEMENTED);
            }
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
            throw new RuntimeException(e);
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
            if(pass == sectionLevelResults.size()) {
                res.put(Constants.PASS, true);
            }
            else
            {
                res.put(Constants.PASS, false);
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new RuntimeException(e);
        }
        return res;
    }

    private Map<String, Object> getReadHierarchyApiResponse(String assessmentIdentifier, String token) {
        try {
            StringBuilder sbUrl = new StringBuilder(cbExtServerProperties.getAssessmentHost());
            sbUrl.append(cbExtServerProperties.getAssessmentHierarchyReadPath());
            String serviceURL = sbUrl.toString().replace(Constants.IDENTIFIER_REPLACER, assessmentIdentifier);
            Map<String, String> headers = new HashMap<>();
            headers.put(Constants.X_AUTH_TOKEN, token);
            headers.put(Constants.AUTHORIZATION, cbExtServerProperties.getSbApiKey());
            Object o = outboundRequestHandlerService.fetchUsingGetWithHeaders(serviceURL, headers);
            return mapper.convertValue(o, Map.class);
        } catch (Exception e) {
            logger.error(e);
            throw new ApplicationLogicError(e.getMessage());
        }
    }

    private SBApiResponse prepareAssessmentResponse(Map<String, Object> hierarchyResponse, boolean isSuccess) {
        SBApiResponse outgoingResponse = new SBApiResponse();
        outgoingResponse.setId(Constants.API_QUESTIONSET_HIERARCHY_GET);
        outgoingResponse.setVer(Constants.VER);
        outgoingResponse.getParams().setResmsgid(UUID.randomUUID().toString());
        if (isSuccess) {
            outgoingResponse.getParams().setStatus(Constants.SUCCESS);
            outgoingResponse.setResponseCode(HttpStatus.OK);
            readAssessmentLevelData(hierarchyResponse, outgoingResponse);
        } else {
            outgoingResponse.getParams().setStatus(Constants.FAILED);
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
            List<String> allQuestionIdList = new ArrayList<String>();
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

    private List<String> getQuestionIdList(Map<String, Object> questionListRequest) throws Exception {
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
        throw new Exception("Failed to process the questionList request body.");
    }

    private Map<String, Object> readQuestionDetails(List<String> identifiers) throws Exception {
        try {
            StringBuilder sbUrl = new StringBuilder(cbExtServerProperties.getAssessmentHost());
            sbUrl.append(cbExtServerProperties.getAssessmentQuestionListPath());
            Map<String, String> headers = new HashMap<>();
            headers.put(Constants.AUTHORIZATION, cbExtServerProperties.getSbApiKey());
            Map<String, Object> requestBody = new HashMap<String, Object>();
            Map<String, Object> requestData = new HashMap<String, Object>();
            Map<String, Object> searchData = new HashMap<String, Object>();
            searchData.put(Constants.IDENTIFIER, identifiers);
            requestData.put(Constants.SEARCH, searchData);
            requestBody.put(Constants.REQUEST, requestData);
            return outboundRequestHandlerService.fetchResultUsingPost(sbUrl.toString(), requestBody, headers);
        } catch (Exception e) {
            logger.error(e);
            throw new Exception("Failed to process the readQuestionDetails.");
        }
    }

    private Map<String, Object> filterQuestionMapDetail(Map<String, Object> questionMapResponse) {
        List<String> questionParams = cbExtServerProperties.getAssessmentQuestionParams();
        Map<String, Object> updatedQuestionMap = new HashMap<String, Object>();
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

    private SBApiResponse prepareQuestionResponse(List<Object> updatedQuestions, boolean isSuccess) {
        SBApiResponse outgoingResponse = new SBApiResponse();
        outgoingResponse.setId(Constants.API_QUESTIONS_LIST);
        outgoingResponse.setVer(Constants.VER);
        if (isSuccess) {
            outgoingResponse.setResponseCode(HttpStatus.OK);
            outgoingResponse.getResult().put(Constants.QUESTIONS, updatedQuestions);
        } else {
            outgoingResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return outgoingResponse;
    }

    private Map<String, Object> validateScores(Map<String, Object> userSectionData,
                                               Map<String, Object> hierarchySection, List<String> questionsFromAssessment, String authUserToken) {
            // We have both answer and user given data. This needs to be compared and result
            // should be return.
        Map<String, Object> resultMap = assessUtilServ.validateQumlAssessment(questionsFromAssessment,
                (List<Map<String, Object>>) userSectionData.get(Constants.CHILDREN));
        return createResponseMapWithProperStructure(hierarchySection, resultMap);
        }
    @Override
    public Map<String, Object> createResponseMapWithProperStructure(Map<String, Object> hierarchySection, Map<String, Object> resultMap) {
        Map<String, Object> sectionLevelResult = new HashMap<String, Object>();
        sectionLevelResult.put(Constants.IDENTIFIER, hierarchySection.get(Constants.IDENTIFIER));
        sectionLevelResult.put(Constants.OBJECT_TYPE, hierarchySection.get(Constants.OBJECT_TYPE));
        sectionLevelResult.put(Constants.PRIMARY_CATEGORY, hierarchySection.get(Constants.PRIMARY_CATEGORY));
        sectionLevelResult.put(Constants.SCORE_CUTOFF_TYPE, hierarchySection.get(Constants.SCORE_CUTOFF_TYPE));
        sectionLevelResult.put(Constants.PASS_PERCENTAGE, (Integer) hierarchySection.get(Constants.MINIMUM_PASS_PERCENTAGE));
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
}

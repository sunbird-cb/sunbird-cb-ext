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
import org.sunbird.common.model.SunbirdApiResp;
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
                if (assessmentAllDetail.get(Constants.DURATION) != null) {
                    boolean resp = assessmentRepository.addUserAssesmentStartTime(userId, Constants.ASSESSMENT_ID + assessmentIdentifier, new Timestamp(new Date().getTime()));
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
            List<Object> map = redisCacheMgr.mget(identifierList);
            int size = map.size();
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
    public SBApiResponse submitAssessment(Map<String, Object> data, String authUserToken) throws Exception {
        SBApiResponse outgoingResponse = new SBApiResponse();
        String assessmentId = (String) data.get(Constants.IDENTIFIER);
        Map<String, Object> assessmentHierarchy = (Map<String, Object>) redisCacheMgr
                .getCache(Constants.ASSESSMENT_ID + assessmentId);
        // logger.info("Submit Assessment: userId: " + userId + ", data: " +
        // data.toString());
        // Check User exists
        // if (!userUtilService.validateUser(userId)) {
        // throw new BadRequestException("Invalid UserId.");
        // }
        String userId = RequestInterceptor.fetchUserIdFromAccessToken(authUserToken);
        if (userId != null) {
            Date assessmentStartTime = assessmentRepository.fetchUserAssessmentStartTime(userId, Constants.ASSESSMENT_ID + assessmentId);
            if (assessmentStartTime != null) {
                Timestamp submissionTime = new Timestamp(new Date().getTime());
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(new Timestamp(assessmentStartTime.getTime()).getTime());
                cal.add(Calendar.SECOND, Integer.valueOf((String) assessmentHierarchy.get(Constants.DURATION)).intValue() + Integer.valueOf(cbExtServerProperties.getUserAssessmentSubmissionDuration()));
                Timestamp later = new Timestamp(cal.getTime().getTime());
                int time = submissionTime.compareTo(later);
                if (time <= 0) {
                    outgoingResponse.setResponseCode(HttpStatus.OK);
                    outgoingResponse.getResult().put(Constants.IDENTIFIER, assessmentId);
                    outgoingResponse.getResult().put(Constants.OBJECT_TYPE, assessmentHierarchy.get(Constants.OBJECT_TYPE));
                    outgoingResponse.getResult().put(Constants.PRIMARY_CATEGORY, assessmentHierarchy.get(Constants.PRIMARY_CATEGORY));

                    // Check Sections are available
                    if (data.containsKey(Constants.CHILDREN)
                            && !CollectionUtils.isEmpty((List<Map<String, Object>>) data.get(Constants.CHILDREN))) {
                        List<Map<String, Object>> sectionList = (List<Map<String, Object>>) data.get(Constants.CHILDREN);

                        for (Map<String, Object> section : sectionList) {
                            String id = (String) section.get(Constants.IDENTIFIER);
                            String scoreCutOffType = ((String) section.get(Constants.SCORE_CUTOFF_TYPE)).toLowerCase();
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
                }
            }
        }
        return outgoingResponse;
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
        List<Map<String, Object>> userQuestionList = (List<Map<String, Object>>) hierarchySection.get(Constants.CHILDREN);
        for (Map<String, Object> question : userQuestionList) {
            questionIdList.add((String) question.get(Constants.IDENTIFIER));
        }

        // We have both answer and user given data. This needs to be compared and result
        // should be return.
        Map<String, Object> resultMap = assessUtilServ.validateQumlAssessment(questionIdList,
                (List<Map<String, Object>>) userSectionData.get(Constants.CHILDREN));

        Double result = (Double) resultMap.get(Constants.RESULT);
        int passPercentage = (Integer) hierarchySection.get(Constants.MINIMUM_PASS_PERCENTAGE);
        Map<String, Object> sectionLevelResult = new HashMap<String, Object>();
        sectionLevelResult.put(Constants.IDENTIFIER, hierarchySection.get(Constants.IDENTIFIER));
        sectionLevelResult.put(Constants.OBJECT_TYPE, hierarchySection.get(Constants.OBJECT_TYPE));
        sectionLevelResult.put(Constants.PRIMARY_CATEGORY, hierarchySection.get(Constants.PRIMARY_CATEGORY));
        sectionLevelResult.put(Constants.SCORE_CUTOFF_TYPE, hierarchySection.get(Constants.SCORE_CUTOFF_TYPE));
        sectionLevelResult.put(Constants.PASS_PERCENTAGE, passPercentage);
        sectionLevelResult.put(Constants.RESULT, result);
        sectionLevelResult.put(Constants.TOTAL, resultMap.get(Constants.TOTAL));
        sectionLevelResult.put(Constants.BLANK, resultMap.get(Constants.BLANK));
        sectionLevelResult.put(Constants.CORRECT, resultMap.get(Constants.CORRECT));
        sectionLevelResult.put(Constants.PASS_PERCENTAGE, hierarchySection.get(Constants.MINIMUM_PASS_PERCENTAGE));
        sectionLevelResult.put(Constants.INCORRECT, resultMap.get(Constants.INCORRECT));
        sectionLevelResult.put(Constants.PASS, result >= passPercentage);

        List<Map<String, Object>> sectionChildren = new ArrayList<Map<String, Object>>();
        sectionChildren.add(sectionLevelResult);
        outgoingResponse.getResult().put(Constants.CHILDREN, sectionChildren);

        outgoingResponse.getResult().put(Constants.OVERALL_RESULT, result);
        outgoingResponse.getResult().put(Constants.TOTAL, resultMap.get(Constants.TOTAL));
        outgoingResponse.getResult().put(Constants.BLANK, resultMap.get(Constants.BLANK));
        outgoingResponse.getResult().put(Constants.CORRECT, resultMap.get(Constants.CORRECT));
        outgoingResponse.getResult().put(Constants.PASS_PERCENTAGE, hierarchySection.get(Constants.MINIMUM_PASS_PERCENTAGE));
        outgoingResponse.getResult().put(Constants.INCORRECT, resultMap.get(Constants.INCORRECT));
        outgoingResponse.getResult().put(Constants.PASS, result >= passPercentage);
    }

}

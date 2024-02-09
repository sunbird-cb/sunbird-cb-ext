package org.sunbird.progress.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.exception.InvalidDataInputException;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.core.producer.Producer;
import org.sunbird.progress.model.ContentProgressInfo;
import org.sunbird.user.service.UserUtilityService;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class ContentProgressServiceImpl implements ContentProgressService {

    @Autowired
    private CbExtServerProperties cbExtServerProperties;
    @Autowired
    Producer kafkaProducer;

    private CbExtLogger logger = new CbExtLogger(getClass().getName());

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private CassandraOperation cassandraOperation;

    @Autowired
    private UserUtilityService userUtilityService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Marking the attendance for offline sessions
     *
     * @param authUserToken- It's authorization token received in request header.
     * @param requestBody    -Request body of the API which needs to be processed.
     * @return- Return the response of success/failure after processing the request.
     */
    @Override
    public SBApiResponse updateContentProgress(String authUserToken, SunbirdApiRequest requestBody) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_UPDATE_CONTENT_PROGRESS);
        Map<String, Object> contentProgressAttributes;
        Map<String, String> headersValues = new HashMap<>();
        headersValues.put("X-Authenticated-User-Token", authUserToken);
        headersValues.put("Authorization", cbExtServerProperties.getSbApiKey());
        try {
            contentProgressAttributes = new HashMap<>();
            contentProgressAttributes.put("requestBody", requestBody);
            contentProgressAttributes.put("headersValues", headersValues);
            kafkaProducer.push(cbExtServerProperties.getUpdateContentProgressKafkaTopic(), contentProgressAttributes);
            response.getParams().setStatus(Constants.SUCCESSFUL);
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception ex) {
            logger.error(ex);
            response.getParams().setErrmsg(String.format(Constants.UPDATE_CONTENT_PROGRESS_ERROR_MSG, ex.getMessage()));
            response.getParams().setStatus(Constants.FAILED);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }


    /**
     * This method is responsible for fetching the user content progress and user details.
     *
     * @param requestBody   -Request body of the API which needs to be processed.
     * @param authUserToken - It's authorization token received in request header.
     * @return - Return the response of success/failure after processing the request.
     */
    @Override
    public SBApiResponse getUserSessionDetailsAndCourseProgress(String authUserToken, SunbirdApiRequest requestBody) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.USER_SEARCH_CONTENT_RESULT_LIST);
        try {
            List<String> usersList = new ArrayList<>();
            ContentProgressInfo contentProgressInfo = new ContentProgressInfo();
            if (!ObjectUtils.isEmpty(requestBody.getRequest())) {
                contentProgressInfo = mapper.convertValue(requestBody.getRequest(), ContentProgressInfo.class);
                validateContentProgressInfo(contentProgressInfo);
            }
            if (contentProgressInfo.getUserId() != null && contentProgressInfo.getUserId().size() > 0) {
                usersList.addAll(contentProgressInfo.getUserId());
            } else {
                List<Map<String, Object>> enrollmentBatchLookupList = getEnrollmentBatchLookupDetails(contentProgressInfo);
                enrollmentBatchLookupList.forEach(userEnrollmentBatchDetail -> userEnrollmentBatchDetail.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(Constants.USER_ID)).forEach(entry -> {
                    if (entry.getKey().equalsIgnoreCase(Constants.USER_ID)) {
                        usersList.add(entry.getValue().toString());
                    }
                }));
            }
            logger.info(" Troubleshoot start of the getUserContentConsumptionDetails : 1");
            List<Map<String, Object>> userContentConsumptionList = getUserContentConsumptionDetails(contentProgressInfo, usersList);
            logger.info(" Troubleshoot End of the getUserContentConsumptionDetails : 1");
            Map<String, Map<String, Object>> userDetailsList = userUtilityService.getUserDetailsFromES(usersList, Arrays.asList(Constants.USER_FIRST_NAME, Constants.PROFILE_DETAILS_DESIGNATION, Constants.PROFILE_DETAILS_PRIMARY_EMAIL, Constants.CHANNEL, Constants.USER_ID, Constants.EMPLOYMENT_DETAILS_DEPARTMENT_NAME, Constants.PROFILE_DETAILS_PHONE, Constants.ROOT_ORG_ID));
            userContentConsumptionList.forEach(contentMap -> {
                String userId = (String) contentMap.get(Constants.USER_ID);
                String contentId = (String) contentMap.get("contentid");
                contentMap.remove(Constants.USER_ID);
                Map<String, Object> userMap = userDetailsList.get(userId);
                if (userMap.containsKey(Constants.PROGRESS_DETAILS)) {
                    Map<String, Map<String, Object>> progressDetailsMap = (Map<String, Map<String, Object>>) userMap.get(Constants.PROGRESS_DETAILS);
                    progressDetailsMap.compute(contentId, (key, existingValue) -> {
                        return contentMap;
                    });
                } else {
                    Map<String, Map<String, Object>> progressDetailsMap = new HashMap<String, Map<String, Object>>();
                    Map<String, Object> progressMap = new HashMap<String, Object>();
                    progressMap.put("contentId", contentId);
                    progressMap.put("status", contentMap.get("status"));
                    progressDetailsMap.put(contentId, progressMap);
                    userMap.put(Constants.PROGRESS_DETAILS, progressDetailsMap);

                }
            });
            userDetailsList.forEach((userId, userInformation) -> {
                if (userInformation.containsKey(Constants.PROGRESS_DETAILS)) {
                    Map<String, Object> progressDetails = (Map<String, Object>) userInformation.get(Constants.PROGRESS_DETAILS);
                    userInformation.put(Constants.PROGRESS_DETAILS, new ArrayList<>(progressDetails.values()));
                } else {
                    userInformation.put(Constants.PROGRESS_DETAILS, new ArrayList<>());
                }
            });
            response.getResult().put(Constants.COUNT, userDetailsList.size());
            response.getResult().put(Constants.RESPONSE, userDetailsList.values());
        } catch (InvalidDataInputException exception) {
            logger.error("Exception Occurred ",exception);
            response.getParams().setErrmsg(exception.getMessage());
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            response.getParams().setStatus(Constants.FAILED);
        } catch (Exception e) {
            logger.error("Exception Occurred ",e);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.getParams().setStatus(Constants.FAILED);
        }
        return response;
    }

    /**
     * This method returns the user_content_consumption details for the users.
     *
     * @param contentProgressInfo - Model class for the contentProgress.
     * @param usersList           - List of users to get the content consumption details.
     * @return -return a list of the user_content_consumption details based on the userIds passed.
     */
    private List<Map<String, Object>> getUserContentConsumptionDetails(ContentProgressInfo contentProgressInfo, List<String> usersList) {
        Map<String, Object> propertyMap;
        propertyMap = new HashMap<>();
        propertyMap.put(Constants.BATCH_ID, contentProgressInfo.getBatchId());
        propertyMap.put(Constants.COURSE_ID, contentProgressInfo.getCourseId());
        propertyMap.put(Constants.USER_ID, usersList);
        if (contentProgressInfo.getContentId() != null && contentProgressInfo.getContentId().size() > 0) {
            propertyMap.put(Constants.CONTENT_ID_KEY, contentProgressInfo.getContentId());
        }
        return cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD_COURSES,
                Constants.USER_CONTENT_CONSUMPTION, propertyMap, Arrays.asList(Constants.CONTENT_ID_KEY, Constants.USER_ID, Constants.STATUS));

    }


    /**
     * This method returns the enrollment_batch_lookup details for the users.
     *
     * @param contentProgressInfo - Model class for the contentProgress.
     * @return - return a list of the enrollment_batch_lookup details based on the batchId passed.
     */
    private List<Map<String, Object>> getEnrollmentBatchLookupDetails(ContentProgressInfo contentProgressInfo) {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(Constants.BATCH_ID, contentProgressInfo.getBatchId());
        List<Map<String, Object>>  list = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD_COURSES,
                Constants.TABLE_ENROLMENT_BATCH_LOOKUP, propertyMap, Arrays.asList(Constants.BATCH_ID, Constants.USER_ID,Constants.ACTIVE));
        // Stream to filter and collect only non-null "endDate" maps
        return list.stream()
                .filter(item -> {
                    return item != null && item.containsKey("active") && (boolean) item.get("active") == true;
                })
                .collect(Collectors.toList());
    }

    private void validateContentProgressInfo(ContentProgressInfo contentProgressInfo) {
        if (StringUtils.isEmpty(contentProgressInfo.getBatchId())) {
            throw new InvalidDataInputException(Constants.CONTENT_PROGRESS_BATCH_ID_ERROR_MSG);
        }

        if (StringUtils.isEmpty(contentProgressInfo.getCourseId())) {
            throw new InvalidDataInputException(Constants.CONTENT_PROGRESS_COURSE_ID_ERROR_MSG);
        }

        if (contentProgressInfo.getContentId().isEmpty()) {
            throw new InvalidDataInputException(Constants.CONTENT_PROGRESS_CONTENT_ID_ERROR_MSG);
        }
    }
}

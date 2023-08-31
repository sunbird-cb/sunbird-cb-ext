package org.sunbird.progress.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.IndexerService;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.cipher.DecryptServiceImpl;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.core.producer.Producer;
import org.sunbird.progress.model.ContentProgressInfo;

import java.io.IOException;
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
    CbExtServerProperties serverConfig;

    @Autowired
    IndexerService indexerService;

    @Autowired
    DecryptServiceImpl decryptService;

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
    public String getUserSessionDetailsAndCourseProgress(String authUserToken, SunbirdApiRequest requestBody) throws IOException {
        List<String> usersList = new ArrayList<>();
        ContentProgressInfo contentProgressInfo = new ContentProgressInfo();
        if (!ObjectUtils.isEmpty(requestBody.getRequest())) {
            contentProgressInfo = mapper.convertValue(requestBody.getRequest(), ContentProgressInfo.class);
        }
        List<Map<String, Object>> enrollmentBatchLookupList = getEnrollmentBatchLookupDetails(contentProgressInfo);
        enrollmentBatchLookupList.forEach(userEnrollmentBatchDetail ->
                userEnrollmentBatchDetail.entrySet().stream()
                        .filter(entry -> entry.getKey().equalsIgnoreCase(Constants.USER_ID))
                        .forEach(entry -> {
                            if (entry.getKey().equalsIgnoreCase(Constants.USER_ID)) {
                                usersList.add(entry.getValue().toString());
                            }
                        })
        );

        List<Map<String, Object>> userContentConsumptionList = getUserContentConsumptionDetails(contentProgressInfo, usersList);
        Map<String, Map<String, String>> userDetailsList = getUserDetails();
        userDetailsList.entrySet().stream()
                .flatMap(a -> a.getValue().entrySet().stream())
                .forEach(b -> logger.info(b.getKey() + "-" + b.getValue()));
        userContentConsumptionList.forEach(userContentMap -> userContentMap.forEach((key, value) -> logger.info(key + "--" + value)));
        return null;
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
        propertyMap.put(Constants.BATCH_ID, contentProgressInfo.getApplicationId());
        propertyMap.put(Constants.COURSE_ID, contentProgressInfo.getCourseId());
        propertyMap.put(Constants.USER_ID, usersList);
        return cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD_COURSES,
                Constants.USER_CONTENT_CONSUMPTION, propertyMap, Arrays.asList(Constants.COURSE_ID, Constants.CONTENT_ID_KEY, Constants.BATCH_ID, Constants.USER_ID, Constants.PROGRESS));

    }


    /**
     * This method returns the enrollment_batch_lookup details for the users.
     *
     * @param contentProgressInfo - Model class for the contentProgress.
     * @return - return a list of the enrollment_batch_lookup details based on the batchId passed.
     */
    private List<Map<String, Object>> getEnrollmentBatchLookupDetails(ContentProgressInfo contentProgressInfo) {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(Constants.BATCH_ID, contentProgressInfo.getApplicationId());
        return cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD_COURSES,
                Constants.TABLE_ENROLMENT_BATCH_LOOKUP, propertyMap, Arrays.asList(Constants.BATCH_ID, Constants.USER_ID, Constants.ACTIVE));
    }

    /**
     * @return - The user details after processing the elastic search query.
     * @throws IOException - will throw an IO Exception if any occurs.
     */
    public Map<String, Map<String, String>> getUserDetails() throws IOException {
        int index = 0;
        int size = 500;
        long userCount = 0L;
        boolean isCompleted = false;
        SearchSourceBuilder sourceBuilder = null;
        List<Map<String, Object>> resultArray = new ArrayList<>();
        Map<String, Map<String, String>> backupUserInfoMap = new HashMap<>();
        Map<String, Object> result;
        Map<String, Map<String, String>> userInfoMap = new HashMap<>();
        final BoolQueryBuilder finalQuery = QueryBuilders.boolQuery();
        finalQuery.must(QueryBuilders.termQuery(Constants.STATUS, 1));
        do {
            sourceBuilder = new SearchSourceBuilder().query(finalQuery).from(index).size(size);
            sourceBuilder.fetchSource(serverConfig.getEsUserReportIncludeFields(), new String[]{});
            SearchResponse searchResponse = indexerService.getEsResult(serverConfig.getSbEsUserProfileIndex(),
                    serverConfig.getSbEsProfileIndexType(), sourceBuilder, true);
            if (index == 0) {
                userCount = searchResponse.getHits().getTotalHits();
                logger.info(String.format("Number of users in ES index : %s", userCount));
            }
            for (SearchHit hit : searchResponse.getHits()) {
                result = hit.getSourceAsMap();
                resultArray.add(result);
            }
            processUserDetails(resultArray, userInfoMap);
            backupUserInfoMap.putAll(userInfoMap);
            resultArray.clear();

            index = (int) Math.min(userCount, index + size);
            if (index == userCount) {
                isCompleted = true;
            }
        } while (!isCompleted);
        return backupUserInfoMap;
    }

    /**
     * This method is responsible for processing and setting the data in the collection object.
     *
     * @param userMapList - Contains the data fetched from the elastic search.
     * @param userInfoMap - collection object where the data is set after the processing.
     */
    private void processUserDetails(List<Map<String, Object>> userMapList,
                                    Map<String, Map<String, String>> userInfoMap) {
        for (Map<String, Object> user : userMapList) {
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put(Constants.USER_ID, (String) user.get(Constants.USER_ID));
            userInfo.put(Constants.FIRSTNAME, (String) user.get(Constants.FIRSTNAME));
            userInfo.put(Constants.ROOT_ORG_ID, (String) user.get(Constants.ROOT_ORG_ID));
            userInfo.put(Constants.CHANNEL, (String) user.get(Constants.CHANNEL));
            if (StringUtils.isNotBlank((String) user.get(Constants.EMAIL))) {
                String value = decryptService.decryptString((String) user.get(Constants.EMAIL));
                userInfo.put(Constants.EMAIL, value);
            }
            if (StringUtils.isNotBlank((String) user.get(Constants.PHONE))) {
                userInfo.put(Constants.PHONE, decryptService.decryptString((String) user.get(Constants.PHONE)));
            }
            String strRoles = "";
            List<Map<String, Object>> roles = (List<Map<String, Object>>) user.get(Constants.ROLES);
            for (Map<String, Object> role : roles) {
                String strRole = (String) role.get(Constants.ROLE);
                if (StringUtils.isNotBlank(strRoles)) {
                    strRoles = strRoles.concat(", ").concat(strRole);
                } else {
                    strRoles = StringUtils.isBlank(strRole) ? "" : strRole;
                }
            }
            userInfo.put(Constants.ROLES, strRoles);
            userInfoMap.put(userInfo.get(Constants.USER_ID), userInfo);
        }
    }
}

package org.sunbird.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.*;
import org.sunbird.common.service.ElasticSearchService;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.service.impl.ElasticSearchRestHighImpl;
import org.sunbird.common.util.*;
import org.sunbird.core.exception.ApplicationLogicError;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.telemetry.model.LastLoginInfo;
import org.sunbird.user.util.TelemetryUtils;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import static org.sunbird.common.util.PropertiesCache.getInstance;

/**
 * @author akhilesh.kumar05
 */
@Service
public class UserUtilityServiceImpl implements UserUtilityService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CbExtServerProperties props;

    @Autowired
    TelemetryUtils telemetryUtils;

    @Autowired
    RestHighLevelClient esClient;

    @Autowired
    private CassandraOperation cassandraOperation;

    @Autowired
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    @Autowired
    CbExtServerProperties serverConfig;
    private final ElasticSearchService esService = EsClientFactory.getInstance(Constants.REST);
    private CbExtLogger logger = new CbExtLogger(getClass().getName());
    private final String SEARCH_RESULT = "Search API response: ";

    public boolean validateUser(String userId) {
        return validateUser(StringUtils.EMPTY, userId);
    }

    @Override
    public boolean validateUser(String rootOrg, String userId) {

        Map<String, Object> requestMap = new HashMap<>();

        Map<String, Object> request = new HashMap<>();

        Map<String, String> filters = new HashMap<>();
        filters.put(Constants.USER_ID, userId);
        request.put(Constants.FILTERS, filters);

        requestMap.put("request", request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            String reqBodyData = new ObjectMapper().writeValueAsString(requestMap);

            HttpEntity<String> requestEnty = new HttpEntity<>(reqBodyData, headers);

            String serverUrl = props.getSbUrl() + "private/user/v1/search";

            SunbirdApiResp sunbirdApiResp = restTemplate.postForObject(serverUrl, requestEnty, SunbirdApiResp.class);

            boolean expression = (sunbirdApiResp != null && "OK".equalsIgnoreCase(sunbirdApiResp.getResponseCode())
                    && sunbirdApiResp.getResult().getResponse().getCount() >= 1);
            return expression;

        } catch (Exception e) {
            throw new ApplicationLogicError("Sunbird Service ERROR: ", e);
        }

    }

    @Override
    public Map<String, Object> getUsersDataFromUserIds(String rootOrg, List<String> userIds, List<String> source) {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> filters = new HashMap<>();
        filters.put("userId", userIds);
        request.put("filters", filters);
        requestBody.put("request", request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            HttpEntity<?> requestEnty = new HttpEntity<>(requestBody, headers);
            String url = props.getSbUrl() + props.getUserSearchEndPoint();
            SearchUserApiResp searchUserResult = restTemplate.postForObject(url, requestEnty, SearchUserApiResp.class);
            logger.info("searchUserResult ---->" + searchUserResult.toString());
            if (searchUserResult != null && "OK".equalsIgnoreCase(searchUserResult.getResponseCode())
                    && searchUserResult.getResult().getResponse().getCount() > 0) {
                for (SearchUserApiContent searchUserApiContent : searchUserResult.getResult().getResponse()
                        .getContent()) {
                    result.put(searchUserApiContent.getUserId(), searchUserApiContent);
                }
            }
        } catch (Exception e) {
            throw new ApplicationLogicError("Sunbird Service ERROR: ", e);
        }

        return result;
    }

    @Override
    public Map<String, Object> getUsersDataFromUserIds(List<String> userIds, List<String> fields, String authToken) {
        Map<String, Object> result = new HashMap<>();

        // headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.USER_TOKEN, authToken);
        headers.add(Constants.AUTHORIZATION, props.getSbApiKey());
        // request body
        SunbirdApiRequest requestObj = new SunbirdApiRequest();
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put(Constants.FILTERS, new HashMap<String, Object>() {
            {
                put(Constants.USER_ID, userIds);
            }
        });
        reqMap.put(Constants.FIELDS_CONSTANT, fields);
        requestObj.setRequest(reqMap);

        try {
            String url = props.getSbUrl() + props.getUserSearchEndPoint();
            HttpEntity<?> requestEnty = new HttpEntity<>(requestObj, headers);
            SearchUserApiResp searchUserResult = restTemplate.postForObject(url, requestEnty, SearchUserApiResp.class);
            logger.debug(SEARCH_RESULT + searchUserResult.toString());
            if (searchUserResult != null && Constants.OK.equalsIgnoreCase(searchUserResult.getResponseCode())
                    && searchUserResult.getResult().getResponse().getCount() > 0) {
                for (SearchUserApiContent searchUserApiContent : searchUserResult.getResult().getResponse()
                        .getContent()) {
                    result.put(searchUserApiContent.getUserId(), searchUserApiContent);
                }
            }
        } catch (Exception e) {
            throw new ApplicationLogicError("Sunbird Service ERROR: ", e);
        }

        return result;
    }

    @Override
    public Map<String, Object> updateLogin(LastLoginInfo userLoginInfo) {
        Map<String, Object> response = new HashMap<>();
        logger.info(String.format("Updating User Login: rootOrg: %s, userId: %s", userLoginInfo.getOrgId(),
                userLoginInfo.getUserId()));
        userLoginInfo.setLoginTime(new Timestamp(new Date().getTime()));
        try {
            Map<String, Object> propertyMap = new HashMap<>();
            propertyMap.put(Constants.USER_ID, userLoginInfo.getUserId());
            List<Map<String, Object>> details = cassandraOperation.getRecordsByProperties(Constants.DATABASE,
                    Constants.LOGIN_TABLE, propertyMap, null);
            if (CollectionUtils.isEmpty(details)) {
                Map<String, Object> request = new HashMap<>();
                request.put(Constants.USER_ID, userLoginInfo.getUserId());
                request.put(Constants.FIRST_LOGIN_TIME, userLoginInfo.getLoginTime());
                cassandraOperation.insertRecord(Constants.DATABASE, Constants.LOGIN_TABLE, request);
                telemetryUtils.pushDataToKafka(telemetryUtils.getUserslastLoginTelemetryEventData(userLoginInfo),
                        serverConfig.getUserUtilityTopic());
            }
        } catch (Exception e) {
            throw new ApplicationLogicError("Sunbird Service ERROR: ", e);
        }
        response.put(userLoginInfo.getUserId(), Boolean.TRUE);
        return response;
    }

    @Override
    public Map<String, Object> getUsersReadData(String userId, String authToken, String X_authToken) {
        Map<String, String> header = new HashMap<>();
        header.put(Constants.AUTH_TOKEN, authToken);
        header.put(Constants.X_AUTH_TOKEN, X_authToken);
        Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService
                .fetchUsingGetWithHeadersProfile(serverConfig.getSbUrl() + serverConfig.getLmsUserReadPath() + userId,
                        header);
        Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
        Map<String, Object> responseMap = (Map<String, Object>) result.get(Constants.RESPONSE);
        return responseMap;
    }

    @Override
    public Map<String, Object> autoSearchUser(String key) throws IOException {
        Map<String, Object> searchQueryMap = new HashMap<>();
        String field = getInstance().getProperty("user.autocomplete.es.keys");
        //prepare searchQuerymap
        final String[] includeFields = field.split(",");
        Map<String, List<String>> querySearchFields = new HashMap();
        querySearchFields.put(key, Arrays.asList(includeFields));
        searchQueryMap.put(Constants.MULTI_QUERY_SEARCH_FIELDS, querySearchFields);
        SearchDTO searchDto = ElasticSearchHelper.createSearchDTO(searchQueryMap);
        searchDto.setExcludedFields(Arrays.asList(ProjectUtil.excludes));
        Map<String, Object> result = esService.search(searchDto, ProjectUtil.EsType.user.getTypeName(), esClient);
        List<Map<String, Object>> userMapList =
                (List<Map<String, Object>>) result.get(Constants.CONTENT);
        List<String> fields = (List<String>) searchQueryMap.get(Constants.FIELDS);
        Map<String, Object> userDefaultFieldValue = ProjectUtil.getUserDefaultValue();
        getDefaultValues(userDefaultFieldValue, fields);
        for (Map<String, Object> userMap : userMapList) {
            UserUtility.decryptUserDataFrmES(userMap);
        }
        Response response = new Response();
        response.put(Constants.RESPONSE, result);
        return result;
    }

    private void getDefaultValues(Map<String, Object> orgDefaultFieldValue, List<String> fields) {
        if (!CollectionUtils.isEmpty(fields)) {
            Iterator<Map.Entry<String, Object>> iterator = orgDefaultFieldValue.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                if (!fields.contains(entry.getKey())) {
                    iterator.remove();
                }
            }
        }
    }
}

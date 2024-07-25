package org.sunbird.ehrms.service;

/**
 * @author Deepak kumar Thakur
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.AccessTokenValidator;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;

import java.io.IOException;
import java.util.*;

@Service
public class EhrmsProfileDetailImpl implements EhrmsService {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private AccessTokenValidator accessTokenValidator;

    @Autowired
    private CbExtServerProperties serverConfig;

    @Autowired
    private RedisCacheMgr redisCacheMgr;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CassandraOperation cassandraOperation;

    public SBApiResponse fetchEhrmsProfileDetail(String userId, String authToken) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.EHRMS);
        try {
            Map<String, Object> propertyMap = new HashMap<>();
            propertyMap.put(Constants.ID, userId);
            Map<String, Object> result = cassandraOperation.getRecordsByProperties(
                    Constants.KEYSPACE_SUNBIRD,
                    Constants.USER,
                    propertyMap,
                    Arrays.asList(Constants.PROFILE_DETAILS_LOWER, Constants.USER_ID_LOWER),
                    Constants.USER_ID
            );
            String externalSystemId = null;
            if (result != null && !result.isEmpty()) {
                String userProfileDetails = (String) ((Map<String, Object>) result.get(userId)).get(Constants.PROFILE_DETAILS_LOWER);
                logger.info("User Profile Details : {}" , userProfileDetails);
                Map<String, Object> userDetails = mapper.readValue(userProfileDetails, new TypeReference<Map<String, Object>>() {
                });
                logger.info("User Details : {}" , userDetails);
                Map<String, Object> userAdditionalProperties = ((Map<String, Object>) userDetails.get(Constants.ADDITIONAL_PROPERTIES));
                if(userAdditionalProperties == null || userAdditionalProperties.isEmpty() )
                {
                    response.getParams().setStatus(Constants.FAILED);
                    response.getParams().setErrmsg("User does not have externalSystemId in profile details");
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                    return response;
                }
                externalSystemId = (String) userAdditionalProperties.get(Constants.EXTERNAL_SYSTEM_ID);
                logger.info("externalSystemId : {}" , externalSystemId);
                if (externalSystemId == null || externalSystemId.isEmpty()) {
                    response.getParams().setStatus(Constants.FAILED);
                    response.getParams().setErrmsg("User does not have externalSystemId in profile details");
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                    return response;
                }
                String ehrmsAuthUrl = serverConfig.getEhrmsAuthUrl();
                String ehrmsAuthUsername = serverConfig.getEhrmsAuthUserName();
                String ehrmsAuthPassword = serverConfig.getEhrmsAuthPassword();
                String jwtToken = redisCacheMgr.getCache(Constants.EHRMS_USER_TOKEN);
                if (StringUtils.isEmpty(jwtToken)) {
                    jwtToken = fetchJwtToken(ehrmsAuthUrl, ehrmsAuthUsername, ehrmsAuthPassword).replace("\"", "");
                    redisCacheMgr.putCache(Constants.EHRMS_USER_TOKEN, jwtToken, serverConfig.getRedisEhrmsTokenTimeOut());
                }
                Map<String, Object> fetchEhrmsUserDetails = fetchEhrmsUserDetails(serverConfig.getEhrmsDetailUrl(), externalSystemId, jwtToken);
                response.setResult(fetchEhrmsUserDetails);
                return response;
            }
        } catch (HttpClientErrorException errorException) {
            String responseBodyString = errorException.getResponseBodyAsString();
            response.getParams().setStatus(Constants.FAILED);
            response.setResponseCode(errorException.getStatusCode());
            response.setResult(prepareErrorResponse(responseBodyString));
        } catch (Exception e) {
            logger.error("Failed to look up user details. Exception: {}", e.getMessage(), e);
            response.getParams().setStatus(Constants.FAILED);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private String fetchJwtToken(String ehrmsAuthUrl, String ehrmsAuthUsername, String ehrmsAuthPassword) {
        Map<String, Object> body = new HashMap<>();
        body.put(Constants.EHRMS_AUTH_USERNAME, ehrmsAuthUsername);
        body.put(Constants.EHRMS_AUTH_PASSWORD, ehrmsAuthPassword);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(ehrmsAuthUrl, HttpMethod.POST, entity, String.class);
        return response.getBody();
    }

    private Map<String, Object> fetchEhrmsUserDetails(String ehrmsAuthUrl, String externalSystemId, String jwtToken) {
        jwtToken = jwtToken.replace("\"", "");
        Map<String, Object> body = new HashMap<>();
        body.put(Constants.EMP_CODE, externalSystemId);
        HttpHeaders headers = new HttpHeaders();
        headers.put(Constants.AUTH_TOKEN, Collections.singletonList(jwtToken));
        HttpEntity entity = new HttpEntity(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(ehrmsAuthUrl, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }

    private Map<String, Object> prepareErrorResponse(String responseBodyString) {
        Map<String, Object> errResponseBody = null;
        try {
            errResponseBody = mapper.readValue(responseBodyString, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return errResponseBody;
    }
}

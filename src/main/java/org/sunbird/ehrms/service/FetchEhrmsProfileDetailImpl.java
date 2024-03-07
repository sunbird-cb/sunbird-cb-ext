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
import org.springframework.web.client.RestTemplate;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.AccessTokenValidator;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;

import java.util.*;

@Service
public class FetchEhrmsProfileDetailImpl implements EhrmsService {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final ObjectMapper mapper = new ObjectMapper();

    private  final AccessTokenValidator accessTokenValidator;

    private final CbExtServerProperties serverConfig;

    private final RedisCacheMgr redisCacheMgr;

    private final RestTemplate restTemplate;

    private final  CassandraOperation cassandraOperation;

    @Autowired
    public FetchEhrmsProfileDetailImpl(AccessTokenValidator accessTokenValidator, CbExtServerProperties serverConfig, RedisCacheMgr redisCacheMgr, RestTemplate restTemplate, CassandraOperation cassandraOperation) {
        this.accessTokenValidator = accessTokenValidator;
        this.serverConfig = serverConfig;
        this.redisCacheMgr = redisCacheMgr;
        this.restTemplate = restTemplate;
        this.cassandraOperation = cassandraOperation;
    }

    private String validateAuthTokenAndFetchUserId(String authUserToken) {
        return accessTokenValidator.fetchUserIdFromAccessToken(authUserToken);
    }

    public SBApiResponse fetchEhrmsProfileDetail(String rootOrgId, String authToken) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.EHRMS);
        try {
            String userId = validateAuthTokenAndFetchUserId(authToken);
            Map<String, Object> propertyMap = new HashMap<>();
            propertyMap.put(Constants.USER_ID_LOWER, userId);
            Map<String, Object> result = cassandraOperation.getRecordsByProperties(
                    Constants.KEYSPACE_SUNBIRD,
                    Constants.USER,
                    propertyMap,
                    Arrays.asList(Constants.PROFILE_DETAILS_LOWER, Constants.USER_ID_LOWER),
                    Constants.USER_ID
            );
            if (result != null && !result.isEmpty()) {
                String userProfileDetails = (String) ((Map<String, Object>) result.get(userId)).get(Constants.PROFILE_DETAILS_LOWER);
                Map<String, Object> userDetails = mapper.readValue(userProfileDetails, new TypeReference<Map<String, Object>>() {
                });
                String externalSystemId = (String) ((Map<String, Object>) userDetails.get(Constants.ADDITIONAL_PROPERTIES)).get(Constants.EXTERNAL_SYSTEM_ID);
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
        HttpEntity entity = new HttpEntity(body, new HttpHeaders());
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
}
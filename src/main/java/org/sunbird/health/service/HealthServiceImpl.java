package org.sunbird.health.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sunbird.cache.service.RedisCacheService;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;

import java.util.*;

@Service
public class HealthServiceImpl implements HealthService {



    CassandraOperation cassandraOperation;


    RedisCacheService redisCacheService;
    @Autowired
    public HealthServiceImpl(CassandraOperation cassandraOperation, RedisCacheService redisCacheService) {
        this.cassandraOperation = cassandraOperation;
        this.redisCacheService = redisCacheService;
    }

    private final  Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    public SBApiResponse checkHealthStatus() throws Exception {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_HEALTH_CHECK);
        try {
            response.put(Constants.HEALTHY, true);
            List<Map<String, Object>> healthResults = new ArrayList<>();
            response.put(Constants.CHECKS, healthResults);
            cassandraHealthStatus(response);
            redisHealthStatus(response);
        } catch (Exception e) {
            log.error("Failed to process health check. Exception: ", e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErr(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private void cassandraHealthStatus(SBApiResponse response) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.NAME, Constants.CASSANDRA_DB);
        Boolean res = true;
        List<Map<String, Object>> cassandraQueryResponse = cassandraOperation.getRecordsByProperties(
                Constants.KEYSPACE_SUNBIRD, Constants.TABLE_SYSTEM_SETTINGS, null, null);
        if (cassandraQueryResponse.isEmpty()) {
            res = false;
            response.put(Constants.HEALTHY, res);
        }
        result.put(Constants.HEALTHY, res);
        ((List<Map<String, Object>>) response.get(Constants.CHECKS)).add(result);
    }

    private void redisHealthStatus(SBApiResponse response) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.NAME, Constants.REDIS_CACHE);
        Boolean res = true;
        SBApiResponse redisResponse = redisCacheService.getKeys();
        if (!HttpStatus.OK.equals(redisResponse.getResponseCode())) {
            res = false;
            response.put(Constants.HEALTHY, res);
        }
        result.put(Constants.HEALTHY, res);
        ((List<Map<String, Object>>) response.get(Constants.CHECKS)).add(result);
    }

}


package org.sunbird.cache.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.sunbird.cache.RedisCache;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;

import java.util.*;

public class RedisCacheServiceImpl implements RedisCacheService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    RedisCache redisCache;

    private CbExtLogger logger = new CbExtLogger(getClass().getName());

    @Override
    public SBApiResponse deleteCache() throws Exception {
        SBApiResponse response = new SBApiResponse(Constants.API_REDIS_DELETE);
        try {
           boolean res = redisCache.deleteCache();
           if(res){
               response.getParams().setStatus(Constants.SUCCESSFUL);
               response.setResponseCode(HttpStatus.OK);
           } else{
               String errMsg = "No Keys found, Redis cache is empty";
               logger.info(errMsg);
               response.getParams().setErrmsg(errMsg);
               response.setResponseCode(HttpStatus.NOT_FOUND);
           }
        } catch (Exception e) {
            logger.error(e);
        }
        return response;
    }

    @Override
    public SBApiResponse getKeys() throws Exception {
        SBApiResponse response = new SBApiResponse(Constants.API_REDIS_GET_KEYS);
        try {
            Set<String> res = redisCache.getAllKeys();
            if(!res.isEmpty()) {
                logger.info("All Keys in Redis Cache is Fetched");
                response.getParams().setStatus(Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, res);
                response.setResponseCode(HttpStatus.OK);

            } else {
                String errMsg = "No Keys found, Redis cache is empty";
                logger.info(errMsg);
                response.getParams().setErrmsg(errMsg);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return response;
    }

    @Override
    public SBApiResponse getKeysAndValues() throws Exception {
        SBApiResponse response = new SBApiResponse(Constants.API_REDIS_GET_KEYS_VALUE_SET);
        try {

            List<Map<String, Object>> result =  redisCache.getAllKeysAndValues();

            if(!result.isEmpty()) {
                logger.info("All Keys and Values in Redis Cache is Fetched");
                response.getParams().setStatus(Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, result);
                response.setResponseCode(HttpStatus.OK);

            } else {
                String errMsg = "No Keys found, Redis cache is empty";
                logger.info(errMsg);
                response.getParams().setErrmsg(errMsg);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return response;
    }
}

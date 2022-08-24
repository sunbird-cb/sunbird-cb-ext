package org.sunbird.cache;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;

@Component
public class RedisCacheMgr {

    private static final int cache_ttl = 84600;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    CbExtServerProperties cbExtServerProperties;

    private CbExtLogger logger = new CbExtLogger(getClass().getName());

    public void putCache(String key, Object object) {
        try {
            int ttl = cache_ttl;
            if (!StringUtils.isEmpty(cbExtServerProperties.getRedisTimeout())) {
                ttl = Integer.parseInt(cbExtServerProperties.getRedisTimeout());
            }
            redisTemplate.opsForValue().set(Constants.REDIS_COMMON_KEY + key, object);
            redisTemplate.expire(Constants.REDIS_COMMON_KEY + key, ttl, TimeUnit.SECONDS);
            logger.info("Cache_key_value " + Constants.REDIS_COMMON_KEY + key + " is saved in redis");
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public boolean deleteKeyByName(String key) {
        try {
            redisTemplate.delete(Constants.REDIS_COMMON_KEY + key);
            logger.info("Cache_key_value " + Constants.REDIS_COMMON_KEY + key + " is deleted from redis");
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public boolean deleteAllCBExtKey() {
        try {
            String keyPattern = Constants.REDIS_COMMON_KEY + "*";
            Set<String> keys = redisTemplate.keys(keyPattern);
            for (String key : keys) {
                redisTemplate.delete(key);
            }
            logger.info("All Keys starts with " + Constants.REDIS_COMMON_KEY + " is deleted from redis");
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public Object getCache(String key) {
        try {
            return redisTemplate.opsForValue().get(Constants.REDIS_COMMON_KEY + key);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public List<Object> mget(List<String> fields) {
        try {
            List<String> ls = new ArrayList<>();
            for (int i = 0; i < fields.size(); i++) {
				ls.add(Constants.REDIS_COMMON_KEY + Constants.QUESTION_ID + fields.get(i));
            }
            Collection<String> questionIdList = ls;
            return redisTemplate.opsForValue().multiGet(questionIdList);
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    public Set<String> getAllKeyNames() {
        Set<String> keys = null;
        try {
            String keyPattern = Constants.REDIS_COMMON_KEY + "*";
            keys = redisTemplate.keys(keyPattern);
        } catch (Exception e) {
            logger.error(e);
            return Collections.emptySet();
        }
        return keys;
    }

    public List<Map<String, Object>> getAllKeysAndValues() {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        try {
            String keyPattern = Constants.REDIS_COMMON_KEY + "*";
            Map<String, Object> res = new HashMap<>();
            Set<String> keys = redisTemplate.keys(keyPattern);
            if (!keys.isEmpty()) {
                for (String key : keys) {
                    Object entries;
                    entries = redisTemplate.opsForValue().get(key);
                    res.put(key, entries);
                }
                result.add(res);
            }
        } catch (Exception e) {
            logger.error(e);
            return Collections.emptyList();
        }
        return result;
    }
}

package org.sunbird.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

@Component
public class RedisCacheMgr {
    private static final int cache_ttl = 84600;
    @Autowired
    private JedisPool jedisPool;

    @Autowired
    CbExtServerProperties cbExtServerProperties;

    private CbExtLogger logger = new CbExtLogger(getClass().getName());

    public Jedis getJedis() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis;
        }
    }

    public void putCache(String key, Object object) {
        try {
            int ttl = cache_ttl;
            if (!StringUtils.isEmpty(cbExtServerProperties.getRedisTimeout())) {
                ttl = Integer.parseInt(cbExtServerProperties.getRedisTimeout());
            }
            ObjectMapper objectMapper = new ObjectMapper();
            String data = objectMapper.writeValueAsString(object);
            getJedis().set(Constants.REDIS_COMMON_KEY + key, data);
            getJedis().expire(Constants.REDIS_COMMON_KEY + key, ttl);
            logger.info("Cache_key_value " + Constants.REDIS_COMMON_KEY + key + " is saved in redis");
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public boolean deleteKeyByName(String key) {
        try {
            key = key.toUpperCase();
            getJedis().del(Constants.REDIS_COMMON_KEY + key);
            logger.info("Cache_key_value " + Constants.REDIS_COMMON_KEY + key + " is deleted from redis");
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public boolean deleteAllKey() {
        try {
            String keyPattern = Constants.REDIS_COMMON_KEY + "*";
            Set<String> keys = getJedis().keys(keyPattern);
            for (String key : keys) {
                getJedis().del(key);
            }
            logger.info("All Keys starts with " + Constants.REDIS_COMMON_KEY + " is deleted from redis");
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public String getCache(String key) {
        try {
            return getJedis().get(Constants.REDIS_COMMON_KEY + key);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }



    public List<String> mget(List<String> fields) {
        try {
            List<String> ls = new ArrayList<>();
            for (int i = 0; i < fields.size(); i++) {
                ls.add(Constants.REDIS_COMMON_KEY + Constants.QUESTION_ID + fields.get(i));
            }
            String[] keysForRedis = ls.toArray(new String[ls.size()]);
            return getJedis().mget(keysForRedis);
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    public boolean deleteCache() {
        try {
            String keyPattern = "*";
            Set<String> keys = getJedis().keys(keyPattern);
            if (!keys.isEmpty()) {
                for (String key : keys) {
                    getJedis().del(key);
                }
                logger.info("All Keys in Redis Cache is Deleted");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public Set<String> getAllKeys() {
        Set<String> keys = null;
        try {
            String keyPattern = "*";
            keys = getJedis().keys(keyPattern);

        } catch (Exception e) {
            logger.error(e);
            return Collections.emptySet();
        }
        return keys;
    }

    public List<Map<String, Object>> getAllKeysAndValues() {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        try {
            String keyPattern = "*";
            Map<String, Object> res = new HashMap<>();
            Set<String> keys = getJedis().keys(keyPattern);
            if (!keys.isEmpty()) {
                for (String key : keys) {
                    Object entries;
                    entries = getJedis().get(key);
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

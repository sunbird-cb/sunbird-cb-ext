package org.sunbird.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;

import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import javax.annotation.PostConstruct;

@Component
public class RedisCacheMgr {

    private static int cache_ttl = 84600;

    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private JedisPool jedisDataPopulationPool;

    @Autowired
    CbExtServerProperties cbExtServerProperties;

    private CbExtLogger logger = new CbExtLogger(getClass().getName());
    
    ObjectMapper objectMapper = new ObjectMapper();

    private static int questions_cache_ttl = 84600;

    @PostConstruct
    public void postConstruct() {
        this.questions_cache_ttl = cbExtServerProperties.getRedisQuestionsReadTimeOut().intValue();
        if (!StringUtils.isEmpty(cbExtServerProperties.getRedisTimeout())) {
            cache_ttl = Integer.parseInt(cbExtServerProperties.getRedisTimeout());
        }
    }
    public void putCache(String key, Object object, int ttl) {
        try (Jedis jedis = jedisPool.getResource()) {
            String data = objectMapper.writeValueAsString(object);
            jedis.set(Constants.REDIS_COMMON_KEY + key, data);
            jedis.expire(Constants.REDIS_COMMON_KEY + key, ttl);
            logger.debug("Cache_key_value " + Constants.REDIS_COMMON_KEY + key + " is saved in redis");
        } catch (Exception e) {
            logger.error(e);
        }
    }
    public void putCache(String key, Object object) {
        putCache(key,object,cache_ttl);
    }
    public void putInQuestionCache(String key, Object object) {
        try (Jedis jedis = jedisPool.getResource()) {
            String data = objectMapper.writeValueAsString(object);
            jedis.set(Constants.REDIS_COMMON_KEY + key, data);
            jedis.expire(Constants.REDIS_COMMON_KEY + key, questions_cache_ttl);
            logger.debug("Cache_key_value " + Constants.REDIS_COMMON_KEY + key + " is saved in redis");
        } catch (Exception e) {
            logger.error(e);
        }
    }
    public void putStringInCache(String key, String value,int ttl) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(Constants.REDIS_COMMON_KEY + key, value);
            jedis.expire(Constants.REDIS_COMMON_KEY + key, ttl);
            logger.debug("Cache_key_value " + Constants.REDIS_COMMON_KEY + key + " is saved in redis");
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void putStringInCache(String key, String value) {
        putStringInCache(key, value, cache_ttl);
    }

    public boolean deleteKeyByName(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
        	jedis.del(Constants.REDIS_COMMON_KEY + key);
            logger.debug("Cache_key_value " + Constants.REDIS_COMMON_KEY + key + " is deleted from redis");
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public boolean deleteAllCBExtKey() {
        try (Jedis jedis = jedisPool.getResource()) {
            String keyPattern = Constants.REDIS_COMMON_KEY + "*";
            Set<String> keys = jedis.keys(keyPattern);
            for (String key : keys) {
            	jedis.del(key);
            }
            logger.info("All Keys starts with " + Constants.REDIS_COMMON_KEY + " is deleted from redis");
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public String getCache(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(Constants.REDIS_COMMON_KEY + key);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public List<String> mget(List<String> fields) {
        try (Jedis jedis = jedisPool.getResource()) {
        	String[] updatedKeys = new String[fields.size()];
            for (int i = 0; i < fields.size(); i++) {
            	updatedKeys[i] = Constants.REDIS_COMMON_KEY + Constants.QUESTION_ID + fields.get(i);
            }
            return jedis.mget(updatedKeys);
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    public Set<String> getAllKeyNames() {
        try (Jedis jedis = jedisPool.getResource()) {
            String keyPattern = Constants.REDIS_COMMON_KEY + "*";
            return jedis.keys(keyPattern);
        } catch (Exception e) {
            logger.error(e);
            return Collections.emptySet();
        }
    }

    public List<Map<String, Object>> getAllKeysAndValues() {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        try (Jedis jedis = jedisPool.getResource()) {
            String keyPattern = Constants.REDIS_COMMON_KEY + "*";
            Map<String, Object> res = new HashMap<>();
            Set<String> keys = jedis.keys(keyPattern);
            if (!keys.isEmpty()) {
                for (String key : keys) {
                    Object entries;
                    entries = jedis.get(key);
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
    
    public List<String> hget(String key, int index, String... fields) {
        try (Jedis jedis = jedisDataPopulationPool.getResource()) {
            jedis.select(index);
            return jedis.hmget(key, fields);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public String getCache(String key, Integer index) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (index != null) {
                jedis.select(index);
            }
            return jedis.get(key);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public String getCacheFromDataRedish(String key, Integer index) {
        try (Jedis jedis = jedisDataPopulationPool.getResource()) {
            if (index != null) {
                jedis.select(index);
            }
            return jedis.get(key);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public String getContentFromCache(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public Boolean isKeyExist(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(Constants.REDIS_COMMON_KEY + key);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }
}

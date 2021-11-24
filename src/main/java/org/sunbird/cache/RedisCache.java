package org.sunbird.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.sunbird.core.logger.CbExtLogger;

import java.util.Set;

@Component
public class RedisCache {

    @Autowired
    private RedisTemplate redisTemplate;

    private CbExtLogger logger = new CbExtLogger(getClass().getName());
    private static final String KEY = "CB_EXT_";

    public void putCache(String key, Object object) {
        try {
            redisTemplate.opsForValue().set(KEY + key, object);
            logger.info("Cache_key_value " + KEY + key + " is saved in redis");
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public boolean deleteKeyByName(String key) {
        try {
            key = key.toUpperCase();
            redisTemplate.delete(KEY+key);
            logger.info("Cache_key_value " + KEY + key + " is deleted from redis");
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public boolean deleteAllKey() {
        try {
            String keyPattern = KEY+"*";
            Set<String> keys = redisTemplate.keys(keyPattern);
            for (String key : keys) {

                redisTemplate.delete(key);
            }
            logger.info("All Keys starts with " + KEY + " is deleted from redis");
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public Object getCache(String key) {
        try{
            Object entries;
            entries= redisTemplate.opsForValue().get(KEY+key);
            logger.info("Entires in Cache_key_value " + KEY + key + " is retrieved from redis");
            return  entries;
        } catch (Exception e){
            logger.error(e);
            return null;
        }
    }

}

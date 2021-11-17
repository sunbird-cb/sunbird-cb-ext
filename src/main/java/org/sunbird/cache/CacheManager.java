package org.sunbird.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.core.logger.CbExtLogger;

@Component
public class CacheManager {
    private Map<String, Object> cache = new HashMap<String, Object>();
    private Map<String, Long> cacheUpdatedTime = new HashMap<String, Long>();
    private CbExtLogger logger = new CbExtLogger(getClass().getName());

    @Autowired
    CbExtServerProperties cbProperties;

    public Object getCache(String key) {
        if (cache.containsKey(key)) {
            Long lastUpdated = cacheUpdatedTime.get(key);
            lastUpdated = (lastUpdated == null) ? 0 : lastUpdated;
            long diff = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastUpdated);
            if (diff >= cbProperties.getCacheMaxTTL()) {
                logger.info("Cache - " + key + " reached the max TTL value. Returning null to refresh.");
                return null;
            }
        }
        return cache.get(key);
    }

    public void putCache(String key, Object object) {
        cacheUpdatedTime.put(key, System.currentTimeMillis());
        cache.put(key, object);
    }
}

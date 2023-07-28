package org.sunbird.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class DataCacheMgr {
    private Map<String, String> strCacheMap = new HashMap<String, String>();

    private Map<String, Object> objCacheMap = new HashMap<String, Object>();

    public void putStringInCache(String key, String value) {
        strCacheMap.put(key, value);
    }

    public void putObjectInCache(String key, Object value) {
        objCacheMap.put(key, value);
    }

    public String getStringFromCache(String key) {
        if (strCacheMap.containsKey(key)) {
            return strCacheMap.get(key);
        }
        return "";
    }

    public Object getObjectFromCache(String key) {
        if (objCacheMap.containsKey(key)) {
            return objCacheMap.get(key);
        }
        return null;
    }
}

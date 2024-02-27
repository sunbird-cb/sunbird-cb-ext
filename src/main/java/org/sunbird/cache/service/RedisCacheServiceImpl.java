package org.sunbird.cache.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;

@Service
public class RedisCacheServiceImpl implements RedisCacheService {

	@Autowired
	RedisCacheMgr redisCache;

	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	@Override
	public SBApiResponse deleteCache() {
		SBApiResponse response = new SBApiResponse(Constants.API_REDIS_DELETE);
		boolean res = redisCache.deleteAllCBExtKey();
		if (res) {
			response.getParams().setStatus(Constants.SUCCESSFUL);
			response.setResponseCode(HttpStatus.OK);
		} else {
			String errMsg = Constants.NO_REDIS_CACHE_KEY_FOUND;
			logger.info(errMsg);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.NOT_FOUND);
		}
		return response;
	}

	@Override
	public SBApiResponse getKeys() {
		SBApiResponse response = new SBApiResponse(Constants.API_REDIS_GET_KEYS);
		Set<String> res = redisCache.getAllKeyNames();
		if (!res.isEmpty()) {
			response.getParams().setStatus(Constants.SUCCESSFUL);
			response.put(Constants.RESPONSE, res);
			response.setResponseCode(HttpStatus.OK);

		} else {
			String errMsg = Constants.NO_REDIS_CACHE_KEY_FOUND;
			logger.info(errMsg);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.NOT_FOUND);
		}
		return response;
	}

	@Override
	public SBApiResponse getKeysAndValues() {
		SBApiResponse response = new SBApiResponse(Constants.API_REDIS_GET_KEYS_VALUE_SET);
		List<Map<String, Object>> result = redisCache.getAllKeysAndValues();

		if (!result.isEmpty()) {
			logger.info("All Keys and Values in Redis Cache is Fetched");
			response.getParams().setStatus(Constants.SUCCESSFUL);
			response.put(Constants.RESPONSE, result);
			response.setResponseCode(HttpStatus.OK);
		} else {
			String errMsg = Constants.NO_REDIS_CACHE_KEY_FOUND;
			logger.info(errMsg);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.NOT_FOUND);
		}
		return response;
	}
}

package org.sunbird.cache.service;

import org.sunbird.common.model.SBApiResponse;

public interface RedisCacheService {

	public SBApiResponse deleteCache();

	public SBApiResponse getKeys();

	public SBApiResponse getKeysAndValues();

}

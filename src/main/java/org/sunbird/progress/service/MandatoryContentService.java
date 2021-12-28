package org.sunbird.progress.service;

import java.util.Map;

import org.sunbird.progress.model.MandatoryContentResponse;

public interface MandatoryContentService {

	public MandatoryContentResponse getMandatoryContentStatusForUser(String authUserToken, String rootOrg, String org,
			String userId);

	public Map<String, Object> getUserProgress(Map<String, Object> requestBody);
}

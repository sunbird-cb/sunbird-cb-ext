package org.sunbird.progress.service;

import java.util.Map;

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.progress.model.MandatoryContentResponse;

public interface MandatoryContentService {

	public MandatoryContentResponse getMandatoryContentStatusForUser(String authUserToken, String rootOrg, String org,
			String userId);

	public Map<String, Object> getUserProgress(SunbirdApiRequest requestBody, String authUserToken, String rootOrgId, String userChannel);

	public SBApiResponse getUserProgressV2(Map<String, Object> requestBody, String authUserToken, String rootOrgId, String userChannel);
}

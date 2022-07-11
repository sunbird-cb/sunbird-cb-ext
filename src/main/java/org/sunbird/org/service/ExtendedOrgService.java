package org.sunbird.org.service;

import java.util.Map;

import org.sunbird.common.model.SBApiResponse;

public interface ExtendedOrgService {
	public SBApiResponse listOrg(String mapId);

	public SBApiResponse createOrg(Map<String, Object> requestData, String userToken);

	public SBApiResponse orgExtSearch(Map<String, Object> request) throws Exception;
}

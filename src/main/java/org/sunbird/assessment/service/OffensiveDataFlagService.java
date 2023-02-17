package org.sunbird.assessment.service;

import org.sunbird.common.model.SBApiResponse;

import java.util.Map;

public interface OffensiveDataFlagService {
	public SBApiResponse createFlag(Map<String, Object> requestBody, String token);

	public SBApiResponse  getFlaggedData(String token);

	SBApiResponse updateFlag(String token, Map<String, Object> request);
}

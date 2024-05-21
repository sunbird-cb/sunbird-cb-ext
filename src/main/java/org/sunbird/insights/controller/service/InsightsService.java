package org.sunbird.insights.controller.service;

import org.sunbird.common.model.SBApiResponse;

import java.util.Map;

public interface InsightsService {

    public SBApiResponse insights(Map<String, Object> requestBody,String userId) throws Exception;

    public SBApiResponse readInsightsForOrganisation(Map<String, Object> requestBody, String userId);
}

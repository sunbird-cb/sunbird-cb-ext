package org.sunbird.trending.controller;

import org.sunbird.common.model.SBApiResponse;

import java.util.Map;

public interface TrendingService {
    public Map<String, Object> trendingSearch(Map<String, Object> requestBody, String token) throws Exception;

    public SBApiResponse trendingContentSearch(Map<String, Object> requestBody, String token) throws Exception;
}

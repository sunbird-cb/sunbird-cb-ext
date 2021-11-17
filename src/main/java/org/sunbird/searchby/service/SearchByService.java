package org.sunbird.searchby.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbird.cache.CacheManager;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.searchby.dto.SearchByFilter;
import org.sunbird.searchby.model.CompetencyInfo;
import org.sunbird.searchby.model.ProviderInfo;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@SuppressWarnings("unchecked")
public class SearchByService {
    private static final String COMPETENCY_CACHE_NAME = "competency";
    private static final String COMPETENCY_CACHE_NAME_BY_AREA = "competencyByArea";
    private static final String COMPETENCY_CACHE_NAME_BY_TYPE = "competencyByType";
    private static final String PROVIDER_CACHE_NAME = "provider";
    private static final String COMPETENCY_FACET_NAME = "competencies_v3.name";
    private static final String FILTERS = "filters";
    private static final String FACETS = "facets";
    private CbExtLogger logger = new CbExtLogger(getClass().getName());

    @Autowired
    CbExtServerProperties cbExtServerProperties;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    public Collection<CompetencyInfo> getCompetencyDetails(String authUserToken) throws Exception {
        Object object = cacheManager.getCache(COMPETENCY_CACHE_NAME);
        if (object == null) {
            logger.info("Initializing/Refreshing the Cache Value.");
            updateCompetencyDetails(authUserToken);
        }
        return (Collection<CompetencyInfo>) cacheManager.getCache(COMPETENCY_CACHE_NAME);
    }

    public Collection<CompetencyInfo> getCompetencyDetailsByFilter(String authUserToken, SearchByFilter filter) throws Exception {
        Object objectName = cacheManager.getCache(COMPETENCY_CACHE_NAME);
        Object objectArea = cacheManager.getCache(COMPETENCY_CACHE_NAME_BY_AREA);
        Object objectType = cacheManager.getCache(COMPETENCY_CACHE_NAME_BY_TYPE);
        if (objectName == null || objectArea == null || objectType == null) {
            logger.info("Initializing/Refreshing the Cache Value.");
            updateCompetencyDetails(authUserToken);
        }
        Object objectNameCache = cacheManager.getCache(COMPETENCY_CACHE_NAME);
        Object objectAreaCache = cacheManager.getCache(COMPETENCY_CACHE_NAME_BY_AREA);
        Object objectTypeCache = cacheManager.getCache(COMPETENCY_CACHE_NAME_BY_TYPE);
        Collection<CompetencyInfo> wholeCache = (Collection<CompetencyInfo>) objectNameCache;
        Map<String, CompetencyInfo> afterFilter = new HashMap<>();
        if (filter.getCompetencyName().isEmpty() && filter.getCompetencyType().isEmpty() && filter.getCompetencyArea().isEmpty()) {
            return wholeCache;
        } else {

            if (!filter.getCompetencyName().isEmpty()) {
                for (CompetencyInfo eachInfo : wholeCache) {
                    if (this.listToLowerCase(filter.getCompetencyName()).contains(this.stringToLowerCase(eachInfo.getName()))) {
                        afterFilter.put(eachInfo.getId(), eachInfo);
                    }
                }
            }
            if (!filter.getCompetencyType().isEmpty()) {
                Map<String, List<CompetencyInfo>> typeCache = (Map<String, List<CompetencyInfo>>) objectTypeCache;
                for (Map.Entry<String, List<CompetencyInfo>> eachInfo : typeCache.entrySet()) {
                    if (this.listToLowerCase(filter.getCompetencyType()).contains(this.stringToLowerCase(eachInfo.getKey()))) {
                        for (CompetencyInfo competencyInfo : eachInfo.getValue())
                            afterFilter.put(competencyInfo.getId(), competencyInfo);
                    }
                }

            }
            if (!filter.getCompetencyArea().isEmpty()) {
                Map<String, List<CompetencyInfo>> areaCache = (Map<String, List<CompetencyInfo>>) objectAreaCache;
                for (Map.Entry<String, List<CompetencyInfo>> eachInfo : areaCache.entrySet()) {
                    if (this.listToLowerCase(filter.getCompetencyArea()).contains(this.stringToLowerCase(eachInfo.getKey()))) {
                        for (CompetencyInfo competencyInfo : eachInfo.getValue())
                            afterFilter.put(competencyInfo.getId(), competencyInfo);
                    }
                }

            }

        }
        return afterFilter.values();
    }

    private List<String> listToLowerCase(List<String> convertString) {
        return convertString.stream().map(String::toLowerCase).map(String::trim).collect(Collectors.toList());
    }

    private String stringToLowerCase(String convertString) {
        return convertString.toLowerCase().trim();
    }

    public Collection<ProviderInfo> getProviderDetails(String authUserToken) throws Exception {
        Object object = cacheManager.getCache(PROVIDER_CACHE_NAME);
        if (object == null) {
            logger.info("");
            updateProviderDetails(authUserToken);
        }
        return (Collection<ProviderInfo>) cacheManager.getCache(PROVIDER_CACHE_NAME);
    }

    private void updateCompetencyDetails(String authUserToken) throws Exception {
        Map<String, CompetencyInfo> competencyMap;
        Map<String, List<CompetencyInfo>> comInfoByType = new HashMap<>();
        Map<String, List<CompetencyInfo>> comInfoByArea = new HashMap<>();

        // Get facets from Composite Search
        Map<String, String> headers = new HashMap<>();
        headers.put("x-authenticated-user-token", authUserToken);
        headers.put("authorization", cbExtServerProperties.getSbApiKey());

        HashMap<String, Object> reqBody = new HashMap<>();
        HashMap<String, Object> req = new HashMap<>();
        req.put("facets", Arrays.asList(COMPETENCY_FACET_NAME));
        Map<String, Object> filters = new HashMap<>();
        filters.put("primaryCategory", Arrays.asList("Course", "Program"));
        filters.put("status", Arrays.asList("Live"));
        req.put(FILTERS, filters);
        req.put("limit", 0);
        reqBody.put("request", req);

        Map<String, Object> compositeSearchRes = outboundRequestHandlerService.fetchResultUsingPost(
                cbExtServerProperties.getKmBaseHost() + cbExtServerProperties.getKmCompositeSearchPath(), reqBody,
                headers);

        Map<String, Object> compositeSearchResult = (Map<String, Object>) compositeSearchRes.get("result");
        List<Map<String, Object>> facetsList = (List<Map<String, Object>>) compositeSearchResult.get("facets");
        if (!CollectionUtils.isEmpty(facetsList)) {
            competencyMap = new HashMap<>();
            for (Map<String, Object> facetObj : facetsList) {
                String name = (String) facetObj.get("name");
                if (COMPETENCY_FACET_NAME.equals(name)) {
                    List<Map<String, Object>> facetValueList = (List<Map<String, Object>>) facetObj.get("values");
                    if (!CollectionUtils.isEmpty(facetValueList)) {
                        for (Map<String, Object> facetValueObj : facetValueList) {
                            CompetencyInfo compInfo = new CompetencyInfo();
                            compInfo.setContentCount((int) facetValueObj.get("count"));
                            competencyMap.put((String) facetValueObj.get("name"), compInfo);
                        }
                    }
                }
            }
        } else {
            Exception err = new Exception("Failed to get facets value from Composite Search API.");
            logger.error(err);
            try {
                logger.info("Received Response: " + (new ObjectMapper()).writeValueAsString(compositeSearchResult));
            } catch (Exception e) {
            }
            throw err;
        }

        // Get Competency Values
        headers = new HashMap<>();
        headers.put("authorization", "bearer " + authUserToken);
        reqBody = new HashMap<>();
        List<Map<String, Object>> searchList = new ArrayList<>();

        for (String compName : competencyMap.keySet()) {
            Map<String, Object> compSearchObj = new HashMap<>();
            compSearchObj.put("type", "COMPETENCY");
            compSearchObj.put("field", "name");
            compSearchObj.put("keyword", compName);
            searchList.add(compSearchObj);
        }
        reqBody.put("searches", searchList);
        reqBody.put("childCount", false);
        reqBody.put("childNodes", false);

        Map<String, Object> fracSearchRes = outboundRequestHandlerService.fetchResultUsingPost(
                cbExtServerProperties.getFracHost() + cbExtServerProperties.getFracSearchPath(), reqBody, headers);
        List<Map<String, Object>> fracResponseList = (List<Map<String, Object>>) fracSearchRes.get("responseData");

        if (!CollectionUtils.isEmpty(fracResponseList)) {

            for (Map<String, Object> respObj : fracResponseList) {
                String compName = ((String) respObj.get("name")).toLowerCase();
                if (competencyMap.containsKey(compName)) {
                    CompetencyInfo compInfo = competencyMap.get(compName);
                    compInfo.setName((String) respObj.get("name"));
                    compInfo.setCompetencyArea(
                            ((Map<String, String>) respObj.get("additionalProperties")).get("competencyArea"));
                    compInfo.setCompetencyType(
                            ((Map<String, String>) respObj.get("additionalProperties")).get("competencyType"));
                    compInfo.setDescription((String) respObj.get("description"));
                    compInfo.setId((String) respObj.get("id"));
                    compInfo.setSource((String) respObj.get("source"));
                    compInfo.setStatus((String) respObj.get("status"));
                    competencyMap.put(compName, compInfo);

//					// Competency Map by Type
                    if (!compInfo.getCompetencyType().isEmpty()) {
                        String competencyType = compInfo.getCompetencyType();
                        List<CompetencyInfo> competencyInfoList;
                        if (comInfoByType.containsKey(competencyType)) {
                            competencyInfoList = comInfoByType.get(competencyType);
                        } else {
                            competencyInfoList = new ArrayList<>();
                        }
                        competencyInfoList.add(compInfo);
                        comInfoByType.put(competencyType, competencyInfoList);
                    }
//					// Competency Map by Area
                    if (!compInfo.getCompetencyArea().isEmpty()) {
                        String competencyArea = compInfo.getCompetencyArea();
                        List<CompetencyInfo> competencyInfoList;
                        if (comInfoByArea.containsKey(competencyArea)) {
                            competencyInfoList = comInfoByArea.get(competencyArea);
                        } else {
                            competencyInfoList = new ArrayList<>();
                        }
                        competencyInfoList.add(compInfo);
                        comInfoByArea.put(competencyArea, competencyInfoList);
                    }
                }

            }
        } else {
            Exception err = new Exception("Failed to get competency info from FRAC API.");
            logger.error(err);
            try {
                logger.info("Received Response: " + (new ObjectMapper()).writeValueAsString(fracSearchRes));
            } catch (Exception e) {
            }
            throw err;
        }

        cacheManager.putCache(COMPETENCY_CACHE_NAME, competencyMap.values());
        cacheManager.putCache(COMPETENCY_CACHE_NAME_BY_TYPE, comInfoByType);
        cacheManager.putCache(COMPETENCY_CACHE_NAME_BY_AREA, comInfoByArea);
    }

    private void updateProviderDetails(String authUserToken) throws Exception {
        Map<String, ProviderInfo> providerMap = null;

        // Get facets from Composite Search
        Map<String, String> headers = new HashMap<>();
        headers.put("x-authenticated-user-token", authUserToken);
        headers.put("authorization", cbExtServerProperties.getSbApiKey());

        HashMap<String, Object> reqBody = new HashMap<>();
        HashMap<String, Object> req = new HashMap<>();
        req.put("facets", Arrays.asList("source"));
        Map<String, Object> filters = new HashMap<>();
        filters.put("primaryCategory", Arrays.asList("Course", "Program"));
        filters.put("status", Arrays.asList("Live"));
        req.put(FILTERS, filters);
        req.put("limit", 0);
        reqBody.put("request", req);

        Map<String, Object> compositeSearchRes = outboundRequestHandlerService.fetchResultUsingPost(
                cbExtServerProperties.getKmBaseHost() + cbExtServerProperties.getKmCompositeSearchPath(), reqBody,
                headers);

        Map<String, Object> compositeSearchResult = (Map<String, Object>) compositeSearchRes.get("result");
        List<Map<String, Object>> facetsList = (List<Map<String, Object>>) compositeSearchResult.get("facets");
        if (!CollectionUtils.isEmpty(facetsList)) {
            providerMap = new HashMap<>();
            for (Map<String, Object> facetObj : facetsList) {
                String name = (String) facetObj.get("name");
                if ("source".equals(name)) {
                    List<Map<String, Object>> facetValueList = (List<Map<String, Object>>) facetObj.get("values");
                    if (!CollectionUtils.isEmpty(facetValueList)) {
                        for (Map<String, Object> facetValueObj : facetValueList) {
                            ProviderInfo provInfo = new ProviderInfo();
                            provInfo.setContentCount((int) facetValueObj.get("count"));
                            providerMap.put((String) facetValueObj.get("name"), provInfo);
                        }
                    }
                }
            }
        } else {
            Exception err = new Exception("Failed to get facets value from Composite Search API.");
            logger.error(err);
            try {
                logger.info("Received Response: " + (new ObjectMapper()).writeValueAsString(compositeSearchResult));
            } catch (Exception e) {
            }
            throw err;
        }

        // Get Provider Values
        reqBody = new HashMap<>();
        req = new HashMap<>();
        filters = new HashMap<>();
        filters.put("channel", providerMap.keySet().toArray());
        filters.put("isTenant", true);
        req.put(FILTERS, filters);
        reqBody.put("request", req);

        Map<String, Object> orgSearchRes = outboundRequestHandlerService.fetchResultUsingPost(
                cbExtServerProperties.getSbUrl() + cbExtServerProperties.getSbOrgSearchPath(), reqBody, headers);

        Map<String, Object> orgSearchResponse = (Map<String, Object>) ((Map<String, Object>) orgSearchRes.get("result"))
                .get("response");

        List<Map<String, Object>> orgResponseList = (List<Map<String, Object>>) orgSearchResponse.get("content");

        if (!CollectionUtils.isEmpty(orgResponseList)) {
            for (Map<String, Object> respObj : orgResponseList) {
                String channelName = ((String) respObj.get("channel")).toLowerCase();
                if (providerMap.containsKey(channelName)) {
                    ProviderInfo provInfo = providerMap.get(channelName);
                    provInfo.setName((String) respObj.get("channel"));
                    provInfo.setDescription((String) respObj.get("description"));
                    provInfo.setLogoUrl((String) respObj.get("imgurl"));
                    provInfo.setOrgId((String) respObj.get("id"));
                    providerMap.put(channelName, provInfo);
                }
            }
        } else {
            Exception err = new Exception("Failed to get competency info from FRAC API.");
            logger.error(err);
            try {
                logger.info("Received Response: " + (new ObjectMapper()).writeValueAsString(orgSearchRes));
            } catch (Exception e) {
                logger.error(e);
            }
            throw err;
        }

        cacheManager.putCache(PROVIDER_CACHE_NAME, providerMap.values());
    }
}
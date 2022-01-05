package org.sunbird.searchby.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.exception.MyOwnRuntimeException;
import org.sunbird.searchby.model.CompetencyInfo;
import org.sunbird.searchby.model.ProviderInfo;
import org.sunbird.searchby.model.SearchByFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@SuppressWarnings("unchecked")
public class SearchByService {

	private static final String RECEIVED_RESPONSE_S = "Received Response: %s";

	private static final String SOURCE = "source";

	private static final String RESULT = "result";

	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	@Autowired
	CbExtServerProperties cbExtServerProperties;

	@Autowired
	RedisCacheMgr redisCacheMgr;

	@Autowired
	OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	public Collection<CompetencyInfo> getCompetencyDetails(String authUserToken)
			throws MyOwnRuntimeException, JsonProcessingException {
		Object object = redisCacheMgr.getCache(Constants.COMPETENCY_CACHE_NAME);
		if (object == null) {
			logger.info("Initializing/Refreshing the Cache Value.");
			updateCompetencyDetails(authUserToken);
		}

		Map<String, CompetencyInfo> competencyMap = (Map<String, CompetencyInfo>) redisCacheMgr
				.getCache(Constants.COMPETENCY_CACHE_NAME);
		return competencyMap.values();
	}

	public Collection<CompetencyInfo> getCompetencyDetailsByFilter(String authUserToken, SearchByFilter filter)
			throws MyOwnRuntimeException, JsonProcessingException {

		if (filter.isEmptyFilter()) {
			return getCompetencyDetails(authUserToken);
		}

		Map<String, CompetencyInfo> objectNameCache = (Map<String, CompetencyInfo>) redisCacheMgr
				.getCache(Constants.COMPETENCY_CACHE_NAME);
		Object objectAreaCache = redisCacheMgr.getCache(Constants.COMPETENCY_CACHE_NAME_BY_AREA);
		Object objectTypeCache = redisCacheMgr.getCache(Constants.COMPETENCY_CACHE_NAME_BY_TYPE);
		if (CollectionUtils.isEmpty(objectNameCache) || objectAreaCache == null || objectTypeCache == null) {
			logger.info("Initializing/Refreshing the Cache Value.");
			updateCompetencyDetails(authUserToken);
		}
		objectNameCache = (Map<String, CompetencyInfo>) redisCacheMgr.getCache(Constants.COMPETENCY_CACHE_NAME);
		objectAreaCache = redisCacheMgr.getCache(Constants.COMPETENCY_CACHE_NAME_BY_AREA);
		objectTypeCache = redisCacheMgr.getCache(Constants.COMPETENCY_CACHE_NAME_BY_TYPE);

		// Apply Name filter
		Map<String, CompetencyInfo> afterFilter = new HashMap<>();
		if (!CollectionUtils.isEmpty(filter.getCompetencyName())) {
			List<String> lowerCaseNameFilter = listToLowerCase(filter.getCompetencyName());
			for (CompetencyInfo eachInfo : objectNameCache.values()) {
				if (lowerCaseNameFilter.contains(eachInfo.getName().toLowerCase().trim())) {
					afterFilter.put(eachInfo.getId(), eachInfo);
				}
			}
		}

		if (!CollectionUtils.isEmpty(filter.getCompetencyType())) {
			List<String> lowerCaseTypeFilter = listToLowerCase(filter.getCompetencyType());
			Map<String, List<CompetencyInfo>> typeCache = (Map<String, List<CompetencyInfo>>) objectTypeCache;
			for (Map.Entry<String, List<CompetencyInfo>> eachInfo : typeCache.entrySet()) {
				if (lowerCaseTypeFilter.contains(eachInfo.getKey().toLowerCase().trim())) {
					for (CompetencyInfo competencyInfo : eachInfo.getValue()) {
						afterFilter.put(competencyInfo.getId(), competencyInfo);
					}
				}
			}
		}

		if (!CollectionUtils.isEmpty(filter.getCompetencyArea())) {
			List<String> lowerCaseAreaFilter = listToLowerCase(filter.getCompetencyArea());
			Map<String, List<CompetencyInfo>> areaCache = (Map<String, List<CompetencyInfo>>) objectAreaCache;
			for (Map.Entry<String, List<CompetencyInfo>> eachInfo : areaCache.entrySet()) {
				if (lowerCaseAreaFilter.contains(eachInfo.getKey().toLowerCase().trim())) {
					for (CompetencyInfo competencyInfo : eachInfo.getValue()) {
						afterFilter.put(competencyInfo.getId(), competencyInfo);
					}
				}
			}
		}
		return afterFilter.values();
	}

	public Collection<ProviderInfo> getProviderDetails(String authUserToken) throws Exception {
		Object object = redisCacheMgr.getCache(Constants.PROVIDER_CACHE_NAME);
		if (object == null) {
			logger.info("");
			updateProviderDetails(authUserToken);
		}
		return (Collection<ProviderInfo>) redisCacheMgr.getCache(Constants.PROVIDER_CACHE_NAME);
	}

	private List<String> listToLowerCase(List<String> convertString) {
		return convertString.stream().map(String::toLowerCase).map(String::trim).collect(Collectors.toList());
	}

	private void updateCompetencyDetails(String authUserToken) throws MyOwnRuntimeException, JsonProcessingException {
		Map<String, CompetencyInfo> competencyMap;
		Map<String, List<CompetencyInfo>> comInfoByType = new HashMap<>();
		Map<String, List<CompetencyInfo>> comInfoByArea = new HashMap<>();

		// Get facets from Composite Search
		Map<String, String> headers = new HashMap<>();
		headers.put(Constants.USER_TOKEN, authUserToken);
		headers.put(Constants.AUTHORIZATION, cbExtServerProperties.getSbApiKey());

		HashMap<String, Object> reqBody = new HashMap<>();
		HashMap<String, Object> req = new HashMap<>();
		req.put(Constants.FACETS, Arrays.asList(Constants.COMPETENCY_FACET_NAME));
		Map<String, Object> filters = new HashMap<>();
		filters.put(Constants.PRIMARY_CATEGORY, Arrays.asList("Course", "Program"));
		filters.put(Constants.STATUS, Arrays.asList("Live"));
		req.put(Constants.FILTERS, filters);
		req.put(Constants.LIMIT, 0);
		reqBody.put(Constants.REQUEST, req);

		Map<String, Object> compositeSearchRes = outboundRequestHandlerService.fetchResultUsingPost(
				cbExtServerProperties.getKmBaseHost() + cbExtServerProperties.getKmCompositeSearchPath(), reqBody,
				headers);

		Map<String, Object> compositeSearchResult = (Map<String, Object>) compositeSearchRes.get(RESULT);
		List<Map<String, Object>> facetsList = (List<Map<String, Object>>) compositeSearchResult.get("facets");
		if (!CollectionUtils.isEmpty(facetsList)) {
			competencyMap = new HashMap<>();
			for (Map<String, Object> facetObj : facetsList) {
				String name = (String) facetObj.get("name");
				if (Constants.COMPETENCY_FACET_NAME.equals(name)) {
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
			MyOwnRuntimeException err = new MyOwnRuntimeException(
					"Failed to get facets value from Composite Search API.");
			logger.error(err);
			logger.info(
					String.format(RECEIVED_RESPONSE_S, new ObjectMapper().writeValueAsString(compositeSearchResult)));
			throw err;
		}

		// Get Competency Values
		headers = new HashMap<>();
		headers.put(Constants.AUTHORIZATION, "bearer " + authUserToken);
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
					compInfo.setSource((String) respObj.get(SOURCE));
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
			MyOwnRuntimeException err = new MyOwnRuntimeException("Failed to get competency info from FRAC API.");
			logger.error(err);
			logger.info(String.format(RECEIVED_RESPONSE_S, new ObjectMapper().writeValueAsString(fracSearchRes)));
			throw err;
		}

		redisCacheMgr.putCache(Constants.COMPETENCY_CACHE_NAME, competencyMap);
		redisCacheMgr.putCache(Constants.COMPETENCY_CACHE_NAME_BY_TYPE, comInfoByType);
		redisCacheMgr.putCache(Constants.COMPETENCY_CACHE_NAME_BY_AREA, comInfoByArea);
	}

	private void updateProviderDetails(String authUserToken) throws MyOwnRuntimeException, JsonProcessingException {
		Map<String, ProviderInfo> providerMap = null;

		// Get facets from Composite Search
		Map<String, String> headers = new HashMap<>();
		headers.put(Constants.USER_TOKEN, authUserToken);
		headers.put(Constants.AUTHORIZATION, cbExtServerProperties.getSbApiKey());

		HashMap<String, Object> reqBody = new HashMap<>();
		HashMap<String, Object> req = new HashMap<>();
		req.put(Constants.FACETS, Arrays.asList(SOURCE));
		Map<String, Object> filters = new HashMap<>();
		filters.put(Constants.PRIMARY_CATEGORY, Arrays.asList("Course", "Program"));
		filters.put(Constants.STATUS, Arrays.asList("Live"));
		req.put(Constants.FILTERS, filters);
		req.put(Constants.LIMIT, 0);
		reqBody.put(Constants.REQUEST, req);

		Map<String, Object> compositeSearchRes = outboundRequestHandlerService.fetchResultUsingPost(
				cbExtServerProperties.getKmBaseHost() + cbExtServerProperties.getKmCompositeSearchPath(), reqBody,
				headers);

		Map<String, Object> compositeSearchResult = (Map<String, Object>) compositeSearchRes.get(RESULT);
		List<Map<String, Object>> facetsList = (List<Map<String, Object>>) compositeSearchResult.get("facets");
		if (!CollectionUtils.isEmpty(facetsList)) {
			providerMap = new HashMap<>();
			for (Map<String, Object> facetObj : facetsList) {
				String name = (String) facetObj.get("name");
				if (SOURCE.equals(name)) {
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
			MyOwnRuntimeException err = new MyOwnRuntimeException(
					"Failed to get facets value from Composite Search API.");
			logger.error(err);
			logger.info(
					String.format(RECEIVED_RESPONSE_S, new ObjectMapper().writeValueAsString(compositeSearchResult)));
			throw err;
		}

		// Get Provider Values
		reqBody = new HashMap<>();
		req = new HashMap<>();
		filters = new HashMap<>();
		filters.put(Constants.CHANNEL, providerMap.keySet().toArray());
		filters.put(Constants.IS_TENANT, true);
		req.put(Constants.FILTERS, filters);
		reqBody.put(Constants.REQUEST, req);

		Map<String, Object> orgSearchRes = outboundRequestHandlerService.fetchResultUsingPost(
				cbExtServerProperties.getSbUrl() + cbExtServerProperties.getSbOrgSearchPath(), reqBody, headers);

		Map<String, Object> orgSearchResponse = (Map<String, Object>) ((Map<String, Object>) orgSearchRes.get(RESULT))
				.get("response");

		List<Map<String, Object>> orgResponseList = (List<Map<String, Object>>) orgSearchResponse.get("content");

		if (!CollectionUtils.isEmpty(orgResponseList)) {
			for (Map<String, Object> respObj : orgResponseList) {
				String channelName = ((String) respObj.get(Constants.CHANNEL)).toLowerCase();
				if (providerMap.containsKey(channelName)) {
					ProviderInfo provInfo = providerMap.get(channelName);
					provInfo.setName((String) respObj.get(Constants.CHANNEL));
					provInfo.setDescription((String) respObj.get("description"));
					provInfo.setLogoUrl((String) respObj.get("imgurl"));
					provInfo.setOrgId((String) respObj.get("id"));
					providerMap.put(channelName, provInfo);
				}
			}
		} else {
			MyOwnRuntimeException err = new MyOwnRuntimeException("Failed to get competency info from FRAC API.");
			logger.error(err);
			logger.info(String.format(RECEIVED_RESPONSE_S, new ObjectMapper().writeValueAsString(orgSearchRes)));
			throw err;
		}

		redisCacheMgr.putCache(Constants.PROVIDER_CACHE_NAME, providerMap.values());
	}
}
package org.sunbird.searchby.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.common.model.FracApiResponse;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.AccessTokenValidator;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.searchby.model.CompetencyInfo;
import org.sunbird.searchby.model.FracCommonInfo;
import org.sunbird.searchby.model.ProviderInfo;
import org.sunbird.workallocation.model.FracStatusInfo;

import java.util.*;

@Service
@SuppressWarnings("unchecked")
public class SearchByService {

	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	@Autowired
	CbExtServerProperties cbExtServerProperties;

	@Autowired
	RedisCacheMgr redisCacheMgr;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	@Autowired
	AccessTokenValidator accessTokenValidator;

	public Collection<CompetencyInfo> getCompetencyDetails(String authUserToken) throws Exception {
		String strCompetencyMap = redisCacheMgr.getCache(Constants.COMPETENCY_CACHE_NAME);
		Map<String, CompetencyInfo> competencyMap = new HashMap<>();
		if (!StringUtils.isEmpty(strCompetencyMap)) {
			competencyMap = mapper.readValue(strCompetencyMap, new TypeReference<Map<String, CompetencyInfo>>() {
			});
		}

		if (CollectionUtils.isEmpty(competencyMap)) {
			logger.info("Initializing/Refreshing the Cache Value for Key : " + Constants.COMPETENCY_CACHE_NAME);
			competencyMap = updateCompetencyDetails(authUserToken);
		}
		return competencyMap.values();
	}

	public Collection<ProviderInfo> getProviderDetails(String authUserToken) throws Exception {
		String strProviderInfo = redisCacheMgr.getCache(Constants.PROVIDER_CACHE_NAME);
		Map<String, ProviderInfo> providerMap = new HashMap<>();
		if (!StringUtils.isEmpty(strProviderInfo)) {
			providerMap = mapper.readValue(strProviderInfo, new TypeReference<Map<String, ProviderInfo>>() {
			});
		}

		if (CollectionUtils.isEmpty(providerMap)) {
			logger.info("Initializing/Refreshing the Cache Value for Key : " + Constants.PROVIDER_CACHE_NAME);
			providerMap = updateProviderDetails(authUserToken);
		}
		return providerMap.values();
	}

	public FracApiResponse listPositions(String userToken) {
		FracApiResponse response = new FracApiResponse();
		response.setStatusInfo(new FracStatusInfo());
		response.getStatusInfo().setStatusCode(HttpStatus.OK.value());
		try {
			Map<String, List<FracCommonInfo>> positionMap = new HashMap<>();
			String strPositionMap = redisCacheMgr.getCache(Constants.POSITIONS_CACHE_NAME);
			if (!StringUtils.isEmpty(strPositionMap)) {
				positionMap = mapper.readValue(strPositionMap, new TypeReference<Map<String, List<FracCommonInfo>>>() {
				});
			}

			if (ObjectUtils.isEmpty(positionMap)
					|| CollectionUtils.isEmpty(positionMap.get(Constants.POSITIONS_CACHE_NAME))) {
				logger.info("Initializing / Refreshing the Cache value for key : " + Constants.POSITIONS_CACHE_NAME);
				try {
					positionMap = updateDesignationDetails(userToken);
					response.setResponseData(positionMap.get(Constants.POSITIONS_CACHE_NAME));
				} catch (Exception e) {
					logger.error(e);
					response.getStatusInfo().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
					response.getStatusInfo().setErrorMessage(e.getMessage());
				}
			} else {
				response.setResponseData(positionMap.get(Constants.POSITIONS_CACHE_NAME));
			}
		} catch (Exception e) {
			response.getStatusInfo().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return response;
	}

	private Map<String, CompetencyInfo> updateCompetencyDetails(String authUserToken) throws Exception {
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
		filters.put(Constants.PRIMARY_CATEGORY, Arrays.asList(Constants.COURSE, Constants.PROGRAM));
		filters.put(Constants.STATUS, Arrays.asList(Constants.LIVE));
		req.put(Constants.FILTERS, filters);
		req.put(Constants.LIMIT, 0);
		reqBody.put(Constants.REQUEST, req);

		Map<String, Object> compositeSearchRes = outboundRequestHandlerService.fetchResultUsingPost(
				cbExtServerProperties.getKmBaseHost() + cbExtServerProperties.getKmCompositeSearchPath(), reqBody,
				headers);

		Map<String, Object> compositeSearchResult = (Map<String, Object>) compositeSearchRes.get(Constants.RESULT);
		List<Map<String, Object>> facetsList = (List<Map<String, Object>>) compositeSearchResult.get(Constants.FACETS);
		if (!CollectionUtils.isEmpty(facetsList)) {
			competencyMap = new HashMap<>();
			for (Map<String, Object> facetObj : facetsList) {
				String name = (String) facetObj.get(Constants.NAME);
				if (Constants.COMPETENCY_FACET_NAME.equals(name)) {
					List<Map<String, Object>> facetValueList = (List<Map<String, Object>>) facetObj
							.get(Constants.VALUES);
					if (!CollectionUtils.isEmpty(facetValueList)) {
						for (Map<String, Object> facetValueObj : facetValueList) {
							CompetencyInfo compInfo = new CompetencyInfo();
							// TODO - Make sure which competency field is unique
							compInfo.setName((String) facetValueObj.get(Constants.NAME));
							compInfo.setContentCount((int) facetValueObj.get(Constants.COUNT));
							competencyMap.put((String) facetValueObj.get(Constants.NAME), compInfo);
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
		headers.put(Constants.AUTHORIZATION, Constants.BEARER + authUserToken);
		reqBody = new HashMap<>();
		List<Map<String, Object>> searchList = new ArrayList<>();

		for (String compName : competencyMap.keySet()) {
			Map<String, Object> compSearchObj = new HashMap<>();
			compSearchObj.put(Constants.TYPE, Constants.COMPETENCY.toUpperCase());
			compSearchObj.put(Constants.FIELD, Constants.NAME);
			compSearchObj.put(Constants.KEYWORD, compName);
			searchList.add(compSearchObj);
		}
		reqBody.put(Constants.SEARCHES, searchList);
		reqBody.put(Constants.CHILD_COUNT, false);
		reqBody.put(Constants.CHILD_NODES, false);

		Map<String, Object> fracSearchRes = outboundRequestHandlerService.fetchResultUsingPost(
				cbExtServerProperties.getFracHost() + cbExtServerProperties.getFracSearchPath(), reqBody, headers);

		List<Map<String, Object>> fracResponseList = (List<Map<String, Object>>) fracSearchRes
				.get(Constants.RESPONSE_DATA);

		if (!CollectionUtils.isEmpty(fracResponseList)) {
			for (Map<String, Object> respObj : fracResponseList) {
				String compName = ((String) respObj.get(Constants.NAME)).toLowerCase();
				if (competencyMap.containsKey(compName)) {
					CompetencyInfo compInfo = competencyMap.get(compName);
					compInfo.setName((String) respObj.get(Constants.NAME));
					compInfo.setCompetencyArea(((Map<String, String>) respObj.get(Constants.ADDITIONAL_PROPERTIES))
							.get(Constants.COMPETENCY_AREA));
					compInfo.setCompetencyType(((Map<String, String>) respObj.get(Constants.ADDITIONAL_PROPERTIES))
							.get(Constants.COMPETENCY_TYPE));
					compInfo.setDescription((String) respObj.get(Constants.DESCRIPTION));
					compInfo.setId((String) respObj.get(Constants.ID));
					compInfo.setSource((String) respObj.get(Constants.SOURCE));
					compInfo.setStatus((String) respObj.get(Constants.STATUS));
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

		redisCacheMgr.putCache(Constants.COMPETENCY_CACHE_NAME, competencyMap);
		redisCacheMgr.putCache(Constants.COMPETENCY_CACHE_NAME_BY_TYPE, comInfoByType);
		redisCacheMgr.putCache(Constants.COMPETENCY_CACHE_NAME_BY_AREA, comInfoByArea);

		return competencyMap;
	}

	private Map<String, ProviderInfo> updateProviderDetails(String authUserToken) throws Exception {
		Map<String, ProviderInfo> providerMap = null;

		// Get facets from Composite Search
		Map<String, String> headers = new HashMap<>();
		headers.put(Constants.USER_TOKEN, authUserToken);
		headers.put(Constants.AUTHORIZATION, cbExtServerProperties.getSbApiKey());

		HashMap<String, Object> reqBody = new HashMap<>();
		HashMap<String, Object> req = new HashMap<>();
		req.put(Constants.FACETS, Arrays.asList(Constants.SOURCE));
		Map<String, Object> filters = new HashMap<>();
		filters.put(Constants.PRIMARY_CATEGORY, Arrays.asList(Constants.COURSE, Constants.PROGRAM));
		filters.put(Constants.STATUS, Arrays.asList(Constants.LIVE));
		req.put(Constants.FILTERS, filters);
		req.put(Constants.LIMIT, 0);
		reqBody.put(Constants.REQUEST, req);

		Map<String, Object> compositeSearchRes = outboundRequestHandlerService.fetchResultUsingPost(
				cbExtServerProperties.getKmBaseHost() + cbExtServerProperties.getKmCompositeSearchPath(), reqBody,
				headers);

		Map<String, Object> compositeSearchResult = (Map<String, Object>) compositeSearchRes.get(Constants.RESULT);
		List<Map<String, Object>> facetsList = (List<Map<String, Object>>) compositeSearchResult.get(Constants.FACETS);
		if (!CollectionUtils.isEmpty(facetsList)) {
			providerMap = new HashMap<String, ProviderInfo>();
			for (Map<String, Object> facetObj : facetsList) {
				String name = (String) facetObj.get(Constants.NAME);
				if (Constants.SOURCE.equalsIgnoreCase(name)) {
					List<Map<String, Object>> facetValueList = (List<Map<String, Object>>) facetObj
							.get(Constants.VALUES);
					if (!CollectionUtils.isEmpty(facetValueList)) {
						for (Map<String, Object> facetValueObj : facetValueList) {
							ProviderInfo provInfo = new ProviderInfo();
							provInfo.setName((String) facetValueObj.get(Constants.NAME));
							provInfo.setContentCount((int) facetValueObj.get(Constants.COUNT));
							providerMap.put((String) facetValueObj.get(Constants.NAME), provInfo);
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
		filters.put(Constants.CHANNEL, providerMap.keySet().toArray());
		filters.put(Constants.IS_TENANT, true);
		req.put(Constants.FILTERS, filters);
		reqBody.put(Constants.REQUEST, req);

		Map<String, Object> orgSearchRes = outboundRequestHandlerService.fetchResultUsingPost(
				cbExtServerProperties.getSbUrl() + cbExtServerProperties.getSbOrgSearchPath(), reqBody, headers);

		Map<String, Object> orgSearchResponse = (Map<String, Object>) ((Map<String, Object>) orgSearchRes
				.get(Constants.RESULT)).get(Constants.RESPONSE);

		List<Map<String, Object>> orgResponseList = (List<Map<String, Object>>) orgSearchResponse
				.get(Constants.CONTENT);

		if (!CollectionUtils.isEmpty(orgResponseList)) {
			for (Map<String, Object> respObj : orgResponseList) {
				String channelName = ((String) respObj.get(Constants.CHANNEL)).toLowerCase();
				if (providerMap.containsKey(channelName)) {
					ProviderInfo provInfo = providerMap.get(channelName);
					provInfo.setName((String) respObj.get(Constants.CHANNEL));
					provInfo.setDescription((String) respObj.get(Constants.DESCRIPTION));
					provInfo.setLogoUrl((String) respObj.get(Constants.IMG_URL_KEY));
					provInfo.setOrgId((String) respObj.get(Constants.ID));
					providerMap.put(channelName, provInfo);
				}
			}
		} else {
			Exception err = new Exception("Failed to get competency info from FRAC API.");
			logger.error(err);
			try {
				logger.info("Received Response: " + (new ObjectMapper()).writeValueAsString(orgSearchRes));
			} catch (Exception e) {
			}
			throw err;
		}

		redisCacheMgr.putCache(Constants.PROVIDER_CACHE_NAME, providerMap);
		return providerMap;
	}

	private Map<String, List<FracCommonInfo>> updateDesignationDetails(String authUserToken) throws Exception {
		Map<String, String> headers = new HashMap<>();
		HashMap<String, Object> reqBody = new HashMap<>();
		headers = new HashMap<>();
		headers.put(Constants.AUTHORIZATION, Constants.BEARER + authUserToken);
		reqBody = new HashMap<>();
		List<Map<String, Object>> searchList = new ArrayList<>();
		Map<String, Object> compSearchObj = new HashMap<>();
		compSearchObj.put(Constants.TYPE, Constants.POSITION.toUpperCase());
		compSearchObj.put(Constants.FIELD, Constants.NAME);
		compSearchObj.put(Constants.KEYWORD, StringUtils.EMPTY);
		searchList.add(compSearchObj);

		compSearchObj = new HashMap<String, Object>();
		compSearchObj.put(Constants.TYPE, Constants.POSITION.toUpperCase());
		compSearchObj.put(Constants.KEYWORD, Constants.VERIFIED);
		compSearchObj.put(Constants.FIELD, Constants.STATUS);
		searchList.add(compSearchObj);

		reqBody.put(Constants.SEARCHES, searchList);

		List<String> positionNameList = new ArrayList<String>();
		List<FracCommonInfo> positionList = getMasterPositionList(positionNameList);

		Map<String, Object> fracSearchRes = outboundRequestHandlerService.fetchResultUsingPost(
				cbExtServerProperties.getFracHost() + cbExtServerProperties.getFracSearchPath(), reqBody, headers);
		List<Map<String, Object>> fracResponseList = (List<Map<String, Object>>) fracSearchRes
				.get(Constants.RESPONSE_DATA);
		if (!CollectionUtils.isEmpty(fracResponseList)) {
			for (Map<String, Object> respObj : fracResponseList) {
				if (!positionNameList.contains((String) respObj.get(Constants.NAME))) {
					positionList.add(new FracCommonInfo((String) respObj.get(Constants.ID),
							(String) respObj.get(Constants.NAME), (String) respObj.get(Constants.DESCRIPTION)));
					positionNameList.add((String) respObj.get(Constants.NAME));
				}
			}
		} else {
			Exception err = new Exception("Failed to get position info from FRAC API.");
			logger.error(err);
			try {
				logger.info("Received Response: " + (new ObjectMapper()).writeValueAsString(fracSearchRes));
			} catch (Exception e) {
			}
			throw err;
		}
		Map<String, List<FracCommonInfo>> positionMap = new HashMap<String, List<FracCommonInfo>>();
		positionMap.put(Constants.POSITIONS_CACHE_NAME, positionList);
		redisCacheMgr.putCache(Constants.POSITIONS_CACHE_NAME, positionMap);
		return positionMap;
	}

	private List<FracCommonInfo> getMasterPositionList(List<String> positionNameList) throws Exception {
		List<FracCommonInfo> positionList = new ArrayList<FracCommonInfo>();
		JsonNode jsonTree = new ObjectMapper().readTree(this.getClass().getClassLoader()
				.getResourceAsStream(cbExtServerProperties.getMasterPositionListFileName()));
		JsonNode positionsObj = jsonTree.get(Constants.POSITIONS);

		Iterator<JsonNode> positionsItr = positionsObj.elements();
		while (positionsItr.hasNext()) {
			JsonNode position = positionsItr.next();
			positionNameList.add(position.get(Constants.NAME).asText());
			positionList.add(new FracCommonInfo(position.get(Constants.ID).asText(),
					position.get(Constants.NAME).asText(), position.get(Constants.DESCRIPTION).asText()));
		}
		return positionList;
	}

	public SBApiResponse listCompetenciesByOrg(String orgId, String userToken) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.ORG_COMPETENCY_SEARCH_API);
		try {
			String userId = validateAuthTokenAndFetchUserId(userToken);
			if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
				response.getParams().setStatus(Constants.FAILED);
				response.getParams().setErrmsg(Constants.USER_ID_DOESNT_EXIST);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			}
			List<String> courseIdList = redisCacheMgr.hget(Constants.COMPETENCIES_ORG_COURSES_REDIS_KEY, cbExtServerProperties.getRedisInsightIndex(), orgId);
			if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(courseIdList)) {
				Map<String, Object> competencyMap = listCompetencyDetails(Arrays.asList(courseIdList.get(0).split(",")));
				logger.info("Initializing/Refreshing the Cache Value for Key : " + Constants.COMPETENCY_CACHE_NAME);
				if (MapUtils.isNotEmpty(competencyMap)) {
					response.setResult((Map<String, Object>) competencyMap.get(Constants.RESULT));
				} else {
					response.getParams().setStatus(Constants.FAILED);
					response.getParams().setErrmsg("Competency map is empty");
					response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				response.getParams().setStatus(Constants.FAILED);
				response.getParams().setErrmsg("Issue while fetching the competency for org: " + orgId);
				response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			logger.error("Issue while fetching the competency for org: " + orgId, e);
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg(e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	private Map<String, Object> listCompetencyDetails(List<String> identifiers) throws Exception {

		HashMap<String, Object> reqBody = new HashMap<>();
		HashMap<String, Object> req = new HashMap<>();
		req.put(Constants.FACETS, Arrays.asList(Constants.COMPETENCIES_V5 + "." + Constants.COMPETENCY_AREA,
				Constants.COMPETENCIES_V5 + "." + Constants.SEARCH_COMPETENCY_THEMES,
				Constants.COMPETENCIES_V5 + "." + Constants.SEARCH_COMPETENCY_SUB_THEMES));
		Map<String, Object> filters = new HashMap<>();
		filters.put(Constants.IDENTIFIER, identifiers);
		filters.put(Constants.STATUS, Arrays.asList(Constants.LIVE));
		req.put(Constants.FILTERS, filters);
		req.put(Constants.LIMIT, 0);
		reqBody.put(Constants.REQUEST, req);

		Map<String, Object> compositeSearchRes = outboundRequestHandlerService.fetchResultUsingPost(
				cbExtServerProperties.getSbSearchServiceHost() + cbExtServerProperties.getSbCompositeV4Search(), reqBody,
				null);

		return compositeSearchRes;
	}

	private String validateAuthTokenAndFetchUserId(String authUserToken) {
		return accessTokenValidator.fetchUserIdFromAccessToken(authUserToken);
	}
}

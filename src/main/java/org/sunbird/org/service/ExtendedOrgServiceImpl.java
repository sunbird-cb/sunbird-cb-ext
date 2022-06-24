package org.sunbird.org.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiOrgSearchRequest;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.logger.CbExtLogger;

@Service
public class ExtendedOrgServiceImpl implements ExtendedOrgService {
	private CbExtLogger log = new CbExtLogger(getClass().getName());

	@Autowired
	CassandraOperation cassandraOperation;

	@Autowired
	OutboundRequestHandlerServiceImpl outboundService;

	@Autowired
	CbExtServerProperties configProperties;

	@SuppressWarnings("unchecked")
	@Override
	public SBApiResponse createOrg(Map<String, Object> request, String userToken) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_ORG_EXT_CREATE);
		try {
			String errMsg = validateOrgRequest(request);
			if (StringUtils.isEmpty(errMsg)) {
				Map<String, Object> requestData = (Map<String, Object>) request.get(Constants.REQUEST);
				String url = configProperties.getSbUrl() + configProperties.getLmsOrgCreatePath();
				Map<String, String> headers = new HashMap<String, String>();
				headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
				headers.put(Constants.X_AUTH_TOKEN, userToken);

				Map<String, Object> apiResponse = (Map<String, Object>) outboundService.fetchResultUsingPost(url,
						request, headers);
				if (Constants.OK.equalsIgnoreCase((String) apiResponse.get(Constants.RESPONSE_CODE))) {
					Map<String, Object> result = (Map<String, Object>) apiResponse.get(Constants.RESULT);
					String orgId = (String) result.get(Constants.ORGANIZATION_ID);
					log.info(String.format("Org onboarded successfully for Name: %s, with orgId: %s",
							requestData.get(Constants.ORG_NAME), orgId));
					Map<String, Object> updateRequest = new HashMap<String, Object>() {
						private static final long serialVersionUID = 1L;
						{
							put(Constants.SB_ORG_ID, orgId);
							if (requestData.containsKey(Constants.SB_ROOT_ORG_ID)) {
								put(Constants.SB_ROOT_ORG_ID, (String) requestData.get(Constants.SB_ROOT_ORG_ID));
							}
						}
					};
					Map<String, Object> compositeKey = new HashMap<String, Object>() {
						private static final long serialVersionUID = 1L;
						{
							put(Constants.ORG_NAME, requestData.get(Constants.ORG_NAME));
							put(Constants.MAP_ID, requestData.get(Constants.MAP_ID));
						}
					};
					cassandraOperation.updateRecord(Constants.SUNBIRD_KEY_SPACE_NAME, Constants.TABLE_ORG_HIERARCHY,
							updateRequest, compositeKey);
					response.getResult().put(Constants.ORGANIZATION_ID, orgId);
					response.getResult().put(Constants.RESPONSE, Constants.SUCCESS);
				}
			} else {
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			log.error(e);
			response.getParams().setErrmsg(e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return response;
	}

	@Override
	public SBApiResponse listOrg(String parentMapId) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_ORG_LIST);

		Map<String, Object> request = new HashMap<>();
		if (StringUtils.isEmpty(parentMapId)) {
			parentMapId = Constants.SPV;
		}

		if (Constants.MINISTRY.equalsIgnoreCase(parentMapId) || Constants.STATE.equalsIgnoreCase(parentMapId)) {
			request.put(Constants.SB_ORG_TYPE, parentMapId);
		} else {
			request.put(Constants.PARENT_MAP_ID, parentMapId);
		}

		List<Map<String, Object>> existingDataList = cassandraOperation
				.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_ORG_HIERARCHY, request, null);
		if (CollectionUtils.isNotEmpty(existingDataList)) {
			Map<String, Object> responseMap = new HashMap<String, Object>();
			responseMap.put(Constants.CONTENT, existingDataList);
			responseMap.put(Constants.COUNT, existingDataList.size());
			response.put(Constants.RESPONSE, responseMap);
		} else {
			response.setResponseCode(HttpStatus.NOT_FOUND);
			response.getParams().setErrmsg("Failed to get Org Details");
		}

		return response;
	}

	@Override
	public SBApiResponse orgExtSearch(Map<String, Object> request) throws Exception {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_ORG_EXT_SEARCH);
		try {
			String errMsg = validateOrgSearchReq(request);
			if (!StringUtils.isEmpty(errMsg)) {
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			}

			Map<String, Object> requestData = (Map<String, Object>) request.get(Constants.REQUEST);
			Map<String, Object> filters = (Map<String, Object>) requestData.get(Constants.FILTERS);
			String sbRootOrgId = (String) filters.get(Constants.SB_ROOT_ORG_ID);
			Map<String, Object> searchRequest = new HashMap<String, Object>() {
				private static final long serialVersionUID = 1L;
				{
					put(Constants.SB_ROOT_ORG_ID, sbRootOrgId);
				}
			};

			List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(
					Constants.KEYSPACE_SUNBIRD, Constants.TABLE_ORG_HIERARCHY, searchRequest, null);
			if (CollectionUtils.isNotEmpty(existingDataList)) {
				List<String> orgIdList = existingDataList.stream().filter(item -> !ObjectUtils.isEmpty(item))
						.map(item -> {
							return (String) item.get(Constants.SB_ORG_ID);
						}).collect(Collectors.toList());
				SBApiOrgSearchRequest orgSearchRequest = new SBApiOrgSearchRequest();
				orgSearchRequest.getFilters().setId(orgIdList);

				Map<String, Object> orgSearchRequestBody = new HashMap<String, Object>() {
					private static final long serialVersionUID = 1L;

					{
						put(Constants.REQUEST, orgSearchRequest);
					}
				};
				Map<String, String> headers = new HashMap<String, String>();
				headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
				String url = configProperties.getSbUrl() + configProperties.getSbOrgSearchPath();

				Map<String, Object> apiResponse = (Map<String, Object>) outboundService.fetchResultUsingPost(url,
						orgSearchRequestBody, headers);
				if (Constants.OK.equalsIgnoreCase((String) apiResponse.get(Constants.RESPONSE_CODE))) {
					Map<String, Object> apiResponseResult = (Map<String, Object>) apiResponse.get(Constants.RESULT);
					response.put(Constants.RESPONSE, apiResponseResult.get(Constants.RESPONSE));
				} else {
					response.getParams().setErrmsg("Failed to search org details");
					response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				Map<String, Object> responseMap = new HashMap<String, Object>();
				responseMap.put(Constants.COUNT, 0);
				responseMap.put(Constants.CONTENT, Collections.EMPTY_LIST);
				response.put(Constants.RESPONSE, responseMap);
			}
		} catch (Exception e) {
			log.error(e);
			response.getParams().setErrmsg(e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	private String validateOrgRequest(Map<String, Object> requestData) {
		List<String> params = new ArrayList<String>();
		StringBuilder strBuilder = new StringBuilder();
		Map<String, Object> request = (Map<String, Object>) requestData.get(Constants.REQUEST);
		if (ObjectUtils.isEmpty(request)) {
			strBuilder.append("Request object is empty.");
			return strBuilder.toString();
		}

		if (StringUtils.isEmpty((String) request.get(Constants.ORG_NAME))) {
			params.add(Constants.ORG_NAME);
		}

		String orgType = (String) request.get(Constants.ORGANIZATION_TYPE);
		if (StringUtils.isEmpty(orgType)) {
			params.add(Constants.ORGANIZATION_TYPE);
		} else {
			// We do have OrgType Value
			if (!Constants.MINISTRY.equalsIgnoreCase(orgType) && !Constants.STATE.equalsIgnoreCase(orgType)) {
				// orgType is not ministry or state - in this case sbOrgRootId parameter is
				// required
				params.add(Constants.SB_ROOT_ORG_ID);
			}
		}

		if (StringUtils.isEmpty((String) request.get(Constants.ORGANIZATION_SUB_TYPE))) {
			params.add(Constants.ORGANIZATION_SUB_TYPE);
		}

		if (StringUtils.isEmpty((String) request.get(Constants.MAP_ID))) {
			params.add(Constants.MAP_ID);
		}

		if (ObjectUtils.isEmpty(request.get(Constants.IS_TENANT))) {
			params.add(Constants.IS_TENANT);
		}

		if (StringUtils.isEmpty((String) request.get(Constants.CHANNEL))) {
			params.add(Constants.CHANNEL);
		}

		if (!params.isEmpty()) {
			strBuilder.append("Invalid Request. Missing params - " + params);
		}

		return strBuilder.toString();
	}

	private String validateOrgSearchReq(Map<String, Object> requestData) {
		List<String> params = new ArrayList<String>();
		StringBuilder strBuilder = new StringBuilder();

		Map<String, Object> request = (Map<String, Object>) requestData.get(Constants.REQUEST);
		Map<String, Object> filters = (Map<String, Object>) request.get(Constants.FILTERS);
		if (ObjectUtils.isEmpty(filters)) {
			strBuilder.append("Filters in Request object is empty.");
			return strBuilder.toString();
		}

		if (StringUtils.isEmpty((String) filters.get(Constants.SB_ROOT_ORG_ID))) {
			params.add(Constants.SB_ROOT_ORG_ID);
		}

		if (!params.isEmpty()) {
			strBuilder.append("Invalid filters in Request. Missing params - " + params);
		}

		return strBuilder.toString();

	}
}

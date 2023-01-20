package org.sunbird.org.service;

import java.util.ArrayList;
import java.util.Arrays;
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
			if (!StringUtils.isEmpty(errMsg)) {
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			}

			Map<String, Object> requestData = (Map<String, Object>) request.get(Constants.REQUEST);

			String orgId = checkOrgExist((String) requestData.get(Constants.CHANNEL), userToken);

			if (StringUtils.isEmpty(orgId)) {
				orgId = createOrgInSunbird(request, (String) requestData.get(Constants.CHANNEL), userToken);
			}

			if (!StringUtils.isEmpty(orgId)) {
				Map<String, Object> updateRequest = new HashMap<String, Object>();
				updateRequest.put(Constants.SB_ORG_ID.toLowerCase(), orgId);
				String orgType = (String) requestData.get(Constants.ORGANIZATION_TYPE);
				String orgName = (String) requestData.get(Constants.ORG_NAME);
				updateRequest.put(Constants.SB_ORG_ID, orgId);
				updateRequest.put(Constants.ORG_NAME, orgName);
				updateRequest.put(Constants.SB_ORG_TYPE, orgType);
				String mapId = (String) requestData.get(Constants.MAP_ID);
				String orgCode = (String) requestData.get(Constants.ORG_CODE);
				String sbRootOrgid = (String) requestData.get(Constants.SB_ROOT_ORG_ID);
				updateRequest.put(Constants.SB_SUB_ORG_TYPE, requestData.get(Constants.ORGANIZATION_SUB_TYPE));
				if (!StringUtils.isEmpty(mapId)) {
					updateRequest.put(Constants.MAP_ID, mapId);
				} else {
					mapId = createMapId(requestData);
					updateRequest.put(Constants.MAP_ID, mapId);
					updateRequest.put(Constants.ORG_CODE, mapId);
				}
				if (!StringUtils.isEmpty(orgCode)) {
					updateRequest.put(Constants.ORG_CODE, orgCode);
				}
				if (!Constants.STATE.equalsIgnoreCase(orgType) && !Constants.MINISTRY.equalsIgnoreCase(orgType)) {
					updateRequest.put(Constants.PARENT_MAP_ID, requestData.get(Constants.PARENT_MAP_ID));
					if (!StringUtils.isEmpty(sbRootOrgid)) {
						updateRequest.put(Constants.SB_ROOT_ORG_ID, sbRootOrgid);
					} else {
						updateRequest.put(Constants.SB_ROOT_ORG_ID, fetchRootOrgId(requestData));
					}
				} else {
					updateRequest.put(Constants.PARENT_MAP_ID, Constants.SPV);
				}
				if (!StringUtils.isEmpty((String) requestData.get(Constants.MAP_ID))) {
					Map<String, Object> compositeKey = new HashMap<String, Object>() {
						private static final long serialVersionUID = 1L;
						{
							put(Constants.ORG_NAME, orgName);
							put(Constants.MAP_ID, requestData.get(Constants.MAP_ID));
						}
					};
					cassandraOperation.updateRecord(Constants.SUNBIRD_KEY_SPACE_NAME, Constants.TABLE_ORG_HIERARCHY,
							updateRequest, compositeKey);
				} else {
					cassandraOperation.insertRecord(Constants.SUNBIRD_KEY_SPACE_NAME, Constants.TABLE_ORG_HIERARCHY,
							updateRequest);
				}
				response.getResult().put(Constants.ORGANIZATION_ID, orgId);
				response.getResult().put(Constants.RESPONSE, Constants.SUCCESS);
			} else {
				response.getParams().setErrmsg("Failed to create organisation in Sunbird.");
				response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
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
							return (String) item.get(Constants.SB_ORG_ID.toLowerCase());
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
		String orgType = (String) requestData.get(Constants.ORGANIZATION_TYPE);
		Map<String, Object> request = (Map<String, Object>) requestData.get(Constants.REQUEST);
		if (ObjectUtils.isEmpty(request)) {
			strBuilder.append("Request object is empty.");
			return strBuilder.toString();
		}

		if (StringUtils.isEmpty((String) request.get(Constants.ORG_NAME))) {
			params.add(Constants.ORG_NAME);
		}

		if (StringUtils.isEmpty(((String) orgType))) {
			params.add(Constants.ORGANIZATION_TYPE);
		} else if (!Constants.STATE.equalsIgnoreCase(orgType) && !Constants.MINISTRY.equalsIgnoreCase(orgType)) {
			if (StringUtils.isEmpty((String) request.get(Constants.PARENT_MAP_ID))) {
				params.add(Constants.PARENT_MAP_ID);
			}
		}

		if (StringUtils.isEmpty((String) request.get(Constants.ORGANIZATION_SUB_TYPE))) {
			params.add(Constants.ORGANIZATION_SUB_TYPE);
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

	private String checkOrgExist(String channel, String userToken) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
		if (StringUtils.isNotEmpty(userToken)) {
			headers.put(Constants.X_AUTH_TOKEN, userToken);
		}
		Map<String, Object> filterMap = new HashMap<String, Object>() {
			private static final long serialVersionUID = 1L;
			{
				put(Constants.CHANNEL, channel);
			}
		};
		Map<String, Object> searchRequest = new HashMap<String, Object>() {
			private static final long serialVersionUID = 1L;
			{
				put(Constants.FILTERS, filterMap);
				put(Constants.FIELDS, Arrays.asList(Constants.CHANNEL, Constants.IDENTIFIER));
			}
		};
		Map<String, Object> searchRequestBody = new HashMap<String, Object>() {
			private static final long serialVersionUID = 1L;
			{
				put(Constants.REQUEST, searchRequest);
			}
		};
		String url = configProperties.getSbUrl() + configProperties.getSbOrgSearchPath();
		Map<String, Object> apiResponse = (Map<String, Object>) outboundService.fetchResultUsingPost(url,
				searchRequestBody, headers);
		if (Constants.OK.equalsIgnoreCase((String) apiResponse.get(Constants.RESPONSE_CODE))) {
			Map<String, Object> result = (Map<String, Object>) apiResponse.get(Constants.RESULT);
			Map<String, Object> searchResponse = (Map<String, Object>) result.get(Constants.RESPONSE);
			int count = (int) searchResponse.get(Constants.COUNT);
			if (count > 0) {
				// The org is already exist - need to update the org details in org_hierarchy
				// table
				List<Map<String, Object>> orgList = (List<Map<String, Object>>) searchResponse.get(Constants.CONTENT);
				Map<String, Object> existingOrg = orgList.get(0);
				return (String) existingOrg.get(Constants.IDENTIFIER);
			}
		}
		return StringUtils.EMPTY;
	}

	private String createOrgInSunbird(Map<String, Object> request, String channel, String userToken) {
		String url = configProperties.getSbUrl() + configProperties.getLmsOrgCreatePath();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
		if (StringUtils.isNotEmpty(userToken)) {
			headers.put(Constants.X_AUTH_TOKEN, userToken);
		}

		Map<String, Object> apiResponse = (Map<String, Object>) outboundService.fetchResultUsingPost(url, request,
				headers);
		if (Constants.OK.equalsIgnoreCase((String) apiResponse.get(Constants.RESPONSE_CODE))) {
			Map<String, Object> result = (Map<String, Object>) apiResponse.get(Constants.RESULT);
			log.info(String.format("Org onboarded successfully for Name: %s, with orgId: %s", channel,
					result.get(Constants.ORGANIZATION_ID)));
			return (String) result.get(Constants.ORGANIZATION_ID);
		}
		return StringUtils.EMPTY;
	}

	private String findRootOrgId(String orgName, String mapId) {
		String parentMapId = StringUtils.EMPTY;
		// We are going to search only 3 times
		for (int i = 0; i < 3; i++) {
			Map<String, Object> searchRequest = new HashMap<String, Object>();
			if (StringUtils.isNotEmpty(orgName)) {
				searchRequest.put(Constants.ORG_NAME, orgName);
			}
			searchRequest.put(Constants.MAP_ID, mapId);

			List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(
					Constants.KEYSPACE_SUNBIRD, Constants.TABLE_ORG_HIERARCHY, searchRequest, null);
			if (CollectionUtils.isNotEmpty(existingDataList)) {
				Map<String, Object> data = existingDataList.get(0);
				parentMapId = (String) data.get(Constants.PARENT_MAP_ID.toLowerCase());
				// We found the 1st level object
				if (Constants.SPV.equalsIgnoreCase(parentMapId)) {
					return (String) data.get(Constants.SB_ORG_ID.toLowerCase());
				} else {
					mapId = (String) data.get(Constants.PARENT_MAP_ID.toLowerCase());
					orgName = StringUtils.EMPTY;
					continue;
				}
			} else {
				break;
			}
		}

		return StringUtils.EMPTY;
	}

	private String createMapId(Map<String, Object> requestData) {
		Map<String, Object> queryRequest = new HashMap<>();
		String prefix = StringUtils.EMPTY;
		String mapIdNew = StringUtils.EMPTY;
		String orgType = (String) requestData.get(Constants.ORGANIZATION_TYPE);
		if (!Constants.STATE.equalsIgnoreCase(orgType) && !Constants.MINISTRY.equalsIgnoreCase(orgType)) {
			queryRequest.put(Constants.PARENT_MAP_ID, requestData.get(Constants.PARENT_MAP_ID));
			if (Constants.MDO.equalsIgnoreCase(orgType)) {
				prefix = "D_";
			} else if (Constants.ORG.equalsIgnoreCase(orgType)) {
				prefix = "O_";
			} else {
				prefix = "X_";
			}
		} else {
			queryRequest.put(Constants.SB_ORG_TYPE, orgType);
			if (Constants.STATE.equalsIgnoreCase(orgType)) {
				prefix = "S_";
			} else if (Constants.MINISTRY.equalsIgnoreCase(orgType)) {
				prefix = "M_";
			}
		}
		List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(
				Constants.KEYSPACE_SUNBIRD, Constants.TABLE_ORG_HIERARCHY, queryRequest,
				Arrays.asList(Constants.MAP_ID));
		if (CollectionUtils.isNotEmpty(existingDataList)) {
			List<String> mapIdList = new ArrayList<>();
			for (Map<String, Object> map : existingDataList) {
				if (((String) map.get(Constants.MAP_ID)).startsWith(prefix))
					mapIdList.add((String) map.get(Constants.MAP_ID));
			}
			mapIdNew = prefix + (mapIdList.size() + 1);
		} else {
			mapIdNew = prefix + "1";
		}
		return mapIdNew;
	}

	private String fetchRootOrgId(Map<String, Object> requestData) {
		String sbOrgId = null;
		Map<String, Object> queryRequest = new HashMap<>();
		queryRequest.put(Constants.MAP_ID, requestData.get(Constants.PARENT_MAP_ID));
		List<String> fields = new ArrayList<>();
		fields.add(Constants.SB_ORG_ID);
		List<Map<String, Object>> sbRootOrgList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
				Constants.TABLE_ORG_HIERARCHY, queryRequest, fields);
		if (CollectionUtils.isNotEmpty(sbRootOrgList)) {
			Map<String, Object> data = sbRootOrgList.get(0);
			sbOrgId = (String) data.get(Constants.SB_ORG_ID);
		}
		return sbOrgId;
	}
}

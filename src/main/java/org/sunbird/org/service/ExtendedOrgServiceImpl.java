package org.sunbird.org.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.common.model.SBApiOrgSearchRequest;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.org.model.OrgHierarchy;
import org.sunbird.org.model.OrgHierarchyInfo;
import org.sunbird.org.repository.OrgHierarchyRepository;

@Service
public class ExtendedOrgServiceImpl implements ExtendedOrgService {
	private Logger logger = LoggerFactory.getLogger(getClass().getName());

	@Autowired
	OutboundRequestHandlerServiceImpl outboundService;

	@Autowired
	CbExtServerProperties configProperties;

	@Autowired
	OrgHierarchyRepository orgRepository;

	ObjectMapper objectMapper = new ObjectMapper();

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
			String orgType = (String) requestData.get(Constants.ORGANIZATION_TYPE);
			String channelName = null;
			boolean dbUpdateRequired = false;
			boolean orgCreatedWithNewChannel = false;

			if (StringUtils.isEmpty(orgId)) {
				// There is no org exist for given Channel. We can simply create the same in
				// system.
				orgId = createOrgInSunbird(request, (String) requestData.get(Constants.CHANNEL), userToken);
				if (StringUtils.isBlank(orgId)) {
					response.getParams().setErrmsg(Constants.FAILED_CREATING_ORG_IN_SUNBIRD);
					response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
					return response;
				}
				dbUpdateRequired = true;
			} else {
				// The channel already exist. We need to check OrgHierarchy table for duplicate.
				if (Constants.STATE.equalsIgnoreCase(orgType)
						|| Constants.MINISTRY.equalsIgnoreCase(orgType)) {
					// We are not allowing duplicates @ L1 -- Need to throw error
					response.getParams().setErrmsg("Organisation is already exist.");
					response.setResponseCode(HttpStatus.BAD_REQUEST);
					return response;
				} else {
					OrgHierarchy existingDBRecord = orgRepository.findByOrgNameAndParentMapId(
							(String) requestData.get(Constants.CHANNEL),
							(String) requestData.get(Constants.PARENT_MAP_ID));
					if (existingDBRecord == null) {
						channelName = prepareChannelName((String) requestData.get(Constants.PARENT_MAP_ID),
								requestData);
						channelName = channelName + (String) requestData.get(Constants.CHANNEL);
						requestData.put(Constants.CHANNEL, channelName);
						orgId = createOrgInSunbird(request, (String) requestData.get(Constants.CHANNEL), userToken);
						if (StringUtils.isBlank(orgId)) {
							response.getParams().setErrmsg(Constants.FAILED_CREATING_ORG_IN_SUNBIRD);
							response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
							return response;
						}
						dbUpdateRequired = true;
						orgCreatedWithNewChannel = true;
					} else if (StringUtils.isBlank(existingDBRecord.getSbOrgId())) {
						existingDBRecord.setSbOrgId(orgId);
						if (StringUtils.isEmpty(existingDBRecord.getSbRootOrgId())) {
							existingDBRecord
									.setSbRootOrgId(fetchRootOrgId((String) requestData.get(Constants.PARENT_MAP_ID)));
						}
						orgRepository.save(existingDBRecord);
					} else if (existingDBRecord.getSbOrgId().equalsIgnoreCase(orgId)) {
						response.getParams().setErrmsg("Duplicate Record Found in OrgHierarchy. Contact Admin");
						response.setResponseCode(HttpStatus.BAD_REQUEST);
						return response;
					}
				}
			}

			if (dbUpdateRequired) {
				OrgHierarchy existingDBRecord = orgRepository.findByOrgNameAndParentMapId(
						(String) requestData.get(Constants.CHANNEL), (String) requestData.get(Constants.PARENT_MAP_ID));
				if (existingDBRecord != null) {
					existingDBRecord.setSbOrgId(orgId);
					if (StringUtils.isEmpty(existingDBRecord.getSbRootOrgId())) {
						existingDBRecord
								.setSbRootOrgId(fetchRootOrgId((String) requestData.get(Constants.PARENT_MAP_ID)));
					}
					orgRepository.save(existingDBRecord);
				} else {
					// We just created with given channel name. but this is new record in
					// orgHierarchy...
					// By calling prepareChannelName we will update L1 and L2 details.
					prepareChannelName((String) requestData.get(Constants.PARENT_MAP_ID), requestData);
					channelName = (String) requestData.get(Constants.CHANNEL);
					orgCreatedWithNewChannel = true;
				}
			}

			if (orgCreatedWithNewChannel) {
				Map<String, Object> updateRequest = new HashMap<String, Object>();
				String orgName = (String) requestData.get(Constants.ORG_NAME);
				updateRequest.put(Constants.CHANNEL, (String) requestData.get(Constants.CHANNEL));
				updateRequest.put(Constants.SB_ORG_ID, orgId);
				updateRequest.put(Constants.ORG_NAME, orgName);
				updateRequest.put(Constants.SB_ORG_TYPE, orgType);
				updateRequest.put(Constants.L1_MAP_ID, (String) requestData.get(Constants.L1_MAP_ID));
				updateRequest.put(Constants.L2_MAP_ID, (String) requestData.get(Constants.L2_MAP_ID));
				updateRequest.put(Constants.L1_ORG_NAME, (String) requestData.get(Constants.L1_ORG_NAME));
				updateRequest.put(Constants.L2_ORG_NAME, (String) requestData.get(Constants.L2_ORG_NAME));

				String mapId = (String) requestData.get(Constants.MAP_ID);
				if (StringUtils.isEmpty(mapId)) {
					// There is a possibility that this Org already exists in table. Get the MapId
					// if so.
					fetchMapIdFromDB(requestData);
					mapId = (String) requestData.get(Constants.MAP_ID);
				}
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
						updateRequest.put(Constants.SB_ROOT_ORG_ID,
								fetchRootOrgId((String) requestData.get(Constants.PARENT_MAP_ID)));
					}
				} else {
					updateRequest.put(Constants.PARENT_MAP_ID, Constants.SPV);
				}
				if (!StringUtils.isEmpty((String) requestData.get(Constants.MAP_ID))) {
					ObjectMapper om = new ObjectMapper();
					logger.info("Need to update the record here... " + om.writeValueAsString(updateRequest));
					if (ObjectUtils.isEmpty(updateRequest.get(Constants.SB_ROOT_ORG_ID))) {
						orgRepository.updateOrgIdForChannel(channelName,
								(String) updateRequest.get(Constants.SB_ORG_ID));
					} else {
						orgRepository.updateSbOrgIdAndSbOrgRootIdForChannel(channelName,
								(String) updateRequest.get(Constants.SB_ORG_ID),
								(String) updateRequest.get(Constants.SB_ROOT_ORG_ID));
					}
				} else {
					OrgHierarchy newOrg = new OrgHierarchy(orgName, channelName, mapId,
							(String) updateRequest.get(Constants.PARENT_MAP_ID));
					orgRepository.save(getOrgRecord(updateRequest, newOrg));
				}
				response.getResult().put(Constants.ORGANIZATION_ID, orgId);
				response.getResult().put(Constants.RESPONSE, Constants.SUCCESS);
			}
		} catch (Exception e) {
			logger.error("Failed to create user. Exception: ", e);
			response.getParams().setErrmsg(e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return response;
	}

	@Override
	public SBApiResponse listOrg(String parentMapId) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_ORG_LIST);
		if (StringUtils.isEmpty(parentMapId)) {
			parentMapId = Constants.SPV;
		}

		List<OrgHierarchy> orgHierarchyList = null;
		if (Constants.MINISTRY.equalsIgnoreCase(parentMapId)
				|| Constants.STATE.equalsIgnoreCase(parentMapId)) {
			orgHierarchyList = orgRepository.findAllBySbOrgType(parentMapId);
		} else {
			orgHierarchyList = orgRepository.findAllByParentMapId(parentMapId);
		}
		if (CollectionUtils.isNotEmpty(orgHierarchyList)) {
			Map<String, Object> responseMap = new HashMap<String, Object>();
			responseMap.put(Constants.CONTENT, orgHierarchyList);
			responseMap.put(Constants.COUNT, orgHierarchyList.size());
			response.put(Constants.RESPONSE, responseMap);
		} else {
			Map<String, Object> responseMap = new HashMap<>();
			responseMap.put(Constants.CONTENT, orgHierarchyList);
			responseMap.put(Constants.COUNT, orgHierarchyList.size());
			response.put(Constants.RESPONSE, responseMap);
			response.getParams().setErrmsg("No child org found for Id: " + parentMapId);
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

			// sbRootOrgId is State Id. Let's get all the children (i.e. departments)
			List<String> orgIdList = orgRepository.findAllBySbRootOrgId(sbRootOrgId);

			if (CollectionUtils.isNotEmpty(orgIdList)) {
				List<String> orgIdChildList = orgRepository.fetchL2LevelOrgList(orgIdList);
				if (CollectionUtils.isNotEmpty(orgIdChildList)) {
					orgIdList.addAll(orgIdChildList);
				}
				SBApiOrgSearchRequest orgSearchRequest = new SBApiOrgSearchRequest();
				orgSearchRequest.getFilters().setId(orgIdList);
				if (!ProjectUtil.isStringNullOREmpty((String) requestData.get(Constants.QUERY))) {
					orgSearchRequest.setQuery((String) requestData.get(Constants.QUERY));
				}
				orgSearchRequest.setSortBy((Map<String, String>) requestData.get(Constants.SORT_BY_KEYWORD));
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
			logger.error("Failed to search org details. Exception: ", e);
			response.getParams().setErrmsg(e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	private String validateOrgRequest(Map<String, Object> request) {
		List<String> params = new ArrayList<String>();
		StringBuilder strBuilder = new StringBuilder();
		Map<String, Object> requestData = (Map<String, Object>) request.get(Constants.REQUEST);
		if (ObjectUtils.isEmpty(requestData)) {
			strBuilder.append("Request object is empty.");
			return strBuilder.toString();
		}

		if (StringUtils.isEmpty((String) requestData.get(Constants.ORG_NAME))) {
			params.add(Constants.ORG_NAME);
		}

		String orgType = (String) requestData.get(Constants.ORGANIZATION_TYPE);
		if (StringUtils.isEmpty(((String) orgType))) {
			params.add(Constants.ORGANIZATION_TYPE);
		} else if (!Constants.STATE.equalsIgnoreCase(orgType) && !Constants.MINISTRY.equalsIgnoreCase(orgType)) {
			if (StringUtils.isEmpty((String) requestData.get(Constants.PARENT_MAP_ID))) {
				params.add(Constants.PARENT_MAP_ID);
			}
		}

		if (StringUtils.isEmpty((String) requestData.get(Constants.ORGANIZATION_SUB_TYPE))) {
			params.add(Constants.ORGANIZATION_SUB_TYPE);
		}

		if (ObjectUtils.isEmpty(requestData.get(Constants.IS_TENANT))) {
			params.add(Constants.IS_TENANT);
		}

		if (StringUtils.isEmpty((String) requestData.get(Constants.CHANNEL))) {
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
			logger.info(String.format("Org onboarded successfully for Name: %s, with orgId: %s", channel,
					result.get(Constants.ORGANIZATION_ID)));
			return (String) result.get(Constants.ORGANIZATION_ID);
		}
		return StringUtils.EMPTY;
	}

	public Map<String, Object> getOrgDetails(List<String> orgIds, List<String> fields) {
		Map<String, Object> filters = new HashMap<>();
		filters.put(Constants.IDENTIFIER, orgIds);
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put(Constants.FILTERS, filters);
		requestBody.put(Constants.FIELDS, fields);
		Map<String, Object> request = new HashMap<>();
		request.put(Constants.REQUEST, requestBody);
		Map<String, String> headers = new HashMap<>();
		headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
		Map<String, Object> apiResponse = (Map<String, Object>) outboundService.fetchResultUsingPost(
				configProperties.getSbUrl() + configProperties.getSbOrgSearchPath(), request, headers);
		Map<String, Object> orgMap = new HashMap<>();
		if (Constants.OK.equalsIgnoreCase((String) apiResponse.get(Constants.RESPONSE_CODE))) {
			Map<String, Object> result = (Map<String, Object>) apiResponse.get(Constants.RESULT);
			if (MapUtils.isNotEmpty(result)) {
				Map<String, Object> response = (Map<String, Object>) result.get(Constants.RESPONSE);
				if (MapUtils.isNotEmpty(response)) {
					for (int i = 0; i < orgIds.size(); i++) {
						orgMap.put((String) response.get(orgIds.get(i)), response.get(Constants.CONTENT));
					}
				}
			}
		}
		return orgMap;
	}

	public void getOrgDetailsFromDB(List<String> orgIds, Map<String, String> orgInfoMap) {
		// This method is called from report tool.
		// Not doing anything for now.
	}

	public SBApiResponse createOrgForUserRegistration(Map<String, Object> request) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_ORG_EXT_CREATE);
		try {
			String errMsg = validateOrgRequestForRegistration(request);
			if (!StringUtils.isEmpty(errMsg)) {
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			}

			Map<String, Object> requestData = (Map<String, Object>) request.get(Constants.REQUEST);

			boolean dbUpdateRequired = false;
			String orgId = checkOrgExist((String) requestData.get(Constants.CHANNEL), StringUtils.EMPTY);

			if (StringUtils.isEmpty(orgId)) {
				orgId = createOrgInSunbird(request, (String) requestData.get(Constants.CHANNEL), StringUtils.EMPTY);
				dbUpdateRequired = true;
			}

			if (!StringUtils.isEmpty(orgId)) {
				if (dbUpdateRequired) {
					OrgHierarchy existingDBRecord = orgRepository
							.findByChannel((String) requestData.get(Constants.CHANNEL));
					if (StringUtils.isBlank(existingDBRecord.getSbOrgId())) {
						existingDBRecord.setSbOrgId(orgId);
						if (StringUtils.isEmpty(existingDBRecord.getSbRootOrgId())) {
							existingDBRecord.setSbRootOrgId(fetchRootOrgId(existingDBRecord.getParentMapId()));
						}
						orgRepository.save(existingDBRecord);
					} else {
						logger.error(String.format(
								"Failed to update rootOrg details. RootOrg is already available in DB record. Existing: %s, NewValue: %s",
								existingDBRecord.getSbOrgId(), orgId));
					}
				}
				response.getResult().put(Constants.ORGANIZATION_ID, orgId);
				response.getResult().put(Constants.RESPONSE, Constants.SUCCESS);
			} else {
				response.getParams().setErrmsg(Constants.FAILED_CREATING_ORG_IN_SUNBIRD);
				response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			logger.error("Failed to create org for user registration. Exception: ", e);
			response.getParams().setErrmsg(e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return response;
	}

	private String createMapId(Map<String, Object> requestData) {
		List<OrgHierarchy> existingOrgList = null;
		String prefix = StringUtils.EMPTY;
		String mapIdNew;
		String orgType = (String) requestData.get(Constants.ORGANIZATION_TYPE);
		if (!Constants.STATE.equalsIgnoreCase(orgType) && !Constants.MINISTRY.equalsIgnoreCase(orgType)) {
			String orgSubType = (String) requestData.get(Constants.ORGANIZATION_SUB_TYPE);
			String parentMapId = (String) requestData.get(Constants.PARENT_MAP_ID);
			existingOrgList = orgRepository.findAllByParentMapId(parentMapId);
			if (Constants.DEPARTMENT.equalsIgnoreCase(orgSubType)) {
				prefix = "D_";
			} else if (Constants.BOARD.equalsIgnoreCase(orgSubType)) {
				prefix = "O_";
			} else if (Constants.TRAINING_INSTITUTE.equalsIgnoreCase(orgSubType)) {
				prefix = "T_";
			} else {
				prefix = "X_";
			}
			prefix = parentMapId + "_" + prefix;
		} else {
			existingOrgList = orgRepository.findAllBySbOrgType(orgType);
			if (Constants.STATE.equalsIgnoreCase(orgType)) {
				prefix = "S_";
			} else if (Constants.MINISTRY.equalsIgnoreCase(orgType)) {
				prefix = "M_";
			}
		}

		if (CollectionUtils.isNotEmpty(existingOrgList)) {
			List<String> mapIdList = new ArrayList<>();
			for (OrgHierarchy org : existingOrgList) {
				if (org.getMapId().startsWith(prefix)) {
					mapIdList.add(org.getMapId());
				}
			}
			mapIdNew = prefix + (mapIdList.size() + 1);
		} else {
			mapIdNew = prefix + "1";
		}
		return mapIdNew;
	}

	private String fetchRootOrgId(String mapId) {
		OrgHierarchy parentOrg = orgRepository.findByMapId(mapId);
		if (parentOrg != null && StringUtils.isBlank(parentOrg.getSbOrgId())) {
			// Let's try to create parent org
			createParentOrg(parentOrg);
			return parentOrg.getSbOrgId();
		}
		return StringUtils.EMPTY;
	}

	private void fetchMapIdFromDB(Map<String, Object> requestData) {
		List<OrgHierarchy> orgList = orgRepository.findAllByOrgName((String) requestData.get(Constants.ORG_NAME));
		if (ObjectUtils.isEmpty(orgList) || orgList.size() > 1) {
			// There are no args or multiple orgs. return from here.
			return;
		} else {
			// There is one org exist with the given name.
			// Otherwise this new dept name which already exist in someother ministry /
			// state / department.
			if (ObjectUtils.isEmpty(requestData.get(Constants.PARENT_MAP_ID))) {
				// ParentMapId is empty -- we are trying to create dept / state with same name.
				// Return simply.
				return;
			} else {
				OrgHierarchy existingOrg = orgList.get(0);
				// Check given parentMapId is same as existing record parentMapId.
				if (existingOrg.getParentMapId().equalsIgnoreCase((String) requestData.get(Constants.PARENT_MAP_ID))) {
					requestData.put(Constants.MAP_ID, existingOrg.getMapId());
				}
			}
		}
	}

	private String validateOrgRequestForRegistration(Map<String, Object> request) {
		List<String> params = new ArrayList<String>();
		StringBuilder strBuilder = new StringBuilder();
		Map<String, Object> requestData = (Map<String, Object>) request.get(Constants.REQUEST);
		if (ObjectUtils.isEmpty(requestData)) {
			strBuilder.append("Request object is empty.");
			return strBuilder.toString();
		}

		if (StringUtils.isBlank((String) requestData.get(Constants.CHANNEL))) {
			params.add(Constants.CHANNEL);
		}

		if (StringUtils.isEmpty((String) requestData.get(Constants.ORG_NAME))) {
			params.add(Constants.ORG_NAME);
		}

		if (StringUtils.isEmpty((String) requestData.get(Constants.MAP_ID))) {
			params.add(Constants.MAP_ID);
		}

		if (StringUtils.isEmpty((String) requestData.get(Constants.ORGANIZATION_TYPE))) {
			params.add(Constants.ORGANIZATION_TYPE);
		}

		if (StringUtils.isEmpty((String) requestData.get(Constants.ORGANIZATION_SUB_TYPE))) {
			params.add(Constants.ORGANIZATION_SUB_TYPE);
		}

		if (!params.isEmpty()) {
			strBuilder.append("Invalid Request. Missing params - " + params);
		}

		return strBuilder.toString();
	}

	public SBApiResponse orgExtSearchV2(Map<String, Object> request) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_ORG_HIERACHY_SEARCH);
		try {
			Map<String, Object> searchFilters = new HashMap<String, Object>();
			String errMsg = validateSearchRequest(request, searchFilters);
			if (StringUtils.isNotBlank(errMsg)) {
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				response.getParams().setErrmsg(errMsg);
				return response;
			}
			List<OrgHierarchyInfo> orgInfoList = new ArrayList<OrgHierarchyInfo>();
			List<OrgHierarchy> orgList;
			if (searchFilters.containsKey(Constants.IDENTIFIER)) {
				orgList = orgRepository.findAllBySbOrgId((List<String>) searchFilters.get(Constants.IDENTIFIER));
			} else {
				orgList = orgRepository
						.searchOrgWithHierarchy((String) searchFilters.get(Constants.ORG_NAME));
			}
			if (CollectionUtils.isEmpty(orgList)) {
				orgList = Collections.emptyList();
			} else if (StringUtils.isBlank((String) searchFilters.get(Constants.PARENT_TYPE))) {
				for (OrgHierarchy org : orgList) {
					orgInfoList.add(org.toOrgInfo());
				}
			} else {
				Set<String> l1MapIdSet = orgList.stream().map(OrgHierarchy::getL1MapId)
						.filter(l1MapId -> Objects.nonNull(l1MapId)).collect(Collectors.toSet());

				List<OrgHierarchy> parentList = orgRepository.searchOrgForL1MapId(l1MapIdSet);
				Map<String, OrgHierarchy> parentListMap = parentList.stream()
						.collect(Collectors.toMap(OrgHierarchy::getMapId, orgHierarchy -> orgHierarchy));
				String parentType = (String) searchFilters.get(Constants.PARENT_TYPE);
				for (OrgHierarchy org : orgList) {
					OrgHierarchy parentObj = parentListMap.get(org.getL1MapId());
					if (parentObj != null) {
						// We found the parent for this orgObj.. check this parent's sbOrgType is given
						// parentType
						if (parentType.equalsIgnoreCase(parentObj.getSbOrgType())) {
							orgInfoList.add(org.toOrgInfo());
						}
					} else {
						// If Org doesn't have l1MapId then it could be State / Ministry
						if (parentType.equalsIgnoreCase(org.getSbOrgType())) {
							orgInfoList.add(org.toOrgInfo());
						}
					}
				}
			}
			int limit = (Integer) searchFilters.get(Constants.LIMIT);
			if (orgInfoList.size() > limit) {
				orgInfoList.subList(limit, orgInfoList.size()).clear();
			}

			response.getResult().put(Constants.COUNT, orgInfoList.size());
			response.getResult().put(Constants.RESPONSE, orgInfoList);
		} catch (Exception e) {
			logger.error("Failed to retrieve details from org hierarchy table. Exception: ", e);
		}
		return response;
	}

	private String validateSearchRequest(Map<String, Object> request, Map<String, Object> searchFilters) {
		String errMsg = "";
		Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
		if (ObjectUtils.isEmpty(requestBody)) {
			errMsg = Constants.INVALID_REQUEST;
			return errMsg;
		}
		Map<String, Object> filters = (Map<String, Object>) requestBody.get(Constants.FILTERS);
		if (ObjectUtils.isEmpty(filters)) {
			errMsg = Constants.INVALID_REQUEST;
			return errMsg;
		}

		boolean filterExist = false;
		if (filters.containsKey(Constants.IDENTIFIER)) {
			filterExist = true;
			List<String> orgList = (List<String>) filters.get(Constants.IDENTIFIER);
			if (CollectionUtils.isNotEmpty(orgList)) {
				searchFilters.put(Constants.IDENTIFIER, orgList);
			} else {
				errMsg = "Identifier list is empty";
			}
		}

		if (filters.containsKey(Constants.ORG_NAME) && filters.containsKey(Constants.PARENT_TYPE)) {
			filterExist = true;
			if (StringUtils.isNotBlank((String) filters.get(Constants.ORG_NAME))) {
				searchFilters.put(Constants.ORG_NAME, ((String) filters.get(Constants.ORG_NAME)).trim());
			} else {
				errMsg = "OrgName is empty in search request.";
			}

			if (StringUtils.isNotBlank((String) filters.get(Constants.PARENT_TYPE))) {
				searchFilters.put(Constants.PARENT_TYPE, ((String) filters.get(Constants.PARENT_TYPE)).trim());
			} else {
				errMsg = "ParentType is empty in search request";
			}
		}

		if (!filterExist) {
			errMsg = "Need identifier OR orgName and parentType in Filters";
		}
		Integer limit = (Integer) requestBody.get(Constants.LIMIT);
		if (limit == null) {
			searchFilters.put(Constants.LIMIT, configProperties.getOrgSearchResponseDefaultLimit());
		} else {
			searchFilters.put(Constants.LIMIT, limit);
		}
		return errMsg;
	}

	private String prepareChannelName(String parentMapId, Map<String, Object> requestData) {
		String channelName = "";
		if (StringUtils.isBlank(parentMapId)) {
			return channelName;
		}
		List<OrgHierarchy> parentList = orgRepository.findAllByMapId(parentMapId);
		if (!ObjectUtils.isEmpty(parentList) && parentList.size() > 0) {
			OrgHierarchy parent = parentList.get(0);
			if (!Constants.SPV.equalsIgnoreCase(parent.getParentMapId())) {
				prepareChannelName(parent.getParentMapId(), requestData);
				requestData.put(Constants.L2_MAP_ID, parent.getMapId());
				requestData.put(Constants.L2_ORG_NAME, parent.getOrgName());
			} else {
				requestData.put(Constants.L1_MAP_ID, parent.getMapId());
				requestData.put(Constants.L1_ORG_NAME, parent.getOrgName());
			}
			channelName = parent.getChannel() + configProperties.getOrgChannelDelimitter();
		}
		return channelName;
	}

	private OrgHierarchy getOrgRecord(Map<String, Object> request, OrgHierarchy newOrg) {
		newOrg.setOrgCode((String) request.get(Constants.MAP_ID));
		newOrg.setSbOrgId((String) request.get(Constants.SB_ORG_ID));
		newOrg.setSbOrgType((String) request.get(Constants.SB_ORG_TYPE));
		newOrg.setSbOrgSubType((String) request.get(Constants.SB_SUB_ORG_TYPE));
		newOrg.setSbRootOrgId((String) request.get(Constants.SB_ROOT_ORG_ID));
		newOrg.setL1MapId((String) request.get(Constants.L1_MAP_ID));
		newOrg.setL1OrgName((String) request.get(Constants.L1_ORG_NAME));
		newOrg.setL2MapId((String) request.get(Constants.L2_MAP_ID));
		newOrg.setL2OrgName((String) request.get(Constants.L2_ORG_NAME));
		return newOrg;
	}

	public SBApiResponse createParentOrg(OrgHierarchy parentOrg) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_ORG_EXT_CREATE);
		try {

			String orgId = checkOrgExist(parentOrg.getChannel(), StringUtils.EMPTY);

			if (StringUtils.isEmpty(orgId)) {
				Map<String, Object> request = new HashMap<String, Object>();
				Map<String, Object> requestBody = new HashMap<String, Object>();

				requestBody.put(Constants.ORG_NAME, parentOrg.getOrgName());
				requestBody.put(Constants.CHANNEL, parentOrg.getChannel());
				requestBody.put(Constants.IS_TENANT, true);
				requestBody.put(Constants.ORGANIZATION_TYPE, parentOrg.getSbOrgType());
				requestBody.put(Constants.ORGANIZATION_SUB_TYPE, parentOrg.getSbOrgSubType());
				request.put(Constants.REQUEST, requestBody);
				orgId = createOrgInSunbird(request, parentOrg.getChannel(), StringUtils.EMPTY);
			}

			if (!StringUtils.isEmpty(orgId)) {
				String sbRootOrgId = orgRepository.getSbOrgIdFromMapId(parentOrg.getParentMapId());
				;
				if (StringUtils.isBlank(parentOrg.getSbRootOrgId())) {
					sbRootOrgId = orgRepository.getSbOrgIdFromMapId(parentOrg.getParentMapId());
				}
				if (StringUtils.isBlank(parentOrg.getSbRootOrgId()) && !StringUtils.isEmpty(sbRootOrgId)) {
					orgRepository.updateSbOrgIdAndSbOrgRootIdForChannel(parentOrg.getChannel(),
							orgId, sbRootOrgId);
				} else {
					orgRepository.updateOrgIdForChannel(parentOrg.getChannel(), orgId);
				}
				response.getResult().put(Constants.ORGANIZATION_ID, orgId);
				response.getResult().put(Constants.RESPONSE, Constants.SUCCESS);
				parentOrg.setSbOrgId(orgId);
			} else {
				response.getParams().setErrmsg("Failed to create parent organisation in Sunbird.");
				response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			logger.error("Failed to create parent org. Exception: ", e);
			response.getParams().setErrmsg(e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}
}
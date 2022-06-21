package org.sunbird.org.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
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
	OutboundRequestHandlerServiceImpl outboudService;

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

				Map<String, Object> apiResponse = (Map<String, Object>) outboudService.fetchResultUsingPost(url,
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
		if (!CollectionUtils.isEmpty(existingDataList)) {
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

		if (StringUtils.isEmpty((String) request.get(Constants.ORGANIZATION_TYPE))) {
			params.add(Constants.ORGANIZATION_TYPE);
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
}

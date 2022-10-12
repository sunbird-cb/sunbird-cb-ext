package org.sunbird.common.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.common.model.SunbirdApiHierarchyResultBatch;
import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.model.SunbirdApiUserCourseListResp;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ContentServiceImpl implements ContentService {

	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	@Autowired
	private OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	@Autowired
	CbExtServerProperties serverConfig;

	@Autowired
	private ObjectMapper mapper;

	public SunbirdApiResp getHeirarchyResponse(String contentId) {
		StringBuilder url = new StringBuilder();
		url.append(serverConfig.getContentHost()).append(serverConfig.getHierarchyEndPoint()).append("/" + contentId)
				.append("?hierarchyType=detail");
		SunbirdApiResp response = mapper.convertValue(outboundRequestHandlerService.fetchResult(url.toString()),
				SunbirdApiResp.class);
		if (response.getResponseCode().equalsIgnoreCase("Ok")) {
			return response;
		}

		return null;
	}

	public SunbirdApiUserCourseListResp getUserCourseListResponse(String authToken, String userId) {
		StringBuilder url = new StringBuilder();
		String endPoint = serverConfig.getUserCoursesList().replace("{userUUID}", userId);
		url.append(serverConfig.getCourseServiceHost()).append(endPoint);
		Map<String, String> headers = new HashMap<>();
		headers.put("x-authenticated-user-token", authToken);
		SunbirdApiUserCourseListResp response = mapper.convertValue(
				outboundRequestHandlerService.fetchUsingGetWithHeaders(url.toString(), headers),
				SunbirdApiUserCourseListResp.class);
		if (response.getResponseCode().equalsIgnoreCase("Ok")) {
			return response;
		}
		return null;
	}

	public List<String> getParticipantsList(String xAuthUser, List<String> batchIdList) {
		List<String> participantList = new ArrayList<>();
		StringBuilder url = new StringBuilder();
		url.append(serverConfig.getCourseServiceHost()).append(serverConfig.getParticipantsEndPoint());

		HashMap<String, String> headerValues = new HashMap<>();
		headerValues.put("X-Authenticated-User-Token", xAuthUser);
		headerValues.put("Authorization", serverConfig.getSbApiKey());
		headerValues.put("Content-Type", "application/json");

		Map<String, Object> requestBody = new HashMap<>();
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> batch = new HashMap<>();
		batch.put("active", true);
		request.put("batch", batch);
		requestBody.put("request", request);

		for (String batchId : batchIdList) {
			try {
				batch.put("batchId", batchId);
				SunbirdApiResp response = mapper.convertValue(
						outboundRequestHandlerService.fetchResultUsingPost(url.toString(), requestBody, headerValues),
						SunbirdApiResp.class);
				if (response.getResponseCode().equalsIgnoreCase("Ok")) {
					SunbirdApiHierarchyResultBatch batchResp = response.getResult().getBatch();
					if (batchResp != null && batchResp.getCount() > 0) {
						participantList.addAll(batchResp.getParticipants());
					}
					logger.info("Fetch Participants return - " + participantList.size() + " no. of users.");
				} else {
					logger.warn("Failed to get participants for BatchId - " + batchId);
					logger.warn("Error Response -> " + mapper.writeValueAsString(response));
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}

		return participantList;
	}

	@Override
	public List<String> getParticipantsForBatch(String xAuthUser, String batchId) {
		List<String> participantList = new ArrayList<>();
		StringBuilder url = new StringBuilder();
		url.append(serverConfig.getCourseServiceHost()).append(serverConfig.getParticipantsEndPoint());

		HashMap<String, String> headerValues = new HashMap<>();
		headerValues.put("X-Authenticated-User-Token", xAuthUser);
		headerValues.put("Authorization", serverConfig.getSbApiKey());
		headerValues.put("Content-Type", "application/json");

		Map<String, Object> requestBody = new HashMap<>();
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> batch = new HashMap<>();
		batch.put("active", true);
		request.put("batch", batch);
		requestBody.put("request", request);
		try {
			batch.put("batchId", batchId);
			SunbirdApiResp response = mapper.convertValue(
					outboundRequestHandlerService.fetchResultUsingPost(url.toString(), requestBody, headerValues),
					SunbirdApiResp.class);
			if (response.getResponseCode().equalsIgnoreCase("Ok")) {
				SunbirdApiHierarchyResultBatch batchResp = response.getResult().getBatch();
				if (batchResp != null && batchResp.getCount() > 0) {
					participantList.addAll(batchResp.getParticipants());
				}
			} else {
				logger.warn("Failed to get participants for BatchId - " + batchId);
				logger.warn("Error Response -> " + mapper.writeValueAsString(response));
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return participantList;
	}

	public SunbirdApiResp getAssessmentHierachyResponse(String assessmentId) {
		StringBuilder url = new StringBuilder();
		url.append(serverConfig.getAssessmentHost()).append(serverConfig.getAssessmentHierarchyReadPath());
		SunbirdApiResp response = mapper.convertValue(
				outboundRequestHandlerService.fetchResult(url.toString().replace(Constants.IDENTIFIER, assessmentId)),
				SunbirdApiResp.class);
		if (response.getResponseCode().equalsIgnoreCase("Ok")) {
			return response;
		}

		return null;
	}

	public SunbirdApiResp getQuestionListDetails(List<String> questionIdList) {
		SunbirdApiResp response = null;
		StringBuilder url = new StringBuilder();
		url.append(serverConfig.getAssessmentHost()).append(serverConfig.getAssessmentQuestionListPath());

		Map<String, Object> requestMap = new HashMap<>();
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> search = new HashMap<>();
		search.put(Constants.IDENTIFIER, questionIdList);
		request.put("search", search);
		requestMap.put("request", request);

		Map<String, String> headerValues = new HashMap<>();
		headerValues.put(Constants.AUTH_TOKEN, serverConfig.getSbApiKey());

		response = mapper.convertValue(
				outboundRequestHandlerService.fetchResultUsingPost(url.toString(), requestMap, headerValues),
				SunbirdApiResp.class);
		if (response.getResponseCode().equalsIgnoreCase("Ok")) {
			return response;
		}

		return null;
	}

	public Map<String, Object> searchLiveContent(String contentId) {
		Map<String, Object> response = null;
		HashMap<String, String> headerValues = new HashMap<>();
		headerValues.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
		Map<String, Object> filters = new HashMap<>();
		List<String> primaryCategory = new ArrayList<>(Arrays.asList(Constants.COURSE, Constants.PROGRAM));
		List<String> status = new ArrayList<>(Arrays.asList(Constants.LIVE));
		filters.put(Constants.PRIMARY_CATEGORY, primaryCategory);
		filters.put(Constants.STATUS, status);
		filters.put(Constants.IDENTIFIER, contentId);
		Map<String, Object> sortBy = new HashMap<>();
		sortBy.put(Constants.LAST_UPDATED_ON, Constants.DESCENDING_ORDER);
		List<String> fields = new ArrayList<>(Arrays.asList(Constants.IDENTIFIER, Constants.NAME, Constants.PRIMARY_CATEGORY, Constants.BATCHES));
		Map<String, Object> contentRequestValue = new HashMap<>();
		contentRequestValue.put(Constants.FILTERS, filters);
		contentRequestValue.put(Constants.QUERY, "");
		contentRequestValue.put(Constants.SORT_BY, sortBy);
		contentRequestValue.put(Constants.FIELDS, fields);
		contentRequestValue.put(Constants.LIMIT, 1);
		contentRequestValue.put(Constants.OFFSET, 0);
		Map<String, Object> contentRequest = new HashMap<>();
		contentRequest.put(Constants.REQUEST, contentRequestValue);
		System.out.println(contentRequest);
		response = outboundRequestHandlerService.fetchResultUsingPost(serverConfig.getKmBaseHost() + serverConfig.getKmBaseContentSearch(), contentRequest, headerValues);
		if (Constants.OK.equalsIgnoreCase((String) response.get(Constants.RESPONSE_CODE))) {
			return response;
		}
		return null;
	}
}

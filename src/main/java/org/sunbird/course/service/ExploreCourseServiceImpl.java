package org.sunbird.course.service;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.exception.ApplicationLogicError;
import org.sunbird.staff.model.StaffInfo;

/**
 * Implementation of ExploreCourseService
 * {@link org.sunbird.course.service.ExploreCourseService}
 * 
 * @author karthik
 *
 */
@Service
@SuppressWarnings("unchecked")
public class ExploreCourseServiceImpl implements ExploreCourseService {

	private Logger logger = LoggerFactory.getLogger(ExploreCourseServiceImpl.class);

	@Autowired
	CassandraOperation cassandraOperation;

	@Autowired
	RedisCacheMgr redisCacheMgr;

	@Autowired
	CbExtServerProperties serverProperties;

	@Autowired
	OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	@Override
	public SBApiResponse getExploreCourseList() {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_GET_EXPLORE_COURSE_DETAIL);
		String errMsg = "";
		try {
			List<Map<String, Object>> courseList = cassandraOperation.getRecordsByProperties(
					Constants.SUNBIRD_KEY_SPACE_NAME, Constants.TABLE_EXPLORE_COURSE_LIST, MapUtils.EMPTY_MAP,
					ListUtils.EMPTY_LIST);

			if (CollectionUtils.isNotEmpty(courseList)) {
				Comparator<Map<String, Object>> customComparator = Comparator.comparing(entry -> {
					Integer seqNo = (Integer) entry.get(Constants.SEQ_NO);
					return (seqNo != null) ? seqNo : Integer.MAX_VALUE;
				});
				courseList = courseList.stream().sorted(customComparator).collect(Collectors.toList());
			}
			List<String> identifierList = new ArrayList<String>();
			for (Map<String, Object> course : courseList) {
				identifierList.add((String) course.get(Constants.IDENTIFIER));
			}
			Map<String, Object> searchResponse = searchContent(identifierList);
			if (!Constants.OK.equalsIgnoreCase((String) searchResponse.get(Constants.RESPONSE_CODE))) {
				errMsg = "Failed to get contant details for Identifier List from DB.";
			} else {
				Map<String, Object> responseCourseList = (Map<String, Object>) searchResponse.get(Constants.RESULT);
				List<Map<String, Object>> contentList = (List<Map<String, Object>>) responseCourseList.get(Constants.CONTENT);
				if (CollectionUtils.isNotEmpty(contentList)) {
					List<Map<String, Object>> sortedContentList = identifierList.stream().map(identifier -> contentList.stream()
							.filter(content -> identifier.equals(content.get(Constants.IDENTIFIER)))
							.findFirst().orElse(null)).filter(Objects::nonNull).collect(Collectors.toList());
					responseCourseList.put(Constants.CONTENT, sortedContentList);
				}
				response.setResult(responseCourseList);
			}
		} catch (Exception e) {
			errMsg = "Failed to retrieve explore course list. Exception: " + e.getMessage();
			logger.error(errMsg, e);
		}
		if (StringUtils.isNotEmpty(errMsg)) {
			logger.error("Failed to initialize the Open Course Details to Cache. ErrMsg: " + errMsg);
			response.getParams().setErrmsg(errMsg);
			response.getParams().setStatus(Constants.FAILED);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return response;
	}

	public SBApiResponse refreshCache() {
		redisCacheMgr.deleteKeyByName(Constants.PUBLIC_COURSE_LIST);
		SBApiResponse response = getExploreCourseList();
		response.setId(Constants.API_REFRESH_EXPLORE_COURSE_DETAIL);
		return response;
	}

	private Map<String, Object> searchContent(List<String> identifierList) {
		try {
			StringBuilder sbUrl = new StringBuilder(serverProperties.getKmBaseHost());
			sbUrl.append(serverProperties.getKmBaseContentSearch());
			Map<String, String> headers = new HashMap<String, String>();
			headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
			return outboundRequestHandlerService.fetchResultUsingPost(sbUrl.toString(),
					getContentSearchRequest(identifierList), headers);
		} catch (Exception e) {
			logger.error("Failed to call Content Search for given identifiers.", e);
			throw new ApplicationLogicError(e.getMessage());
		}
	}

	private Map<String, Object> getContentSearchRequest(List<String> identifierList) {
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> requestBody = new HashMap<String, Object>();
		Map<String, Object> filters = new HashMap<String, Object>();
		filters.put(Constants.IDENTIFIER, identifierList);
		filters.put(Constants.STATUS, Constants.LIVE);
		requestBody.put(Constants.FILTERS, filters);
		Map<String, Object> sortBy = new HashMap<String, Object>();
		sortBy.put(Constants.LAST_UPDATED_ON, Constants.DESCENDING_ORDER);
		requestBody.put(Constants.SORT_BY, sortBy);
		requestBody.put(Constants.FIELDS, serverProperties.getKmCompositeSearchFields());
		request.put(Constants.REQUEST, requestBody);
		return request;
	}

	@Override
	public SBApiResponse getExploreCourseListV2() {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_GET_EXPLORE_COURSE_DETAIL);
		String errMsg = "";
		try {
			List<Map<String, Object>> courseList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
					Constants.SUNBIRD_KEY_SPACE_NAME, Constants.TABLE_EXPLORE_COURSE_LIST_V2, MapUtils.EMPTY_MAP,
					ListUtils.EMPTY_LIST);
			if (CollectionUtils.isNotEmpty(courseList)) {
				Comparator<Map<String, Object>> customComparator = Comparator.comparing(entry -> {
					Integer seqNo = (Integer) entry.get(Constants.SEQ_NO);
					return (seqNo != null) ? seqNo : Integer.MAX_VALUE;
				});
				courseList = courseList.stream().sorted(customComparator).collect(Collectors.toList());
			}
			List<String> identifierList = new ArrayList<String>();
			for (Map<String, Object> course : courseList) {
				identifierList.add((String) course.get(Constants.IDENTIFIER));
			}
			if(identifierList.isEmpty()) {
				errMsg = "Contents are not configured in Database.";
			} else {
				Map<String, Object> searchResponse = searchContent(identifierList);
				if (!Constants.OK.equalsIgnoreCase((String) searchResponse.get(Constants.RESPONSE_CODE))) {
					errMsg = "Failed to get contant details for Identifier List from DB.";
				} else {
					Map<String, Object> responseCourseList = (Map<String, Object>) searchResponse.get(Constants.RESULT);
					List<Map<String, Object>> contentList = (List<Map<String, Object>>) responseCourseList.get(Constants.CONTENT);
					if (CollectionUtils.isNotEmpty(contentList)) {
						List<Map<String, Object>> sortedContentList = identifierList.stream()
								.map(identifier -> contentList.stream()
										.filter(content -> identifier.equals(content.get(Constants.IDENTIFIER)))
										.findFirst().orElse(null))
								.filter(Objects::nonNull)
								.sorted(Comparator.comparing(content -> (String) content.get(Constants.PRIMARY_CATEGORY)))
								.collect(Collectors.toList());
						responseCourseList.put(Constants.CONTENT, sortedContentList);
					}
					response.setResult(responseCourseList);
				}
			}
		} catch (Exception e) {
			errMsg = "Failed to retrieve explore course list. Exception: " + e.getMessage();
			logger.error(errMsg, e);
		}
		if (StringUtils.isNotEmpty(errMsg)) {
			logger.error("Failed to initialize the Open Course Details to Cache. ErrMsg: " + errMsg);
			response.getParams().setErrmsg(errMsg);
			response.getParams().setStatus(Constants.FAILED);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public SBApiResponse upsertExploreCourse(Map<String, Object> requestObj) {
		SBApiResponse response =
				ProjectUtil.createDefaultResponse(Constants.API_EXPLORE_COURSE_UPDATE);
		logger.info("ExploreCourseService::upsertExploreCourse:inside method");
		Map<String, Object> masterData = (Map<String, Object>) requestObj.get(Constants.REQUEST);
		String errMsg = validateUpsertRequest(masterData);
		if (StringUtils.isNotBlank(errMsg)) {
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			return response;
		}
		try {
			List<?> dataList = (List<?>) masterData.get(Constants.DATA);
			Iterator<?> iterator = dataList.iterator();

			while (iterator.hasNext()) {
				Map<?, ?> itemMap = (Map) iterator.next();
				Map<String, Object> request = new HashMap<>();
				request.put(Constants.IDENTIFIER, itemMap.get(Constants.IDENTIFIER));

				List<Map<String, Object>> listOfMasterData = cassandraOperation.getRecordsByProperties(
						Constants.KEYSPACE_SUNBIRD, Constants.TABLE_EXPLORE_COURSE_LIST_V2, request,
						new ArrayList<>());

				if (CollectionUtils.isNotEmpty(listOfMasterData)) {
					Map<String, Object> updateRequest = new HashMap<>();
					updateRequest.put(Constants.SEQUENCE_NO, itemMap.get(Constants.SEQUENCE_NO));
					Map<String, Object> updateResponse = cassandraOperation.updateRecord(
							Constants.KEYSPACE_SUNBIRD, Constants.TABLE_EXPLORE_COURSE_LIST_V2, updateRequest,
							request);

					if (updateResponse != null && !Constants.SUCCESS.equalsIgnoreCase(
							(String) updateResponse.get(Constants.RESPONSE))) {
						errMsg = String.format("Failed to update details");
						response.getParams().setErrmsg(errMsg);
						response.setResponseCode(HttpStatus.BAD_REQUEST);
						break;
					} else {
						response.getResult().put(Constants.STATUS, Constants.CREATED);
					}
				} else {
					request.put(Constants.SEQUENCE_NO, itemMap.get(Constants.SEQUENCE_NO));
					response = cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD,
							Constants.TABLE_EXPLORE_COURSE_LIST_V2, request);
					response.setResponseCode(HttpStatus.OK);
					if (!Constants.SUCCESS.equalsIgnoreCase((String) response.get(Constants.RESPONSE))) {
						errMsg = String.format("Failed to create position");
						response.setResponseCode(HttpStatus.BAD_REQUEST);
						response.getParams().setErrmsg(errMsg);
						break;
					} else {
						response.getResult().put(Constants.STATUS, Constants.CREATED);
					}
				}
			}
		} catch (Exception e) {
			errMsg = String.format("Exception occurred while performing upsert operation");
			logger.error(errMsg, e);
		}
		if (StringUtils.isNotBlank(errMsg)) {
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
		}
		return response;
	}

	@Override
	public SBApiResponse deleteExploreCourse(String id) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_EXPLORE_COURSE_DELETE);
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put(Constants.IDENTIFIER, id);
		try {
			List<Map<String, Object>> existingDetails = cassandraOperation.getRecordsByProperties(
					Constants.KEYSPACE_SUNBIRD,
					Constants.TABLE_EXPLORE_COURSE_LIST_V2, keyMap, null);
			if (!existingDetails.isEmpty()) {
				cassandraOperation.deleteRecord(Constants.KEYSPACE_SUNBIRD,
						Constants.TABLE_EXPLORE_COURSE_LIST_V2, keyMap);
				response.getParams().setStatus(Constants.SUCCESSFUL);
				response.getResult().put(Constants.STATUS, Constants.DELETED);
				response.getResult().put(Constants.MESSAGE, "Deleted Explore Course for Id: " + id);
				response.setResponseCode(HttpStatus.OK);
			} else {
				String errMsg = "Failed to find Course for OrgId: " + ", Id: " + id;
				logger.error(errMsg);
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			String errMsg =
					"Exception occurred while deleting the ExploredCourse. Exception: " + ex.getMessage();
			logger.error(errMsg, ex);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	private String validateUpsertRequest(Map<String, Object> masterData) {
		logger.info("ExploreCourseService::validateUpsertRequest:inside method");
		StringBuilder strBuilder = new StringBuilder();
		if (ObjectUtils.isEmpty(masterData)) {
			strBuilder.append("Model object is empty.");
			return strBuilder.toString();
		}
		// Check if the requestData contains the key "data"
		if (!masterData.containsKey(Constants.DATA) || !(masterData.get(
				Constants.DATA) instanceof List)) {
			strBuilder.append("Data is missing or invalid.");
			return strBuilder.toString();
		}
		List<?> dataList = (List<?>) masterData.get(Constants.DATA);
		for (Object item : dataList) {
			if (!(item instanceof Map)) {
				strBuilder.append("Item in data list is not a valid map.");
				return strBuilder.toString();
			}
			Map<?, ?> itemMap = (Map<?, ?>) item;
			if (!itemMap.containsKey(Constants.IDENTIFIER)) {
				strBuilder.append("Item is missing 'identifier'. ");
			}
			if (!itemMap.containsKey(Constants.SEQUENCE_NO)) {
				strBuilder.append("Item is missing seqno. ");
			}
		}
		return strBuilder.toString();
	}
}

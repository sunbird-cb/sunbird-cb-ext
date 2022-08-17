package org.sunbird.course.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * Implementation of ExploreCourseService
 * {@link org.sunbird.course.service.ExploreCourseService}
 * 
 * @author karthik
 *
 */
@Service
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

	@SuppressWarnings("unchecked")
	@Override
	public SBApiResponse getExploreCourseList() {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_GET_EXPLORE_COURSE_DETAIL);
		String errMsg = "";
		try {
			Map<String, Object> responseCourseList = (Map<String, Object>) redisCacheMgr
					.getCache(Constants.PUBLIC_COURSE_LIST);

			if (ObjectUtils.isEmpty(responseCourseList)) {
				List<Map<String, Object>> courseList = cassandraOperation.getRecordsByProperties(
						Constants.SUNBIRD_KEY_SPACE_NAME, Constants.TABLE_EXPLORE_COURSE_LIST, MapUtils.EMPTY_MAP,
						ListUtils.EMPTY_LIST);
				List<String> identifierList = new ArrayList<String>();
				for (Map<String, Object> course : courseList) {
					identifierList.add((String) course.get(Constants.IDENTIFIER));
				}
				Map<String, Object> searchResponse = searchContent(identifierList);
				if (!Constants.OK.equalsIgnoreCase((String) searchResponse.get(Constants.RESPONSE_CODE))) {
					errMsg = "Failed to get contant details for Identifier List from DB.";
				} else {
					responseCourseList = (Map<String, Object>) searchResponse.get(Constants.RESULT);
					redisCacheMgr.putCache(Constants.PUBLIC_COURSE_LIST, responseCourseList);
					response.setResult(responseCourseList);
				}
			} else {
				response.setResult(responseCourseList);
			}
		} catch (Exception e) {
			errMsg = "Failed to retrieve explore course list. Exception: " + e.getMessage();
			logger.error(errMsg, e);
		}
		if (StringUtils.isNotEmpty(errMsg)) {
			response.getParams().setErrmsg(errMsg);
			response.getParams().setStatus(Constants.FAILED);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return response;
	}

	private Map<String, Object> searchContent(List<String> identifierList) {
		try {
			StringBuilder sbUrl = new StringBuilder(serverProperties.getKmBaseHost());
			sbUrl.append(serverProperties.getKmCompositeSearchPath());
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
		requestBody.put(Constants.FILTERS, filters);
		Map<String, Object> sortBy = new HashMap<String, Object>();
		sortBy.put(Constants.LAST_UPDATED_ON, Constants.DESCENDING_ORDER);
		requestBody.put(Constants.SORT_BY, sortBy);
		requestBody.put(Constants.FIELDS, Constants.courseAdditionalParam);
		request.put(Constants.REQUEST, requestBody);
		return request;
	}
}

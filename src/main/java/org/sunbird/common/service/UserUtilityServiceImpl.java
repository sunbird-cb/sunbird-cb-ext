package org.sunbird.common.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.common.model.*;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.exception.ApplicationLogicError;
import org.sunbird.core.logger.CbExtLogger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author akhilesh.kumar05
 *
 */
@Service
public class UserUtilityServiceImpl implements UserUtilityService {

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	CbExtServerProperties props;

	private CbExtLogger logger = new CbExtLogger(getClass().getName());
	private final String SEARCH_RESULT = "Search API response: ";

	@Override
	public boolean validateUser(String rootOrg, String userId){

		Map<String, Object> requestMap = new HashMap<>();

		Map<String, Object> request = new HashMap<>();

		Map<String, String> filters = new HashMap<>();
		filters.put(Constants.USER_ID, userId);
		request.put(Constants.FILTERS, filters);

		requestMap.put("request", request);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		try {
			String reqBodyData = new ObjectMapper().writeValueAsString(requestMap);

			HttpEntity<String> requestEnty = new HttpEntity<>(reqBodyData, headers);

			String serverUrl = props.getSbUrl() + "private/user/v1/search";

			SunbirdApiResp sunbirdApiResp = restTemplate.postForObject(serverUrl, requestEnty, SunbirdApiResp.class);

			boolean expression = (sunbirdApiResp != null && "OK".equalsIgnoreCase(sunbirdApiResp.getResponseCode())
					&& sunbirdApiResp.getResult().getResponse().getCount() >= 1);
				return expression;

		} catch (Exception e) {
			throw new ApplicationLogicError("Sunbird Service ERROR: ", e);
		}

	}

	@Override
	public Map<String, Object> getUsersDataFromUserIds(String rootOrg, List<String> userIds, List<String> source) {
		Map<String, Object> result = new HashMap<>();

		Map<String, Object> requestBody = new HashMap<>();
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> filters = new HashMap<>();
		filters.put("userId", userIds);
		request.put("filters", filters);
		requestBody.put("request", request);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		try {
			HttpEntity<?> requestEnty = new HttpEntity<>(requestBody, headers);
			String url = props.getSbUrl() + props.getUserSearchEndPoint();
			SearchUserApiResp searchUserResult = restTemplate.postForObject(url, requestEnty, SearchUserApiResp.class);
			logger.info("searchUserResult ---->"+ searchUserResult.toString());
			if(searchUserResult !=null && "OK".equalsIgnoreCase(searchUserResult.getResponseCode())
					&& searchUserResult.getResult().getResponse().getCount()>0){
				for(SearchUserApiContent searchUserApiContent: searchUserResult.getResult().getResponse().getContent()){
					result.put(searchUserApiContent.getUserId(), searchUserApiContent);
				}
			}
		} catch (Exception e) {
			throw new ApplicationLogicError("Sunbird Service ERROR: ", e);
		}

		return result;
	}
	
	@Override
	public Map<String, Object> getUsersDataFromUserIds(List<String> userIds, List<String> fields, String authToken) {
		Map<String, Object> result = new HashMap<>();

		// headers
		HttpHeaders headers = new HttpHeaders();
		headers.add(Constants.USER_TOKEN, authToken);
		headers.add(Constants.AUTHORIZATION, props.getSbApiKey());
		// request body
		SunbirdApiRequest requestObj = new SunbirdApiRequest();
		Map<String, Object> reqMap = new HashMap<>();
		reqMap.put(Constants.FILTERS, new HashMap<String, Object>() {
			{
				put(Constants.USER_ID, userIds);
			}
		});
		reqMap.put(Constants.FIELDS_CONSTANT, fields);
		requestObj.setRequest(reqMap);

		try {
			String url = props.getSbUrl() + props.getUserSearchEndPoint();
			HttpEntity<?> requestEnty = new HttpEntity<>(requestObj, headers);
			SearchUserApiResp searchUserResult = restTemplate.postForObject(url, requestEnty, SearchUserApiResp.class);
			logger.debug(SEARCH_RESULT + searchUserResult.toString());
			if (searchUserResult != null && Constants.OK.equalsIgnoreCase(searchUserResult.getResponseCode())
					&& searchUserResult.getResult().getResponse().getCount() > 0) {
				for (SearchUserApiContent searchUserApiContent : searchUserResult.getResult().getResponse()
						.getContent()) {
					result.put(searchUserApiContent.getUserId(), searchUserApiContent);
				}
			}
		} catch (Exception e) {
			throw new ApplicationLogicError("Sunbird Service ERROR: ", e);
		}

		return result;
	}
}

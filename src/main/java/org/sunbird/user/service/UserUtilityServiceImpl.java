package org.sunbird.user.service;

import java.sql.Timestamp;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SearchUserApiContent;
import org.sunbird.common.model.SearchUserApiResp;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.exception.ApplicationLogicError;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.telemetry.model.UserInfo;
import org.sunbird.user.registration.model.UserRegistration;
import org.sunbird.user.util.TelemetryUtils;

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

	@Autowired
	TelemetryUtils telemetryUtils;

	@Autowired
	private CassandraOperation cassandraOperation;

	@Autowired
	OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	@Autowired
	CbExtServerProperties serverConfig;

	private CbExtLogger logger = new CbExtLogger(getClass().getName());
	private final String SEARCH_RESULT = "Search API response: ";

	public boolean validateUser(String userId) {
		return validateUser(StringUtils.EMPTY, userId);
	}

	@Override
	public boolean validateUser(String rootOrg, String userId) {

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
			logger.info("searchUserResult ---->" + searchUserResult.toString());
			if (searchUserResult != null && "OK".equalsIgnoreCase(searchUserResult.getResponseCode())
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

	@Override
	public Map<String, Object> updateLogin(UserInfo userLoginInfo) {
		Map<String, Object> response = new HashMap<>();
		logger.info(String.format("Updating User Login: rootOrg: %s, userId: %s", userLoginInfo.getOrgId(),
				userLoginInfo.getUserId()));
		userLoginInfo.setLoginTime(new Timestamp(new Date().getTime()));
		try {
			Map<String, Object> propertyMap = new HashMap<>();
			propertyMap.put(Constants.USER_ID, userLoginInfo.getUserId());
			List<Map<String, Object>> details = cassandraOperation.getRecordsByProperties(Constants.DATABASE,
					Constants.LOGIN_TABLE, propertyMap, null);
			if (CollectionUtils.isEmpty(details)) {
				Map<String, Object> request = new HashMap<>();
				request.put(Constants.USER_ID, userLoginInfo.getUserId());
				request.put(Constants.FIRST_LOGIN_TIME, userLoginInfo.getLoginTime());
				cassandraOperation.insertRecord(Constants.DATABASE, Constants.LOGIN_TABLE, request);
				telemetryUtils.pushDataToKafka(telemetryUtils.getUserslastLoginTelemetryEventData(userLoginInfo),
						serverConfig.getUserUtilityTopic());
			}
		} catch (Exception e) {
			throw new ApplicationLogicError("Sunbird Service ERROR: ", e);
		}
		response.put(userLoginInfo.getUserId(), Boolean.TRUE);
		return response;
	}

	@Override
	public Map<String, Object> getUsersReadData(String userId, String authToken, String X_authToken) {
		Map<String, String> header = new HashMap<>();
		header.put(Constants.AUTH_TOKEN, authToken);
		header.put(Constants.X_AUTH_TOKEN, X_authToken);
		Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService
				.fetchUsingGetWithHeadersProfile(serverConfig.getSbUrl() + serverConfig.getLmsUserReadPath() + userId,
						header);
		Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
		Map<String, Object> responseMap = (Map<String, Object>) result.get(Constants.RESPONSE);
		return responseMap;
	}

	@Override
	public Boolean createUser(UserInfo userInfo, String xAuthToken) {
		try {
			Map<String, String> headers = new HashMap<>();
			headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
			headers.put(Constants.USER_TOKEN, xAuthToken);
			headers.put(Constants.AUTHORIZATION, props.getSbApiKey());
			Map<String, Object> request = new HashMap<>();
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put(Constants.CHANNEL, userInfo.getChannel());
			requestBody.put(Constants.EMAIL, userInfo.getEmail());
			requestBody.put(Constants.EMAIL_VERIFIED, userInfo.getEmail());
			requestBody.put(Constants.FIRSTNAME, userInfo.getFirstName());
			requestBody.put(Constants.LASTNAME, userInfo.getLastName());
			request.put(Constants.REQUEST, requestBody);
			Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService
					.fetchResultUsingPost(props.getSbUrl() + props.getUserCreateEndPoint(), request,
							headers);
			if (readData.get(Constants.RESPONSE_CODE).toString().equalsIgnoreCase(Constants.OK)) {
				Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
				readUser((String) result.get(Constants.USER_ID), xAuthToken);
				userInfo.setUserId((String) result.get(Constants.USER_ID));
				updateUser(userInfo, xAuthToken);
				assignRole(userInfo, xAuthToken);
				return resetPassword(userInfo, xAuthToken);
			}
		} catch (RestClientException e) {
			logger.info(e.getMessage());
		}
		return false;
	}

	public boolean readUser(String userId, String xAuthToken) {
		try {
			Map<String, String> headers = new HashMap<>();
			headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
			headers.put(Constants.USER_TOKEN, xAuthToken);
			headers.put(Constants.AUTHORIZATION, props.getSbApiKey());
			Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService
					.fetchUsingGetWithHeadersProfile(props.getSbUrl() + props.getUserReadEndPoint() + userId,
							headers);
			if (readData.get(Constants.RESPONSE_CODE).toString().equalsIgnoreCase(Constants.OK)) {
				Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
				Map<String, Object> responseMap = (Map<String, Object>) result.get(Constants.RESPONSE);
				if (!responseMap.isEmpty()) {
					String userName = (String) responseMap.get(Constants.USER_NAME);
					String identifier = (String) responseMap.get(Constants.IDENTIFIER);
					String firstName = (String) responseMap.get(Constants.FIRSTNAME);
					String lastName = (String) responseMap.get(Constants.LASTNAME);
					String rootOrgId = (String) responseMap.get(Constants.ROOT_ORG_ID);
					return createNodeBBUser(userName, identifier, firstName, lastName, xAuthToken);
				}

			}
		} catch (RestClientException e) {
			logger.info(e.getMessage());
		}
		return false;
	}

	public boolean createNodeBBUser(String userName, String identifier, String firstName, String lastName, String xAuthToken) {
		try {
			Map<String, Object> request = new HashMap<>();
			Map<String, Object> requestBody = new HashMap<>();
			Map<String, String> headers = new HashMap<>();
			headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
			headers.put(Constants.USER_TOKEN, xAuthToken);
			headers.put(Constants.AUTHORIZATION, props.getSbApiKey());
			requestBody.put(Constants.USERNAME, userName);
			requestBody.put(Constants.IDENTIFIER, identifier);
			requestBody.put(Constants.FULLNAME, firstName + " " + lastName);
			request.put(Constants.REQUEST, requestBody);
			Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService
					.fetchResultUsingPost(props.getSbUrl() + props.getUserCreateBBUserEndPoint(), request, headers);
			if (readData.get(Constants.RESPONSE_CODE).toString().equalsIgnoreCase(Constants.OK)) {
				Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
				if (!result.isEmpty() && result != null) {
					if (((String) result.get(Constants.USER_NAME)).equalsIgnoreCase(userName)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return false;
	}

	public boolean updateUser(UserInfo userInfo, String xAuthToken) {
		try {
			Map<String, Object> request = new HashMap<>();
			Map<String, String> headers = new HashMap<>();
			headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
			headers.put(Constants.USER_TOKEN, xAuthToken);
			headers.put(Constants.AUTHORIZATION, props.getSbApiKey());
			Map<String, Object> profileDetails = new HashMap<>();
			Map<String, Object> employmentDetails = new HashMap<>();
			employmentDetails.put(Constants.DEPARTMENTNAME, userInfo.getChannel());
			Map<String, Object> personalDetails = new HashMap<>();
			personalDetails.put(Constants.FIRST_NAME_, userInfo.getFirstName());
			personalDetails.put(Constants.PRIMARY_EMAIL, userInfo.getEmail());
			personalDetails.put(Constants.SURNAME, userInfo.getLastName());
			profileDetails.put(Constants.EMPLOYMENTDETAILS, employmentDetails);
			profileDetails.put(Constants.PERSONAL_DETAILS, personalDetails);
			if (userInfo.getDesignation() != null) {
				Map<String, Object> designation = new HashMap<>();
				designation.put(Constants.DESIGNATION, userInfo.getDesignation());
				profileDetails.put(Constants.PROFESSIONAL_DETAILS, designation);
			}
			request.put(Constants.PROFILE_DETAILS, profileDetails);
			request.put(Constants.USER_ID, userInfo.getUserId());
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put(Constants.REQUEST, request);
			Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService
					.fetchResultUsingPatch(props.getSbUrl() + props.getUserUpdateEndPoint(), requestBody, headers);
			if (readData.get(Constants.RESPONSE_CODE).toString().equalsIgnoreCase(Constants.OK)) {
				Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
				Map<String, Object> responseMap = (Map<String, Object>) result.get(Constants.RESPONSE);
				return true;
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return false;
	}

	public boolean resetPassword(UserInfo userInfo, String xAuthToken) {
		try {
			Map<String, Object> request = new HashMap<>();
			Map<String, Object> requestBody = new HashMap<>();
			Map<String, String> headers = new HashMap<>();
			headers.put(Constants.USER_TOKEN, xAuthToken);
			headers.put(Constants.AUTHORIZATION, props.getSbApiKey());
			requestBody.put(Constants.KEY, userInfo.getEmail());
			requestBody.put(Constants.TYPES, Constants.EMAIL);
			requestBody.put(Constants.USER_ID, userInfo.getUserId());
			request.put(Constants.REQUEST, requestBody);
			Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService
					.fetchResultUsingPost(props.getSbUrl() + props.getUserResetPasswordEndPoint(), request, headers);
			if (readData.get(Constants.RESPONSE_CODE).toString().equalsIgnoreCase(Constants.OK)) {
				Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
				if (result != null && ((String) result.get(Constants.RESPONSE)).equalsIgnoreCase(Constants.SUCCESS)) {
					String emailLink = (String) result.get(Constants.LINK);
					userInfo.setEmailLink(emailLink);
					return sendWelcomeEmail(userInfo, xAuthToken);
				}
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return false;
	}

	public boolean assignRole(UserInfo userInfo, String xAuthToken) {
		try {
			Map<String, Object> requestBody = new HashMap<>();
			Map<String, Object> request = new HashMap<>();
			Map<String, String> headers = new HashMap<>();
			headers.put(Constants.USER_TOKEN, xAuthToken);
			headers.put(Constants.AUTHORIZATION, props.getSbApiKey());
			requestBody.put(Constants.ORGANISATION_ID, userInfo.getOrgId());
			requestBody.put(Constants.ROLES, Arrays.asList(Constants.PUBLIC));
			requestBody.put(Constants.USER_ID, userInfo.getUserId());
			request.put(Constants.REQUEST, requestBody);
			Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService
					.fetchResultUsingPost(props.getSbUrl() + props.getUserAssignRoleEndPoint(), request, headers);
			if (readData.get(Constants.RESPONSE_CODE).toString().equalsIgnoreCase(Constants.OK)) {
				Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
				Map<String, Object> responseMap = (Map<String, Object>) result.get(Constants.RESPONSE);
				return true;
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return false;
	}

	public boolean sendWelcomeEmail(UserInfo userInfo, String xAuthToken) {
		try {
			Map<String, String> headers = new HashMap<>();
			Map<String, Object> requestBody = new HashMap<>();
			headers.put(Constants.USER_TOKEN, xAuthToken);
			headers.put(Constants.AUTHORIZATION, props.getSbApiKey());
			requestBody.put(Constants.ALLOWED_LOGING, "You can use your email to Login");
			requestBody.put(Constants.BODY, "Hello");
			requestBody.put(Constants.EMAIL_TEMPLATE_TYPE, "iGotWelcome");
			requestBody.put(Constants.FIRSTNAME, userInfo.getFirstName());
			requestBody.put(Constants.LINK, userInfo.getEmailLink());
			requestBody.put(Constants.MODE, Constants.EMAIL);
			requestBody.put(Constants.ORG_NAME, userInfo.getChannel());
			requestBody.put(Constants.RECIPIENT_EMAILS, Arrays.asList(userInfo.getEmail()));
			requestBody.put(Constants.SET_PASSWORD_LINK, true);
			requestBody.put(Constants.SUBJECT, "Welcome Email");
			requestBody.put(Constants.WELCOME_MESSAGE, "Hello");
			Map<String, Object> request = new HashMap<>();
			request.put(Constants.REQUEST, requestBody);
			Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService
					.fetchResultUsingPost(props.getSbUrl() + props.getUserNotificationEmailEndpoint(), request, headers);
			if (readData.get(Constants.RESPONSE_CODE).toString().equalsIgnoreCase(Constants.OK)) {
				Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
				if (result != null && ((String) result.get(Constants.RESPONSE)).equalsIgnoreCase(Constants.SUCCESS)) {
					return true;
				}
			}
		} catch (Exception e) {
			logger.info(String.format("Error while sending the welcome email %s", e.getMessage()));
		}
		return false;
	}

}

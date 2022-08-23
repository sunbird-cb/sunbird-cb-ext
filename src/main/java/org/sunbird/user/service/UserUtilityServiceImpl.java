package org.sunbird.user.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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
import org.sunbird.telemetry.model.LastLoginInfo;
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

	private Logger logger = LoggerFactory.getLogger(UserUtilityServiceImpl.class);

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

		requestMap.put(Constants.REQUEST, request);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		try {
			String reqBodyData = new ObjectMapper().writeValueAsString(requestMap);

			HttpEntity<String> requestEnty = new HttpEntity<>(reqBodyData, headers);

			String serverUrl = props.getSbUrl() + props.getUserSearchEndPoint();

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
	public Map<String, Object> updateLogin(LastLoginInfo userLoginInfo) {
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
	public Map<String, Object> getUsersReadData(String userId, String authToken, String userAuthToken) {
		Map<String, String> header = new HashMap<>();
		if (StringUtils.isNotEmpty(authToken)) {
			header.put(Constants.AUTH_TOKEN, authToken);
		}
		if (StringUtils.isNotEmpty(userAuthToken)) {
			header.put(Constants.X_AUTH_TOKEN, userAuthToken);
		}
		Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService
				.fetchUsingGetWithHeadersProfile(serverConfig.getSbUrl() + serverConfig.getLmsUserReadPath() + userId,
						header);
		Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
		Map<String, Object> responseMap = (Map<String, Object>) result.get(Constants.RESPONSE);
		return responseMap;
	}

	@Override
	public boolean createUser(UserRegistration userRegistration) {
		boolean retValue = false;
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(Constants.EMAIL, userRegistration.getEmail());
		requestBody.put(Constants.CHANNEL, userRegistration.getOrgName());
		requestBody.put(Constants.FIRSTNAME, userRegistration.getFirstName());
		requestBody.put(Constants.LASTNAME, userRegistration.getLastName());
		requestBody.put(Constants.EMAIL_VERIFIED, true);
		request.put(Constants.REQUEST, requestBody);
		try {
			Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPost(
					props.getSbUrl() + props.getLmsUserCreatePath(), request, getDefaultHeaders());
			if (Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
				Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
				userRegistration.setUserId((String) result.get(Constants.USER_ID));
				Map<String, Object> userData = getUsersReadData(userRegistration.getUserId(), StringUtils.EMPTY,
						StringUtils.EMPTY);
				if (!CollectionUtils.isEmpty(userData)) {
					userRegistration.setUserName((String) userData.get(Constants.USER_NAME));
					retValue = updateUser(userRegistration);
				}
			}
		} catch (Exception e) {
			logger.error("Failed to run the create user flow. UserRegCode : " + userRegistration.getRegistrationCode(),
					e);
		}
		printMethodExecutionResult("Create User", userRegistration.toMininumString(), retValue);
		return retValue;
	}

	@Override
	public boolean updateUser(UserRegistration userRegistration) {
		boolean retValue = false;
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(Constants.USER_ID, userRegistration.getUserId());
		Map<String, Object> profileDetails = new HashMap<String, Object>();
		profileDetails.put(Constants.MANDATORY_FIELDS_EXISTS, false);
		Map<String, Object> employementDetails = new HashMap<String, Object>();
		employementDetails.put(Constants.DEPARTMENTNAME, userRegistration.getOrgName());
		profileDetails.put(Constants.EMPLOYMENTDETAILS, employementDetails);
		Map<String, Object> personalDetails = new HashMap<String, Object>();
		personalDetails.put(Constants.FIRSTNAME.toLowerCase(), userRegistration.getFirstName());
		personalDetails.put(Constants.SURNAME, userRegistration.getLastName());
		personalDetails.put(Constants.PRIMARY_EMAIL, userRegistration.getEmail());
		profileDetails.put(Constants.PERSONAL_DETAILS, personalDetails);

		Map<String, Object> professionDetailObj = new HashMap<String, Object>();
		professionDetailObj.put(Constants.ORGANIZATION_TYPE, Constants.GOVERNMENT);
		if (StringUtils.isNotEmpty(userRegistration.getPosition())) {
			professionDetailObj.put(Constants.DESIGNATION, userRegistration.getPosition());
		}
		List<Map<String, Object>> professionalDetailsList = new ArrayList<Map<String, Object>>();
		professionalDetailsList.add(professionDetailObj);
		profileDetails.put(Constants.PROFESSIONAL_DETAILS, professionalDetailsList);

		requestBody.put(Constants.PROFILE_DETAILS, profileDetails);
		request.put(Constants.REQUEST, requestBody);
		Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService
				.fetchResultUsingPatch(props.getSbUrl() + props.getLmsUserUpdatePath(), request, getDefaultHeaders());
		if (Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
			retValue = assignRole(userRegistration.getSbOrgId(), userRegistration.getUserId(),
					userRegistration.toMininumString());
			if (retValue) {
				retValue = createNodeBBUser(userRegistration);
			}
		}
		printMethodExecutionResult("UpdateUser", userRegistration.toMininumString(), retValue);
		return retValue;
	}

	public boolean assignRole(String sbOrgId, String userId, String objectDetails) {
		boolean retValue = false;
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(Constants.ORGANIZATION_ID, sbOrgId);
		requestBody.put(Constants.USER_ID, userId);
		requestBody.put(Constants.ROLES, Arrays.asList(Constants.PUBLIC));
		request.put(Constants.REQUEST, requestBody);
		Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService
				.fetchResultUsingPost(props.getSbUrl() + props.getSbAssignRolePath(), request, getDefaultHeaders());
		if (Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
			retValue = true;
		}
		printMethodExecutionResult("AssignRole", objectDetails, retValue);
		return retValue;
	}

	@Override
	public boolean createNodeBBUser(UserRegistration userRegistration) {
		boolean retValue = false;
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(Constants.USER_NAME.toLowerCase(), userRegistration.getUserName());
		requestBody.put(Constants.IDENTIFIER, userRegistration.getUserId());
		requestBody.put(Constants.USER_FULL_NAME.toLowerCase(),
				userRegistration.getFirstName() + " " + userRegistration.getLastName());
		request.put(Constants.REQUEST, requestBody);

		Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPost(
				props.getDiscussionHubHost() + props.getDiscussionHubCreateUserPath(), request, getDefaultHeaders());
		if (Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
			retValue = getActivationLink(userRegistration);
		}
		printMethodExecutionResult("Create NodeBB User", userRegistration.toMininumString(), retValue);
		return retValue;
	}

	@Override
	public boolean getActivationLink(UserRegistration userRegistration) {
		boolean retValue = false;
		Map<String, Object> request = new HashMap<String, Object>();
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(Constants.USER_ID, userRegistration.getUserId());
		requestBody.put(Constants.KEY, Constants.EMAIL);
		requestBody.put(Constants.TYPE, Constants.EMAIL);
		request.put(Constants.REQUEST, requestBody);
		Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService
				.fetchResultUsingPost(props.getSbUrl() + props.getSbResetPasswordPath(), request, getDefaultHeaders());
		if (Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
			Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
			if (!CollectionUtils.isEmpty(result)) {
				retValue = sendWelcomeEmail((String) result.get(Constants.LINK), userRegistration);
			}
		}
		printMethodExecutionResult("GetActivationLink", userRegistration.toMininumString(), retValue);
		return retValue;
	}

	public boolean sendWelcomeEmail(String activationLink, UserRegistration userRegistration) {
		boolean retValue = false;
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(Constants.ALLOWED_LOGGING, "You can use your email to Login");
		requestBody.put(Constants.BODY, Constants.HELLO);
		requestBody.put(Constants.EMAIL_TEMPLATE_TYPE, props.getWelcomeEmailTemplate());
		requestBody.put(Constants.FIRSTNAME, userRegistration.getFirstName());
		requestBody.put(Constants.LINK, activationLink);
		requestBody.put(Constants.MODE, Constants.EMAIL);
		requestBody.put(Constants.ORG_NAME, userRegistration.getOrgName());
		requestBody.put(Constants.RECIPIENT_EMAILS, Arrays.asList(userRegistration.getEmail()));
		requestBody.put(Constants.SET_PASSWORD_LINK, true);
		requestBody.put(Constants.SUBJECT, props.getWelcomeEmailSubject());
		requestBody.put(Constants.WELCOME_MESSAGE, Constants.HELLO);

		request.put(Constants.REQUEST, requestBody);

		Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPost(
				props.getSbUrl() + props.getSbSendNotificationEmailPath(), request, getDefaultHeaders());
		if (Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
			retValue = true;
		}
		printMethodExecutionResult("SendWelcomeEmail", userRegistration.toMininumString(), retValue);
		return retValue;
	}

	private void printMethodExecutionResult(String methodAction, String objectDetails, boolean isSuccess) {
		StringBuilder strBuilder = new StringBuilder("Action : [").append(methodAction).append("] ");
		if (isSuccess) {
			strBuilder.append(" is successfully executed. ");
		} else {
			strBuilder.append(" is failed to execute. ");
		}
		if (StringUtils.isNotEmpty(objectDetails)) {
			strBuilder.append("For Object : ").append(objectDetails);
		}
		logger.info(strBuilder.toString());
	}

	private Map<String, String> getDefaultHeaders() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
		return headers;
	}
}

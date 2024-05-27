package org.sunbird.user.service;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.jcraft.jsch.UserInfo;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.*;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.*;
import org.sunbird.core.cipher.DecryptServiceImpl;
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

	@Autowired
	DecryptServiceImpl decryptService;

	@Autowired
	IndexerService indexerService;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	AccessTokenValidator accessTokenValidator;

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
			Map<String, Object> response = outboundRequestHandlerService.fetchResultUsingPost(
					url, requestObj, null);
			SearchUserApiResp searchUserResult = objectMapper.convertValue(response, SearchUserApiResp.class);
			if (searchUserResult != null && Constants.OK.equalsIgnoreCase(searchUserResult.getResponseCode())
					&& searchUserResult.getResult().getResponse().getCount() > 0) {
				for (SearchUserApiContent searchUserApiContent : searchUserResult.getResult().getResponse()
						.getContent()) {
					if (searchUserApiContent.getProfileDetails() == null) {
						searchUserApiContent.setProfileDetails(new SunbirdUserProfileDetail());
					}
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
		requestBody.put(Constants.CHANNEL, userRegistration.getChannel());
		requestBody.put(Constants.FIRSTNAME, userRegistration.getFirstName());
		requestBody.put(Constants.EMAIL_VERIFIED, true);
		requestBody.put(Constants.PHONE, userRegistration.getPhone());
		requestBody.put(Constants.PHONE_VERIFIED, true);
		request.put(Constants.REQUEST, requestBody);
		try {
			Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPost(
					props.getSbUrl() + props.getLmsUserCreatePath(), request, ProjectUtil.getDefaultHeaders());
			if (Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
				Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
				userRegistration.setUserId((String) result.get(Constants.USER_ID));
				Map<String, Object> userData = getUsersReadData(userRegistration.getUserId(), StringUtils.EMPTY,
						StringUtils.EMPTY);
				if (!CollectionUtils.isEmpty(userData)) {
					userRegistration.setUserName((String) userData.get(Constants.USER_NAME));
					userRegistration.setSbOrgId((String) userData.get(Constants.ROOT_ORG_ID) );
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
		personalDetails.put(Constants.PRIMARY_EMAIL, userRegistration.getEmail());
		personalDetails.put(Constants.MOBILE, userRegistration.getPhone());
		personalDetails.put(Constants.PHONE_VERIFIED, true);
		profileDetails.put(Constants.PERSONAL_DETAILS, personalDetails);
		Map<String, Object> professionDetailObj = new HashMap<String, Object>();
		professionDetailObj.put(Constants.ORGANIZATION_TYPE, Constants.GOVERNMENT);
		if (StringUtils.isNotEmpty(userRegistration.getPosition())) {
			professionDetailObj.put(Constants.DESIGNATION, userRegistration.getPosition());
		}
		if (!StringUtils.isEmpty(userRegistration.getGroup())) {
			professionDetailObj.put(Constants.GROUP, userRegistration.getGroup());
		}
		List<Map<String, Object>> professionalDetailsList = new ArrayList<Map<String, Object>>();
		professionalDetailsList.add(professionDetailObj);
		profileDetails.put(Constants.PROFESSIONAL_DETAILS, professionalDetailsList);
		Map<String, Object> additionalProperties = new HashMap<String, Object>();
		if (!StringUtils.isEmpty(userRegistration.getGroup())) {
			additionalProperties.put(Constants.GROUP, userRegistration.getGroup());
		}
		if (!CollectionUtils.isEmpty(userRegistration.getTag())) {
			additionalProperties.put(Constants.TAG, userRegistration.getTag());
		}
		if (!StringUtils.isEmpty(userRegistration.getExternalSystemId())) {
			additionalProperties.put(Constants.EXTERNAL_SYSTEM_ID, userRegistration.getExternalSystemId());
		}
		if (!StringUtils.isEmpty(userRegistration.getExternalSystem())) {
			additionalProperties.put(Constants.EXTERNAL_SYSTEM, userRegistration.getExternalSystem());
		}
		profileDetails.put(Constants.ADDITIONAL_PROPERTIES, additionalProperties);
		requestBody.put(Constants.PROFILE_DETAILS, profileDetails);
		request.put(Constants.REQUEST, requestBody);
		Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPatch(
				props.getSbUrl() + props.getLmsUserUpdatePrivatePath(), request, ProjectUtil.getDefaultHeaders());
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
		Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPost(
				props.getSbUrl() + props.getSbAssignRolePath(), request, ProjectUtil.getDefaultHeaders());
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
				userRegistration.getFirstName());
		request.put(Constants.REQUEST, requestBody);

		retValue = getActivationLink(userRegistration);
		/*Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPost(
				props.getDiscussionHubHost() + props.getDiscussionHubCreateUserPath(), request,
				ProjectUtil.getDefaultHeaders());
		if (Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
			retValue = getActivationLink(userRegistration);
		}*/
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
		Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPost(
				props.getSbUrl() + props.getSbResetPasswordPath(), request, ProjectUtil.getDefaultHeaders());
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
		requestBody.put(Constants.SIGNIN_LINK, props.getWelcomeEmailSigninLink());
		requestBody.put(Constants.DISCOVER_LINK, props.getWelcomeEmailDiscoverLink());
		requestBody.put(Constants.MEETING_LINK, props.getWelcomeEmailMeetingLink());
		requestBody.put(Constants.PROFILE_UPDATE_LINK,props.getWelcomeEmailProfileUpdateLink());

		request.put(Constants.REQUEST, requestBody);

		Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPost(
				props.getSbUrl() + props.getSbSendNotificationEmailPath(), request, ProjectUtil.getDefaultHeaders());
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

	@Override
	public Map<String, Map<String, String>> getUserDetails(List<String> userIds, List<String> fields) {
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
			Map<String, Object> apiResponse = outboundRequestHandlerService.fetchResultUsingPost(url, requestObj, null);

			if (Constants.OK.equalsIgnoreCase((String) apiResponse.get(Constants.RESPONSE_CODE))) {
				Map<String, Object> result = (Map<String, Object>) apiResponse.get(Constants.RESULT);
				Map<String, Object> searchResponse = (Map<String, Object>) result.get(Constants.RESPONSE);
				int count = (int) searchResponse.get(Constants.COUNT);
				if (count > 0) {
					Map<String, Map<String, String>> userDetailsMap = new HashMap<String, Map<String, String>>();
					List<Map<String, Object>> userProfiles = (List<Map<String, Object>>) searchResponse
							.get(Constants.CONTENT);
					if (!CollectionUtils.isEmpty(userProfiles)) {
						for (Map<String, Object> userProfile : userProfiles) {
							if (userProfile.get("profileDetails") != null) {
								HashMap<String, Object> profileDetails = (HashMap<String, Object>) userProfile
										.get("profileDetails");
								HashMap<String, Object> personalDetails = (HashMap<String, Object>) profileDetails
										.get("personalDetails");
								Map<String, String> record = new HashMap<>();
								record.put(Constants.USER_ID, (String) userProfile.get(Constants.IDENTIFIER));
								record.put(Constants.FIRSTNAME,
										(String) personalDetails.get(Constants.FIRST_NAME_LOWER_CASE));
								record.put(Constants.EMAIL, (String) personalDetails.get(Constants.PRIMARY_EMAIL));
								record.put(Constants.ROOT_ORG_ID, (String) userProfile.get(Constants.ROOT_ORG_ID));
								record.put(Constants.CHANNEL, (String) userProfile.get(Constants.CHANNEL));

								userDetailsMap.put(record.get(Constants.USER_ID), record);
							}
						}
					}
					return userDetailsMap;
				}
			}
		} catch (Exception e) {
			logger.error("Failed to get user details. Exception: ", e);
		}

		return MapUtils.EMPTY_MAP;
	}

	@Override
	public void getUserDetailsFromDB(List<String> userIds, List<String> fields,
									 Map<String, Map<String, String>> userInfoMap) {
		Map<String, Object> propertyMap = new HashMap<>();
		
		try {
			for (int i = 0; i < userIds.size(); i += 10) {
				List<String> userList = userIds.subList(i, Math.min(userIds.size(), i + 10));
				propertyMap.put(Constants.ID, userList);

				List<Map<String, Object>> userInfoList = cassandraOperation
						.getRecordsByPropertiesWithoutFiltering(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_USER, propertyMap, fields);
				for (Map<String, Object> user : userInfoList) {
					Map<String, String> userMap = new HashMap<String, String>();
					String userId = (String) user.get(Constants.USER_ID);

					if (userInfoMap.containsKey(userId)) {
						continue;
					}

					for (String field : fields) {
						if (user.containsKey(field)) {
							if (Constants.DECRYPTED_FIELDS.contains(field)) {
								if (StringUtils.isNotBlank((String) user.get(field))) {
									String value = decryptService.decryptString((String) user.get(field));
									if (StringUtils.isBlank(value)) {
										logger.error(
												String.format("Invalid valid for field %s for user %s", field, userId));
									}
									userMap.put(field, value);
								}
							} else {
								userMap.put(field, (String) user.get(field));
							}
						}
					}
					userInfoMap.put(userId, userMap);
				}
			}
		} catch (Exception e) {
			logger.error("Failed to get user details from DB. Exception: ", e);
		}
	}

	public void enrichUserInfo(List<String> fields, Map<String, Map<String, String>> userInfoMap) {
		Iterator<Entry<String, Map<String, String>>> it = userInfoMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Map<String, String>> item = it.next();

			for (String field : fields) {
				if (item.getValue().containsKey(field)) {
					if (Constants.DECRYPTED_FIELDS.contains(field)) {
						if (StringUtils.isNotBlank((String) item.getValue().get(field))) {
							String value = decryptService.decryptString((String) item.getValue().get(field));
							item.getValue().put(field, StringUtils.isNotBlank(value) ? value : StringUtils.EMPTY);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean isUserExist(String key, String value) {
		// request body
		SunbirdApiRequest requestObj = new SunbirdApiRequest();
		Map<String, Object> reqMap = new HashMap<>();
		reqMap.put(Constants.FILTERS, new HashMap<String, Object>() {
			{
				put(key, value);
			}
		});
		requestObj.setRequest(reqMap);

		HashMap<String, String> headersValue = new HashMap<>();
		headersValue.put(Constants.CONTENT_TYPE, "application/json");
		headersValue.put(Constants.AUTHORIZATION, props.getSbApiKey());

		try {
			String url = props.getSbUrl() + props.getUserSearchEndPoint();

			Map<String, Object> response = outboundRequestHandlerService.fetchResultUsingPost(url, requestObj,
					headersValue);
			if (response != null && "OK".equalsIgnoreCase((String) response.get("responseCode"))) {
				Map<String, Object> map = (Map<String, Object>) response.get("result");
				if (map.get("response") != null) {
					Map<String, Object> responseObj = (Map<String, Object>) map.get("response");
					int count = (int) responseObj.get(Constants.COUNT);
					if (count == 0)
						return false;
					else
						return true;
				}
			}
		} catch (Exception e) {
			throw new ApplicationLogicError("Sunbird Service ERROR: ", e);
		}
		return true;
	}

	@Override
	public boolean validateGroup(String group) {
		List<String> groupValues = serverConfig.getBulkUploadGroupValue();
		return groupValues != null && groupValues.contains(group);
	}

	@Override
	public String createBulkUploadUser(UserRegistration userRegistration) {
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(Constants.EMAIL, userRegistration.getEmail());
		requestBody.put(Constants.CHANNEL, userRegistration.getChannel());
		requestBody.put(Constants.FIRSTNAME, userRegistration.getFirstName());
		requestBody.put(Constants.EMAIL_VERIFIED, true);
		requestBody.put(Constants.PHONE, userRegistration.getPhone());
		requestBody.put(Constants.PHONE_VERIFIED, true);
		if (CollectionUtils.isEmpty(userRegistration.getRoles())) {
			requestBody.put(Constants.ROLES, Arrays.asList(Constants.PUBLIC));
		} else {
			requestBody.put(Constants.ROLES, userRegistration.getRoles());
		}
		request.put(Constants.REQUEST, requestBody);
		Map<String, String> headerValues = ProjectUtil.getDefaultHeaders();
		if (StringUtils.isNotEmpty(userRegistration.getUserAuthToken())) {
			headerValues.put(Constants.X_AUTH_TOKEN, userRegistration.getUserAuthToken());
		}
		try {
			Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPost(
					props.getSbUrl() + props.getLmsUserCreatePath(), request, headerValues);
			if (readData != null && !Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
				Map<String, Object> params = (Map<String, Object>) readData.get(Constants.PARAMS);
				if (!MapUtils.isEmpty(params)) {
					return (String) params.get(Constants.ERROR_MESSAGE);
				}
			} else if (readData != null && Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
				Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
				userRegistration.setUserId((String) result.get(Constants.USER_ID));
				return updateBulkUploadUser(userRegistration);
			}
		} catch (Exception e) {
			logger.error("Failed to run the create user flow. UserRegCode : " + userRegistration.getRegistrationCode(),
					e);
		}
		return Constants.BULK_USER_CREATE_API_FAILED;
	}

	@Override
	public String updateBulkUploadUser(UserRegistration userRegistration) {
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(Constants.USER_ID, userRegistration.getUserId());
		Map<String, Object> profileDetails = new HashMap<String, Object>();
		profileDetails.put(Constants.MANDATORY_FIELDS_EXISTS, false);
		Map<String, Object> employementDetails = new HashMap<String, Object>();
		employementDetails.put(Constants.DEPARTMENTNAME, userRegistration.getOrgName());
		if (StringUtils.isNotEmpty(userRegistration.getEmployeeId())) {
			employementDetails.put(Constants.EMPLOYEE_CODE, userRegistration.getEmployeeId());
		}
		profileDetails.put(Constants.EMPLOYMENTDETAILS, employementDetails);
		Map<String, Object> personalDetails = new HashMap<String, Object>();
		personalDetails.put(Constants.FIRSTNAME.toLowerCase(), userRegistration.getFirstName());
		personalDetails.put(Constants.PRIMARY_EMAIL, userRegistration.getEmail());
		personalDetails.put(Constants.MOBILE, userRegistration.getPhone());
		personalDetails.put(Constants.PHONE_VERIFIED, true);
		if (StringUtils.isNotEmpty(userRegistration.getDob())) {
			personalDetails.put(Constants.DOB, userRegistration.getDob());
		}
		if (StringUtils.isNotEmpty(userRegistration.getCategory())) {
			personalDetails.put(Constants.CATEGORY, userRegistration.getCategory());
		}
		if (StringUtils.isNotEmpty(userRegistration.getDomicileMedium())) {
			personalDetails.put(Constants.DOMICILE_MEDIUM, userRegistration.getDomicileMedium());
		}
		if (StringUtils.isNotEmpty(userRegistration.getPincode())) {
			personalDetails.put(Constants.PINCODE, userRegistration.getPincode());
		}
		if (StringUtils.isNotEmpty(userRegistration.getGender())) {
			personalDetails.put(Constants.GENDER, userRegistration.getGender());
		}
		profileDetails.put(Constants.PERSONAL_DETAILS, personalDetails);
		Map<String, Object> professionDetailObj = new HashMap<String, Object>();
		professionDetailObj.put(Constants.ORGANIZATION_TYPE, Constants.GOVERNMENT);
		if (StringUtils.isNotEmpty(userRegistration.getPosition())) {
			professionDetailObj.put(Constants.DESIGNATION, userRegistration.getPosition());
		}
		if (!StringUtils.isEmpty(userRegistration.getGroup())) {
			professionDetailObj.put(Constants.GROUP, userRegistration.getGroup());
		}
		List<Map<String, Object>> professionalDetailsList = new ArrayList<Map<String, Object>>();
		professionalDetailsList.add(professionDetailObj);
		profileDetails.put(Constants.PROFESSIONAL_DETAILS, professionalDetailsList);
		Map<String, Object> additionalProperties = new HashMap<String, Object>();
		if (!CollectionUtils.isEmpty(userRegistration.getTag())) {
			additionalProperties.put(Constants.TAG, userRegistration.getTag());
		}
		if (!StringUtils.isEmpty(userRegistration.getExternalSystemId())) {
			additionalProperties.put(Constants.EXTERNAL_SYSTEM_ID, userRegistration.getExternalSystemId());
		}
		if (!StringUtils.isEmpty(userRegistration.getExternalSystem())) {
			additionalProperties.put(Constants.EXTERNAL_SYSTEM, userRegistration.getExternalSystem());
		}
		profileDetails.put(Constants.ADDITIONAL_PROPERTIES, additionalProperties);
		profileDetails.put(Constants.VERIFIED_KARMAYOGI, false);
		profileDetails.put(Constants.MANDATORY_FIELDS_EXISTS, false);
		if (StringUtils.isNotBlank(userRegistration.getGroup())
				&& StringUtils.isNotBlank(userRegistration.getPosition())) {
			profileDetails.put(Constants.PROFILE_STATUS, Constants.VERIFIED);
		} else {
			profileDetails.put(Constants.PROFILE_STATUS, Constants.NOT_VERIFIED);
		}
		requestBody.put(Constants.PROFILE_DETAILS, profileDetails);
		request.put(Constants.REQUEST, requestBody);
		Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPatch(
				props.getSbUrl() + props.getLmsUserUpdatePrivatePath(), request, ProjectUtil.getDefaultHeaders());
		if (readData != null && !Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
			Map<String, Object> params = (Map<String, Object>) readData.get(Constants.PARAMS);
			if (!MapUtils.isEmpty(params)) {
				return (String) params.get(Constants.ERROR_MESSAGE);
			}
		} else if (readData != null && Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
			//	if (getActivationLink(userRegistration)) {
			return (String) readData.get(Constants.RESPONSE_CODE);
			//	}
		}
		return Constants.BULK_USER_UPDATE_API_FAILED;
	}

	/**
	 * @return - The user details after processing the elastic search query.
	 * @throws IOException - will throw an IO Exception if any occurs.
	 */
	@Override
	public Map<String, Map<String, Object>> getUserDetailsFromES(List<String> userIdList, List<String> userFields) throws IOException {
		int index = 0;
		int size = 500;
		long userCount = 0L;
		boolean isCompleted = false;
		SearchSourceBuilder sourceBuilder = null;
		List<Map<String, Object>> resultArray = new ArrayList<>();
		Map<String, Map<String, Object>> backupUserInfoMap = new HashMap<>();
		Map<String, Object> result;
		Map<String, Map<String, Object>> userInfoMap = new HashMap<>();
		final BoolQueryBuilder finalQuery = QueryBuilders.boolQuery();
		finalQuery.must(QueryBuilders.termQuery("status.raw", 1));
		finalQuery.must(QueryBuilders.termsQuery("identifier.raw", userIdList));
		do {
			sourceBuilder = new SearchSourceBuilder().query(finalQuery).from(index).size(size);
			sourceBuilder.fetchSource(userFields.toArray(new String[0]), new String[]{});
			SearchResponse searchResponse = indexerService.getEsResult(serverConfig.getSbEsUserProfileIndex(),
					serverConfig.getSbEsProfileIndexType(), sourceBuilder, true);
			if (index == 0) {
				userCount = searchResponse.getHits().getTotalHits();
				logger.info(String.format("Number of users in ES index : %s", userCount));
			}
			for (SearchHit hit : searchResponse.getHits()) {
				result = hit.getSourceAsMap();
				resultArray.add(result);
			}
			processUserDetails(resultArray, userInfoMap);
			backupUserInfoMap.putAll(userInfoMap);
			resultArray.clear();

			index = (int) Math.min(userCount, index + size);
			if (index == userCount) {
				isCompleted = true;
			}
		} while (!isCompleted);
		return backupUserInfoMap;
	}

	/**
	 * This method is responsible for processing and setting the data in the collection object.
	 *
	 * @param userMapList - Contains the data fetched from the elastic search.
	 * @param userInfoMap - collection object where the data is set after the processing.
	 */
	private void processUserDetails(List<Map<String, Object>> userMapList,
									Map<String, Map<String, Object>> userInfoMap) throws IOException{
		for (Map<String, Object> user : userMapList) {
			Map<String, Object> userInfo = new HashMap<>();
			userInfo.put(Constants.USER_ID, (String) user.get(Constants.USER_ID));
			userInfo.put(Constants.FIRSTNAME, (String) user.get(Constants.USER_FIRST_NAME));
			userInfo.put(Constants.ROOT_ORG_ID, (String) user.get(Constants.ROOT_ORG_ID));
			userInfo.put(Constants.CHANNEL, (String) user.get(Constants.CHANNEL));
			userInfo.put(Constants.DESIGNATION, (String) user.get(Constants.PROFILE_DETAILS_DESIGNATION));
			//userInfo.put(Constants.DEPARTMENTNAME, (String) user.get(Constants.EMPLOYMENT_DETAILS_DEPARTMENT_NAME));
			JsonNode jsonNode = objectMapper.convertValue(user, JsonNode.class);
			JsonNode primaryEmailNode = jsonNode.at("/profileDetails/personalDetails/primaryEmail");
			String primaryEmail = primaryEmailNode.asText();

			JsonNode mobileNode = jsonNode.at("/profileDetails/personalDetails/mobile");
			String mobile = mobileNode.asText();
			if (StringUtils.isNotBlank(primaryEmail)) {
				userInfo.put(Constants.EMAIL, primaryEmail);
			}
			if (StringUtils.isNotBlank((mobile))) {
				userInfo.put(Constants.PHONE, mobile);
			}
			userInfoMap.put((String)userInfo.get(Constants.USER_ID), userInfo);
		}
	}

	@Override
	public Map<String, Object> getUsersDataFromLookup(String email, String authToken) {
		Map<String, String> header = new HashMap<>();
		if (StringUtils.isNotEmpty(authToken)) {
			header.put(Constants.AUTH_TOKEN, authToken);
		}
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(Constants.KEY, Constants.EMAIL);
		requestBody.put(Constants.VALUE, email);
		request.put(Constants.REQUEST, requestBody);
		Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPost(
				props.getSbUrl() + props.getLmsUserLookupPath(), request, ProjectUtil.getDefaultHeaders());
		if (readData != null && Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
			Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
			if (!ObjectUtils.isEmpty(result)) {
				List<Map<String, Object>> responseMap = (List<Map<String, Object>>) result.get(Constants.RESPONSE);
				if (!CollectionUtils.isEmpty(responseMap)) {
					return responseMap.get(0);
				}
			}
		}
		return new HashMap<>();
	}

	@Override
	public SBApiResponse recommendContent(String authUserToken, Map<String, Object> request) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.USER_CONTENT_RECOMMENDATION);
		try {
			String userId = validateAuthTokenAndFetchUserId(authUserToken);
			String errorMsg = validateRequest(request, userId);
			if (!StringUtils.isEmpty(errorMsg)) {
				response.getParams().setErrmsg(errorMsg);
				response.getParams().setStatus(Constants.FAILED);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				return response;
			}
			List<String> fields = Arrays.asList(Constants.FIRSTNAME);
			Map<String, Object> propertiesMap = new HashMap<>();
			propertiesMap.put(Constants.ID, userId);
			List<Map<String, Object>> userDetailsResult = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
					Constants.SUNBIRD_KEY_SPACE_NAME, Constants.USER, propertiesMap, fields);
			if (CollectionUtils.isEmpty(userDetailsResult)) {
				response.getParams().setStatus(Constants.FAILED);
				response.getParams().setErrmsg("User Does not Exist");
				response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
				return response;
			}
			Map<String, Object> userDetails = userDetailsResult.get(0);
			String name = (String) userDetails.get(Constants.FIRSTNAME);

			Map<String, Object> requestData = (Map<String, Object>) request.get(Constants.REQUEST);
			List<Map<String, Object>> recipientsList = (List<Map<String, Object>>) requestData.get(Constants.RECIPIENTS);
			List<String> emailList = new ArrayList<>();
			for (Map<String, Object> map : recipientsList) {
				emailList.add((String) map.get(Constants.EMAIL));
			}
			Map<String, Object> requestObject = new HashMap<>();
			Map<String, Object> req = new HashMap<>();
			Map<String, Object> filters = new HashMap<>();
			filters.put(Constants.PROFILE_DETAILS_PRIMARY_EMAIL, emailList);
			List<String> userFields = Arrays.asList(Constants.USER_ID, Constants.PROFILE_DETAILS);
			req.put(Constants.FILTERS, filters);
			req.put(Constants.FIELDS, userFields);
			requestObject.put(Constants.REQUEST, req);
			HashMap<String, String> headersValue = new HashMap<>();
			headersValue.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
			StringBuilder url = new StringBuilder(props.getSbUrl()).append(props.getUserSearchEndPoint());
			Map<String, Object> searchProfileApiResp = outboundRequestHandlerService.fetchResultUsingPost(url.toString(), requestObject, headersValue);
			List<String> emailResponseList = new ArrayList<>();
			if (searchProfileApiResp != null
					&& Constants.OK.equalsIgnoreCase((String) searchProfileApiResp.get(Constants.RESPONSE_CODE))) {
				Map<String, Object> map = (Map<String, Object>) searchProfileApiResp.get(Constants.RESULT);
				Map<String, Object> resp = (Map<String, Object>) map.get(Constants.RESPONSE);
				List<Map<String, Object>> contents = (List<Map<String, Object>>) resp.get(Constants.CONTENT);
				for (Map<String, Object> content : contents) {
					Map<String, Object> profileDetails = (Map<String, Object>) content.get(Constants.PROFILE_DETAILS);
					Map<String, Object> personalDetails = (Map<String, Object>) profileDetails.get(Constants.PERSONAL_DETAILS);
					String email = (String) personalDetails.get(Constants.PRIMARY_EMAIL);
					emailResponseList.add(email);
				}
			}
			logger.info("emailist : " +  emailResponseList);
			StringBuilder link = new StringBuilder();
			if(requestData.containsKey(Constants.COURSE_LINK) && StringUtils.isNotEmpty((String)requestData.get(Constants.COURSE_LINK))){
				link.append(serverConfig.getDomainUrl()).append((String)requestData.get(Constants.COURSE_LINK));
			}else {
				link.append(serverConfig.getCourseLinkUrl()).append(requestData.get(Constants.COURSE_ID)).append("/").append(Constants.OVERVIEW);
			}

			if (!emailResponseList.isEmpty()) {
				Map<String, Object> mailNotificationDetails = new HashMap<>();
				mailNotificationDetails.put(Constants.RECIPIENT_EMAILS, emailResponseList);
				mailNotificationDetails.put(Constants.COURSE_NAME, requestData.get(Constants.COURSE_NAME));
				mailNotificationDetails.put(Constants.SUBJECT, name + Constants.RECOMMEND_CONTENT_SUBJECT.replace(Constants.PRIMARY_CATEGORY_TAG, (String) requestData.get(Constants.PRIMARY_CATEGORY)));
				mailNotificationDetails.put(Constants.LINK, link.toString());
				mailNotificationDetails.put(Constants.COURSE_POSTER_IMAGE_URL, requestData.get(Constants.COURSE_POSTER_IMAGE_URL));
				mailNotificationDetails.put(Constants.COURSE_PROVIDER, requestData.get(Constants.COURSE_PROVIDER));
				mailNotificationDetails.put(Constants.USER_ID, userId);
				mailNotificationDetails.put(Constants.USER, name);
				mailNotificationDetails.put(Constants.PRIMARY_CATEGORY, requestData.get(Constants.PRIMARY_CATEGORY));
				sendNotificationToRecipients(mailNotificationDetails);
			}
		} catch (Exception e) {
			String errMsg = "Error while performing operation." + e.getMessage();
			logger.error(errMsg, e);
			response.getParams().setErrmsg(errMsg);
			response.getParams().setStatus(Constants.FAILED);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	private String validateRequest(Map<String, Object> request, String userId) {
		StringBuffer str = new StringBuffer();
		List<String> errList = new ArrayList<String>();
		Map<String, Object> requestData = (Map<String, Object>) request.get(Constants.REQUEST);
		if (StringUtils.isBlank(userId)) {
			str.append(Constants.USER_ID_DOESNT_EXIST);
			return str.toString();
		}
		if (ObjectUtils.isEmpty(requestData)) {
			str.append("Request object is empty.");
			return str.toString();
		}
		if (StringUtils.isEmpty((String) requestData.get(Constants.COURSE_ID))) {
			errList.add(Constants.COURSE_ID);
		}
		if (StringUtils.isEmpty((String) requestData.get(Constants.COURSE_NAME))) {
			errList.add(Constants.COURSE_NAME);
		}
		if (StringUtils.isEmpty((String) requestData.get(Constants.COURSE_POSTER_IMAGE_URL))) {
			errList.add(Constants.COURSE_POSTER_IMAGE_URL);
		}
		if (StringUtils.isEmpty((String) requestData.get(Constants.COURSE_PROVIDER))) {
			errList.add(Constants.COURSE_PROVIDER);
		}
		if (StringUtils.isEmpty((String) requestData.get(Constants.PRIMARY_CATEGORY))) {
			errList.add(Constants.PRIMARY_CATEGORY);
		}
		if (ObjectUtils.isEmpty(requestData.get(Constants.RECIPIENTS))) {
			errList.add(Constants.RECIPIENTS);
		}
		if (!errList.isEmpty()) {
			str.append("Failed to Due To Missing Params - ").append(errList).append(".");
		} else {
			List<Map<String, Object>> listOfMaps = (List<Map<String, Object>>) requestData.get(Constants.RECIPIENTS);
			if (listOfMaps.size() <= 30) {
				for (Map<String, Object> map : listOfMaps) {
					if (!map.containsKey(Constants.EMAIL) || StringUtils.isBlank((String) map.get(Constants.EMAIL))) {
						str.append("Failed due to missing or empty recipient's email.");
						break;
					}
				}
			} else {
				str.append("Failed due to Maximum email limit reached");
			}
		}
		return str.toString();
	}

	private String validateAuthTokenAndFetchUserId(String authUserToken) {
		return accessTokenValidator.fetchUserIdFromAccessToken(authUserToken);
	}

	private void sendNotificationToRecipients(Map<String, Object> mailNotificationDetails) {
		Map<String, Object> params = new HashMap<>();
		NotificationAsyncRequest notificationRequest = new NotificationAsyncRequest();
		Map<String, Object> action = new HashMap<>();
		Map<String, Object> templ = new HashMap<>();
		Map<String, Object> usermap = new HashMap<>();
		params.put(Constants.COURSE_NAME, mailNotificationDetails.get(Constants.COURSE_NAME));
		params.put(Constants.COURSE_POSTER_IMAGE_URL, mailNotificationDetails.get(Constants.COURSE_POSTER_IMAGE_URL));
		params.put(Constants.COURSE_PROVIDER, mailNotificationDetails.get(Constants.COURSE_PROVIDER));
		params.put(Constants.LINK, mailNotificationDetails.get(Constants.LINK));
		params.put(Constants.PRIMARY_CATEGORY, mailNotificationDetails.get(Constants.PRIMARY_CATEGORY));
		Template template = new Template(constructEmailTemplate(props.getRecommendContentTemplate(), params), props.getRecommendContentTemplate(), params);
		usermap.put(Constants.ID, mailNotificationDetails.get(Constants.USER_ID));
		usermap.put(Constants.TYPE, Constants.USER);
		action.put(Constants.TEMPLATE, templ);
		action.put(Constants.TYPE, Constants.EMAIL);
		action.put(Constants.CATEGORY, Constants.EMAIL);
		action.put(Constants.CREATED_BY, usermap);
		Config config = new Config();
		config.setSubject((String) mailNotificationDetails.get(Constants.SUBJECT));
		config.setSender(props.getSupportEmail());
		templ.put(Constants.TYPE, Constants.EMAIL);
		templ.put(Constants.DATA, template.getData());
		templ.put(Constants.ID, template.getId());
		templ.put(Constants.PARAMS, params);
		templ.put(Constants.CONFIG, config);
		notificationRequest.setType(Constants.EMAIL);
		notificationRequest.setPriority(1);
		notificationRequest.setIds((List<String>) mailNotificationDetails.get(Constants.RECIPIENT_EMAILS));
		notificationRequest.setAction(action);

		Map<String, Object> req = new HashMap<>();
		Map<String, List<NotificationAsyncRequest>> notificationMap = new HashMap<>();
		notificationMap.put(Constants.NOTIFICATIONS, Collections.singletonList(notificationRequest));
		req.put(Constants.REQUEST, notificationMap);
		sendNotification(req);
	}

	private void sendNotification(Map<String, Object> request) {
		StringBuilder builder = new StringBuilder();
		builder.append(props.getNotifyServiceHost()).append(props.getNotifyServicePathAsync());
		try {
			Map<String, Object> response = outboundRequestHandlerService.fetchResultUsingPost(builder.toString(), request, null);
			logger.debug("The email notification is successfully sent, response is: " + response);
		} catch (Exception e) {
			logger.error("Exception while posting the data in notification service: ", e);
		}
	}

	private String constructEmailTemplate(String templateName, Map<String, Object> params) {
		String replacedHTML = new String();
		try {
			Map<String, Object> propertyMap = new HashMap<>();
			propertyMap.put(Constants.NAME, templateName);
			List<Map<String, Object>> templateMap = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_EMAIL_TEMPLATE, propertyMap, Collections.singletonList(Constants.TEMPLATE));
			String htmlTemplate = templateMap.stream()
					.findFirst()
					.map(template -> (String) template.get(Constants.TEMPLATE))
					.orElse(null);
			VelocityEngine velocityEngine = new VelocityEngine();
			velocityEngine.init();
			VelocityContext context = new VelocityContext();
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				context.put(entry.getKey(), entry.getValue());
			}
			StringWriter writer = new StringWriter();
			velocityEngine.evaluate(context, writer, "HTMLTemplate", htmlTemplate);
			replacedHTML = writer.toString();
		} catch (Exception e) {
			logger.error("Unable to create template ", e);
		}
		return replacedHTML;
	}

	@Override
	public Map<String, Object> getUserDetails(String key, String value) {
		// request body
		SunbirdApiRequest requestObj = new SunbirdApiRequest();
		Map<String, Object> reqMap = new HashMap<>();
		reqMap.put(Constants.FILTERS, new HashMap<String, Object>() {
			{
				put(key, value);
			}
		});
		requestObj.setRequest(reqMap);

		HashMap<String, String> headersValue = new HashMap<>();
		headersValue.put(Constants.CONTENT_TYPE, "application/json");
		headersValue.put(Constants.AUTHORIZATION, props.getSbApiKey());

		try {
			String url = props.getSbUrl() + props.getUserSearchEndPoint();

			Map<String, Object> response = outboundRequestHandlerService.fetchResultUsingPost(url, requestObj,
					headersValue);
			if (response != null && "OK".equalsIgnoreCase((String) response.get("responseCode"))) {
				Map<String, Object> map = (Map<String, Object>) response.get("result");
				if (map.get("response") != null) {
					Map<String, Object> responseObj = (Map<String, Object>) map.get("response");
					if (MapUtils.isNotEmpty(responseObj)) {
						List<Map<String, Object>> content = (List<Map<String, Object>>)responseObj.get("content");
						if (org.apache.commons.collections.CollectionUtils.isNotEmpty(content)) {
							return content.get(0);
						}
					}

				}
			}
		} catch (Exception e) {
			throw new ApplicationLogicError("Sunbird Service ERROR: ", e);
		}
		return null;
	}

	@Override
	public boolean validateGender(String gender) {
		List<String> genderValues = serverConfig.getBulkUploadGenderValue();
		return genderValues != null && genderValues.contains(gender);
	}

	@Override
	public boolean validateCategory(String category) {
		List<String> categoryValues = serverConfig.getBulkUploadCategoryValue();
		return categoryValues != null && categoryValues.contains(category);
	}
}
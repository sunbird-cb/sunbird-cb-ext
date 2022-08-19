package org.sunbird.profile.service;

import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRespParam;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.IndexerService;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.org.service.ExtendedOrgService;
import org.sunbird.user.service.UserUtilityServiceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@SuppressWarnings({ "unchecked", "serial" })
public class ProfileServiceImpl implements ProfileService {

	@Autowired
	CbExtServerProperties serverConfig;

	@Autowired
	OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	@Autowired
	RedisCacheMgr redisCacheMgr;

	@Autowired
	UserUtilityServiceImpl userUtilityService;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	IndexerService indexerService;

	@Autowired
	CbExtServerProperties props;

	@Autowired
	CassandraOperation cassandraOperation;

	@Autowired
	ExtendedOrgService extOrgService;

	private CbExtLogger log = new CbExtLogger(getClass().getName());

	@Override
	public SBApiResponse profileUpdate(Map<String, Object> request, String userToken, String authToken)
			throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_PROFILE_UPDATE);
		SunbirdApiRespParam resultObject = new SunbirdApiRespParam();
		try {
			Map<String, Object> requestData = (Map<String, Object>) request.get(Constants.REQUEST);
			if (!validateRequest(requestData)) {
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				response.getParams().setStatus(Constants.FAILED);
				return response;
			}

			String userId = (String) requestData.get(Constants.USER_ID);
			Map<String, Object> profileDetailsMap = (Map<String, Object>) requestData.get(Constants.PROFILE_DETAILS);
			List<String> approvalFieldList = approvalFields(authToken, userToken);
			Map<String, Object> transitionData = new HashMap<>();
			for (String approvalList : approvalFieldList) {
				if (profileDetailsMap.containsKey(approvalList)) {
					transitionData.put(approvalList, profileDetailsMap.get(approvalList));
					profileDetailsMap.remove(approvalList);
				}
			}
			Map<String, Object> responseMap = userUtilityService.getUsersReadData(userId, authToken, userToken);
			String deptName = (String) responseMap.get(Constants.CHANNEL);
			validateDepartment(deptName, profileDetailsMap);
			Map<String, Object> existingProfileDetails = (Map<String, Object>) responseMap
					.get(Constants.PROFILE_DETAILS);
			StringBuilder url = new StringBuilder();
			HashMap<String, String> headerValues = new HashMap<>();
			headerValues.put(Constants.AUTH_TOKEN, authToken);
			headerValues.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
			Map<String, Object> workflowResponse = new HashMap<>();
			Map<String, Object> updateResponse = new HashMap<>();
			if (!profileDetailsMap.isEmpty()) {
				List<String> listOfChangedDetails = new ArrayList<>();
				for (String keys : profileDetailsMap.keySet()) {
					listOfChangedDetails.add(keys);
				}

				for (String changedObj : listOfChangedDetails) {
					if (profileDetailsMap.get(changedObj) instanceof ArrayList) {
						existingProfileDetails.put(changedObj, profileDetailsMap.get(changedObj));
					} else {
						if (existingProfileDetails.containsKey(changedObj)) {
							Map<String, Object> existingProfileChild = (Map<String, Object>) existingProfileDetails
									.get(changedObj);
							Map<String, Object> requestedProfileChild = (Map<String, Object>) profileDetailsMap
									.get(changedObj);
							for (String childKey : requestedProfileChild.keySet()) {
								existingProfileChild.put(childKey, requestedProfileChild.get(childKey));
							}
						} else {
							existingProfileDetails.put(changedObj, profileDetailsMap.get(changedObj));
						}
					}

					// Additional Condition for updating personal Details directly to user object
					if (Constants.PERSONAL_DETAILS.equalsIgnoreCase(changedObj)) {
						getModifiedPersonalDetails(profileDetailsMap.get(changedObj), requestData);
					}
				}
				Map<String, Object> updateRequestValue = requestData;
				updateRequestValue.put(Constants.PROFILE_DETAILS, existingProfileDetails);
				Map<String, Object> updateRequest = new HashMap<>();
				updateRequest.put(Constants.REQUEST, updateRequestValue);

				url.append(serverConfig.getSbUrl()).append(serverConfig.getLmsUserUpdatePath());
				updateResponse = outboundRequestHandlerService.fetchResultUsingPatch(
						serverConfig.getSbUrl() + serverConfig.getLmsUserUpdatePath(), updateRequest, headerValues);
				if (updateResponse.get(Constants.RESPONSE_CODE).equals(Constants.OK)) {
					resultObject.setStatus(Constants.SUCCESS);
					response.getResult().put(Constants.PERSONAL_DETAILS, resultObject);
					response.getParams().setStatus(Constants.SUCCESS);
				} else {
					resultObject.setStatus(Constants.FAILED);
					response.getResult().put(Constants.PERSONAL_DETAILS, resultObject);
					response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
					response.getParams().setStatus(Constants.FAILED);
					return response;
				}
			}
			List<String> transitionList = new ArrayList<>();
			for (String key : transitionData.keySet()) {
				transitionList.add(key);
			}
			if (!transitionList.isEmpty()) {
				List<Map<String, Object>> finalTransitionList = new ArrayList<>();
				for (String listTransition : transitionList) {

					if (transitionData.get(listTransition) instanceof ArrayList) {
						List<Map<String, Object>> transList = (List<Map<String, Object>>) transitionData
								.get(listTransition);
						for (int j = 0; j < transList.size(); j++) {
							Map<String, Object> transData = new HashMap<>();
							transData = transList.get(j);
							Set<String> list = transData.keySet();
							String[] innerData = list.toArray(new String[list.size()]);

							for (int k = 0; k < innerData.length; k++) {
								Map<String, Object> dataRay = new HashMap<>();
								Map<String, Object> fromValue = new HashMap<>();
								Map<String, Object> toValue = new HashMap<>();
								toValue.put(innerData[k], transData.get(innerData[k]));
								if (existingProfileDetails.get(listTransition) instanceof ArrayList) {
									List<Map<String, Object>> readList = (List<Map<String, Object>>) existingProfileDetails
											.get(listTransition);
									Map<String, Object> readListData = readList.get(j);
									fromValue.put(innerData[k], readListData.get(innerData[k]));
									dataRay.put(Constants.OSID, readListData.get(Constants.OSID));
								}
								dataRay.put(Constants.FROM_VALUE, fromValue);
								dataRay.put(Constants.TO_VALUE, toValue);
								dataRay.put(Constants.FIELD_KEY, listTransition);
								finalTransitionList.add(dataRay);
							}
						}
					} else {
						Map<String, Object> transListObject = new HashMap<>();
						transListObject = (Map<String, Object>) transitionData.get(listTransition);
						Set<String> listObject = transListObject.keySet();
						String[] innerObjectData = listObject.toArray(new String[listObject.size()]);
						for (int k = 0; k < innerObjectData.length; k++) {
							Map<String, Object> updatedTransitionData = new HashMap<>();
							Map<String, Object> fromValue = new HashMap<>();
							Map<String, Object> toValue = new HashMap<>();
							toValue.put(innerObjectData[k], transListObject.get(innerObjectData[k]));
							Map<String, Object> readList = (Map<String, Object>) existingProfileDetails
									.get(listTransition);
							fromValue.put(innerObjectData[k], readList.get(innerObjectData[k]));
							updatedTransitionData.put(Constants.FROM_VALUE, fromValue);
							updatedTransitionData.put(Constants.TO_VALUE, toValue);
							updatedTransitionData.put(Constants.FIELD_KEY, listTransition);
							updatedTransitionData.put(Constants.OSID, readList.get(Constants.OSID));
							finalTransitionList.add(updatedTransitionData);
						}
					}
				}

				Map<String, Object> transitionRequests = new HashMap<>();
				transitionRequests.put(Constants.STATE, Constants.INITIATE);
				transitionRequests.put(Constants.ACTION, Constants.INITIATE);
				transitionRequests.put(Constants.USER_ID, userId);
				transitionRequests.put(Constants.APPLICATION_ID, userId);
				transitionRequests.put(Constants.ACTOR_USER_ID, userId);
				transitionRequests.put(Constants.SERVICE_NAME, Constants.PROFILE);
				transitionRequests.put(Constants.COMMENT, "");
				transitionRequests.put(Constants.WFID, "");
				transitionRequests.put(Constants.DEPT_NAME, deptName);
				transitionRequests.put(Constants.UPDATE_FIELD_VALUES, finalTransitionList);
				url = new StringBuilder();
				url.append(serverConfig.getWfServiceHost()).append(serverConfig.getWfServiceTransitionPath());
				headerValues.put("rootOrg", "igot");
				headerValues.put(Constants.ROOT_ORG_CONSTANT, Constants.IGOT);
				headerValues.put(Constants.ORG_CONSTANT, Constants.DOPT);
				headerValues.put(Constants.X_AUTH_TOKEN, userToken);
				workflowResponse = outboundRequestHandlerService.fetchResultUsingPost(
						serverConfig.getWfServiceHost() + serverConfig.getWfServiceTransitionPath(), transitionRequests,
						headerValues);

				Map<String, Object> resultValue = (Map<String, Object>) workflowResponse.get(Constants.RESULT);
				if (resultValue.get(Constants.STATUS).equals(Constants.OK)) {
					resultObject.setStatus(Constants.SUCCESS);
					response.getResult().put(Constants.TRANSITION_DETAILS, resultObject);
					response.getParams().setStatus(Constants.SUCCESS);
				} else {
					resultObject.setStatus(Constants.FAILED);
					resultObject.setErrmsg((String) resultValue.get(Constants.MESSAGE));
					response.getResult().put(Constants.TRANSITION_DETAILS, resultObject);
				}
			}
			response.setResponseCode(HttpStatus.OK);
		} catch (Exception e) {
			log.error(e);
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErr(e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return response;
	}

	@Override
	public SBApiResponse orgProfileUpdate(Map<String, Object> request) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.ORG_PROFILE_UPDATE);
		Map<String, Object> requestData = (Map<String, Object>) request.get(Constants.REQUEST);
		String errMsg = validateOrgProfilePayload(requestData);
		if (StringUtils.isBlank(errMsg)) {
			try {
				String orgId = (String) requestData.get(Constants.ORG_ID);
				Map<String, Object> esOrgProfileMap = getOrgProfileForOrgId(orgId);
				boolean isOrgProfileExist = true;
				if (ObjectUtils.isEmpty(esOrgProfileMap)) {
					isOrgProfileExist = false;
					esOrgProfileMap = new HashMap<>();
				}

				Map<String, Object> orgProfileDetailsMap = (Map<String, Object>) requestData
						.get(Constants.PROFILE_DETAILS);
				for (String keys : orgProfileDetailsMap.keySet()) {
					esOrgProfileMap.put(keys, orgProfileDetailsMap.get(keys));
				}
				RestStatus status = null;
				if (isOrgProfileExist) {
					status = indexerService.updateEntity(serverConfig.getOrgOnboardingIndex(),
							serverConfig.getEsProfileIndexType(), orgId, esOrgProfileMap);
				} else {
					status = indexerService.addEntity(serverConfig.getOrgOnboardingIndex(),
							serverConfig.getEsProfileIndexType(), orgId, esOrgProfileMap);
				}
				if (status.equals(RestStatus.CREATED) || status.equals(RestStatus.OK)) {
					response.setResponseCode(HttpStatus.ACCEPTED);
					Map<String, Object> resultMap = new HashMap<String, Object>();
					resultMap.put(Constants.ORG_ID, orgId);
					resultMap.put(Constants.PROFILE_DETAILS, esOrgProfileMap);
					response.getResult().put(Constants.RESULT, resultMap);
				} else {
					response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
					response.getParams().setErrmsg("Failed to add details to ES Service");
				}
			} catch (Exception e) {
				log.error(e);
				errMsg = String.format("Failed to process org profile update request. Exception: %s", e.getMessage());
				log.warn(errMsg);
			}
		}
		if (StringUtils.isNotBlank(errMsg)) {
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
		}

		return response;
	}

	@Override
	public SBApiResponse orgProfileRead(String orgId) throws Exception {
		SBApiResponse response = createDefaultResponse(Constants.ORG_ONBOARDING_PROFILE_RETRIEVE_API);
		Map<String, Object> orgProfile = getOrgProfileForOrgId(orgId);
		if (!ObjectUtils.isEmpty(orgProfile)) {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(Constants.ORG_ID, orgId);
			resultMap.put(Constants.PROFILE_DETAILS, orgProfile);
			response.getResult().put(Constants.RESULT, resultMap);
		} else {
			response.getParams().setStatus(HttpStatus.NOT_FOUND.name());
			response.getParams().setErrmsg(String.format("Org details not found for Id :  %s", orgId));
			response.setResponseCode(HttpStatus.NOT_FOUND);
		}

		return response;
	}

	public SBApiResponse userBasicInfo(String userId) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_USER_BASIC_INFO);
		try {
			Map<String, Object> userData = userUtilityService.getUsersReadData(userId, StringUtils.EMPTY,
					StringUtils.EMPTY);
			if (ObjectUtils.isEmpty(userData)) {
				response.getParams().setErrmsg("User READ API Failed.");
				response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
				response.getParams().setStatus(Constants.FAILED);
				return response;
			}
			// Read Custodian Org Id and Channel
			String custodianOrgId = getCustodianOrgId();
			String custodianOrgChannel = getCustodianOrgChannel();
			if (StringUtils.isEmpty(custodianOrgId) || StringUtils.isEmpty(custodianOrgChannel)) {
				response.getParams().setErrmsg("Failed to read Custodian Org values");
				response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
				response.getParams().setStatus(Constants.FAILED);
				return response;
			}

			Map<String, Object> responseMap = new HashMap<String, Object>();
			responseMap.put(Constants.IS_UPDATE_REQUIRED, false);

			if (custodianOrgChannel.equalsIgnoreCase((String) userData.get(Constants.CHANNEL))
					&& custodianOrgId.equalsIgnoreCase((String) userData.get(Constants.ROOT_ORG_ID))) {
				// User has custodian Values, check for profile.
				Map<String, Object> profileData = (Map<String, Object>) userData.get(Constants.PROFILE_DETAILS);
				List<Map<String, Object>> userRole = (List<Map<String, Object>>) profileData.get(Constants.USER_ROLES);
				if (CollectionUtils.isEmpty(userRole)) {
					responseMap.put(Constants.IS_UPDATE_REQUIRED, true);
				}

				// Get Email from personalDetails
				if (profileData.containsKey(Constants.PERSONAL_DETAILS)) {
					Map<String, Object> personalDetail = (Map<String, Object>) profileData
							.get(Constants.PERSONAL_DETAILS);
					responseMap.put(Constants.EMAIL, personalDetail.get(Constants.PRIMARY_EMAIL));
				}

				responseMap.put(Constants.FIRSTNAME, userData.get(Constants.FIRSTNAME));
				responseMap.put(Constants.LASTNAME, userData.get(Constants.LASTNAME));
				responseMap.put(Constants.ROLES, userData.get(Constants.ROLES));
				responseMap.put(Constants.ROOT_ORG_ID, userData.get(Constants.ROOT_ORG_ID));
				responseMap.put(Constants.CHANNEL, userData.get(Constants.CHANNEL));
				responseMap.put(Constants.USER_ID, userData.get(Constants.USER_ID));
			}

			response.getResult().put(Constants.RESPONSE, responseMap);
		} catch (Exception err) {
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			response.getParams().setErrmsg(err.getMessage());
			response.getParams().setStatus(Constants.FAILED);
		}
		return response;
	}

	public SBApiResponse userBasicProfileUpdate(Map<String, Object> request) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_USER_BASIC_PROFILE_UPDATE);
		String errMsg = validateBasicProfilePayload(request);
		if (StringUtils.isNotBlank(errMsg)) {
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			response.getParams().setErrmsg(errMsg);
			response.getParams().setStatus(Constants.FAILED);
			return response;
		}
		try {
			Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
			errMsg = createOrgIfRequired(requestBody);
		} catch (Exception e) {
			log.error(e);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			response.getParams().setErrmsg(e.getMessage());
			response.getParams().setStatus(Constants.FAILED);
		}
		if (!StringUtils.isEmpty(errMsg)) {
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			response.getParams().setErrmsg(errMsg);
			response.getParams().setStatus(Constants.FAILED);
		}
		return response;
	}

	public List<String> approvalFields(String authToken, String userToken) {

		Map<String, Object> approvalFieldsCache = (Map<String, Object>) mapper
				.convertValue(redisCacheMgr.getCache(Constants.PROFILE_UPDATE_FIELDS), Map.class);

		if (!ObjectUtils.isEmpty(approvalFieldsCache)) {
			Map<String, Object> approvalResult = (Map<String, Object>) approvalFieldsCache.get(Constants.RESULT);
			Map<String, Object> approvalResponse = (Map<String, Object>) approvalResult.get(Constants.RESPONSE);
			String value = (String) approvalResponse.get(Constants.VALUE);
			List<String> approvalValues = new ArrayList<>();
			approvalValues.add(value);
			return approvalValues;
		} else {
			Map<String, String> header = new HashMap<>();
			header.put(Constants.AUTH_TOKEN, authToken);
			header.put(Constants.X_AUTH_TOKEN, userToken);
			Map<String, Object> approvalData = (Map<String, Object>) outboundRequestHandlerService
					.fetchUsingGetWithHeadersProfile(serverConfig.getSbUrl() + serverConfig.getLmsSystemSettingsPath(),
							header);
			Map<String, Object> approvalResult = (Map<String, Object>) approvalData.get(Constants.RESULT);
			Map<String, Object> approvalResponse = (Map<String, Object>) approvalResult.get(Constants.RESPONSE);
			String value = (String) approvalResponse.get(Constants.VALUE);
			String strArray[] = value.split(" ");
			List<String> approvalValues = Arrays.asList(strArray);
			return approvalValues;
		}
	}

	public void validateDepartment(String existingDept, Map<String, Object> requestProfile) throws Exception {
		if (requestProfile.containsKey(Constants.EMPLOYMENTDETAILS)) {
			Map<String, Object> empDetails = (Map<String, Object>) requestProfile.get(Constants.EMPLOYMENTDETAILS);
			String requestDeptName = (String) empDetails.get(Constants.DEPARTMENTNAME);
			if (!existingDept.equalsIgnoreCase(requestDeptName)) {
				throw new Exception("User belongs to Dept: " + existingDept + ". Can not update Dept Name to : "
						+ requestDeptName + ". Request Admin to migrate Dept first.");
			}
		}
	}

	public boolean validateRequest(Map<String, Object> requestBody) {
		if (!(ObjectUtils.isEmpty(requestBody.get(Constants.USER_ID)))
				&& !(ObjectUtils.isEmpty(requestBody.get(Constants.PROFILE_DETAILS)))) {
			return true;
		} else {
			return false;
		}
	}

	private void getModifiedPersonalDetails(Object personalDetailsObj, Map<String, Object> updatedRequest) {
		try {
			Map<String, Object> personalDetailsMap = (Map<String, Object>) personalDetailsObj;
			if (!ObjectUtils.isEmpty(personalDetailsMap)) {
				for (String paramName : personalDetailsMap.keySet()) {
					if (Constants.FIRST_NAME_LOWER_CASE.equalsIgnoreCase(paramName)) {
						updatedRequest.put(Constants.FIRSTNAME, (String) personalDetailsMap.get(paramName));
					} else if (Constants.SURNAME.equalsIgnoreCase(paramName)) {
						updatedRequest.put(Constants.FIRSTNAME, (String) personalDetailsMap.get(paramName));
					}
				}
			}
		} catch (Exception e) {
			log.error(e);
			log.warn("Failed to process personalDetails object.");
		}
	}

	public Map<String, Object> getOrgProfileForOrgId(String registrationCode) {
		try {
			Map<String, Object> esObject = indexerService.readEntity(serverConfig.getOrgOnboardingIndex(),
					serverConfig.getEsProfileIndexType(), registrationCode);
			return esObject;
		} catch (Exception e) {
			log.error(e);
			log.warn(String.format("Exception in %s : %s", "getUserRegistrationDetails", e.getMessage()));
		}
		return null;
	}

	private String validateOrgProfilePayload(Map<String, Object> orgProfileInfo) {
		StringBuffer str = new StringBuffer();
		List<String> errList = new ArrayList<String>();
		if (StringUtils.isBlank((String) orgProfileInfo.get(Constants.ORG_ID))) {
			errList.add(Constants.ORG_ID);
		}
		if (ObjectUtils.isEmpty(orgProfileInfo.get(Constants.PROFILE_DETAILS))) {
			errList.add(Constants.PROFILE_DETAILS);
		}
		if (!errList.isEmpty()) {
			str.append("Failed to Register User Details. Missing Params - [").append(errList.toString()).append("]");
		}
		return str.toString();
	}

	private SBApiResponse createDefaultResponse(String api) {
		SBApiResponse response = new SBApiResponse();
		response.setId(api);
		response.setVer(Constants.API_VERSION_1);
		response.setParams(new SunbirdApiRespParam());
		response.getParams().setStatus(Constants.SUCCESS);
		response.setResponseCode(HttpStatus.OK);
		response.setTs(DateTime.now().toString());
		return response;
	}

	public String getCustodianOrgId() {
		String custodianOrgId = (String) redisCacheMgr.getCache(Constants.CUSTODIAN_ORG_ID);
		if (StringUtils.isEmpty(custodianOrgId)) {
			Map<String, Object> searchRequest = new HashMap<String, Object>();
			searchRequest.put(Constants.ID, Constants.CUSTODIAN_ORG_ID);

			List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(
					Constants.KEYSPACE_SUNBIRD, Constants.TABLE_SYSTEM_SETTINGS, searchRequest, null);
			if (CollectionUtils.isEmpty(existingDataList)) {
				Map<String, Object> data = existingDataList.get(0);
				custodianOrgId = (String) data.get(Constants.VALUE.toLowerCase());
			}
			redisCacheMgr.putCache(Constants.CUSTODIAN_ORG_ID, custodianOrgId);
		}
		return custodianOrgId;
	}

	public String getCustodianOrgChannel() {
		String custodianOrgChannel = (String) redisCacheMgr.getCache(Constants.CUSTODIAN_ORG_CHANNEL);
		if (StringUtils.isEmpty(custodianOrgChannel)) {
			Map<String, Object> searchRequest = new HashMap<String, Object>();
			searchRequest.put(Constants.ID, Constants.CUSTODIAN_ORG_CHANNEL);

			List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(
					Constants.KEYSPACE_SUNBIRD, Constants.TABLE_SYSTEM_SETTINGS, searchRequest, null);
			if (CollectionUtils.isNotEmpty(existingDataList)) {
				Map<String, Object> data = existingDataList.get(0);
				custodianOrgChannel = (String) data.get(Constants.VALUE.toLowerCase());
			}
			redisCacheMgr.putCache(Constants.CUSTODIAN_ORG_CHANNEL, custodianOrgChannel);
		}
		return custodianOrgChannel;
	}

	private String validateBasicProfilePayload(Map<String, Object> requestObj) {
		StringBuffer str = new StringBuffer();
		List<String> errList = new ArrayList<String>();

		if (ObjectUtils.isEmpty(requestObj.get(Constants.REQUEST))) {
			errList.add(Constants.REQUEST);
		} else {
			Map<String, Object> request = (Map<String, Object>) requestObj.get(Constants.REQUEST);
			List<String> keys = Arrays.asList(Constants.USER_ID, Constants.POSITION, Constants.CHANNEL,
					Constants.MAP_ID, Constants.ORGANIZATION_TYPE, Constants.ORGANIZATION_SUB_TYPE);
			for (String key : keys) {
				if (StringUtils.isBlank((String) request.get(key))) {
					errList.add(key);
				}
			}
		}
		if (!errList.isEmpty()) {
			str.append("Failed to Self Migrate User. Missing Params - [").append(errList.toString()).append("]");
		}

		return str.toString();
	}

	private Map<String, Object> getOrgCreateRequest(Map<String, Object> request) {
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(Constants.ORG_NAME, request.get(Constants.CHANNEL));
		requestBody.put(Constants.CHANNEL, request.get(Constants.CHANNEL));
		requestBody.put(Constants.SB_ROOT_ORG_ID, request.get(Constants.SB_ROOT_ORG_ID));
		requestBody.put(Constants.ORGANIZATION_TYPE, request.get(Constants.ORGANIZATION_TYPE));
		requestBody.put(Constants.ORGANIZATION_SUB_TYPE, request.get(Constants.ORGANIZATION_SUB_TYPE));
		requestBody.put(Constants.MAP_ID, request.get(Constants.MAP_ID));
		requestBody.put(Constants.IS_TENANT, true);
		Map<String, Object> newRequest = new HashMap<String, Object>();
		newRequest.put(Constants.REQUEST, requestBody);
		return newRequest;
	}

	private Map<String, Object> getUserSelfMigrateRequest(String userId, String channel) {
		Map<String, Object> requestBody = new HashMap<String, Object>() {
			{
				put(Constants.USER_ID, userId);
				put(Constants.CHANNEL, channel);
				put(Constants.SOFT_DELETE_OLD_ORG, true);
				put(Constants.NOTIFY_MIGRATION, false);
			}
		};
		Map<String, Object> request = new HashMap<String, Object>() {
			{
				put(Constants.REQUEST, requestBody);
			}
		};
		return request;
	}

	private String createOrgIfRequired(Map<String, Object> requestBody) {
		String errMsg = StringUtils.EMPTY;
		// Create the org if it's not already onboarded.
		if (StringUtils.isEmpty((String) requestBody.get(Constants.SB_ORG_ID))) {
			SBApiResponse orgResponse = extOrgService.createOrg(getOrgCreateRequest(requestBody), StringUtils.EMPTY);
			if (orgResponse.getResponseCode() == HttpStatus.OK) {
				String orgId = (String) orgResponse.getResult().get(Constants.ORGANIZATION_ID);
				requestBody.put(Constants.SB_ORG_ID, orgId);
				log.info(String.format("New org created for basicProfileUpdate. OrgName: %s, OrgId: %s",
						requestBody.get(Constants.CHANNEL), orgId));
				// We got the orgId successfully... let's migrate the user to this org.
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
				errMsg = executeSelfMigrateUser(requestBody);
			} else {
				try {
					errMsg = "Failed to auto onboard org.";
					log.warn(String.format("%s. Error: %s", errMsg, mapper.writeValueAsString(orgResponse)));
				} catch (Exception e) {
				}
			}
		} else {
			errMsg = executeSelfMigrateUser(requestBody);
		}
		return errMsg;
	}

	private String executeSelfMigrateUser(Map<String, Object> requestBody) {
		String errMsg = StringUtils.EMPTY;
		Map<String, Object> migrateResponse = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPatch(
				serverConfig.getSbUrl() + serverConfig.getLmsUserSelfMigratePath(),
				getUserSelfMigrateRequest((String) requestBody.get(Constants.USER_ID),
						(String) requestBody.get(Constants.CHANNEL)),
				MapUtils.EMPTY_MAP);
		if (Constants.OK.equalsIgnoreCase((String) migrateResponse.get(Constants.RESPONSE_CODE))) {
			log.info(String.format("Successfully self migrated user. UserId: %s, Channel: %s",
					(String) requestBody.get(Constants.USER_ID), (String) requestBody.get(Constants.CHANNEL)));
			errMsg = updateUserProfile(requestBody);
		} else {
			try {
				errMsg = "Failed to Self migrate User.";
				log.warn(String.format("%s. Error: %s", errMsg, mapper.writeValueAsString(migrateResponse)));
			} catch (Exception e) {
			}
		}
		return errMsg;
	}

	private String updateUserProfile(Map<String, Object> request) {
		String errMsg = StringUtils.EMPTY;

		Map<String, Object> userReadResponse = userUtilityService
				.getUsersReadData((String) request.get(Constants.USER_ID), StringUtils.EMPTY, StringUtils.EMPTY);

		List<String> existingRoles;
		if (userReadResponse.containsKey(Constants.ROLES)) {
			existingRoles = (List<String>) userReadResponse.get(Constants.ROLES);
		} else {
			existingRoles = new ArrayList<String>();
		}

		Map<String, Object> existingProfile = (Map<String, Object>) userReadResponse.get(Constants.PROFILE_DETAILS);
		if (ObjectUtils.isEmpty(existingProfile)) {
			errMsg = "Existing ProfileDetails object is empty. Failed to update Profile";
			return errMsg;
		}

		List<Map<String, Object>> professionalDetails;
		if (existingProfile.containsKey(Constants.PROFESSIONAL_DETAILS)) {
			professionalDetails = (List<Map<String, Object>>) existingProfile.get(Constants.PROFESSIONAL_DETAILS);
		} else {
			professionalDetails = new ArrayList<Map<String, Object>>() {
				{
					Map<String, Object> profDetail = new HashMap<String, Object>();
					profDetail.put(Constants.OSID, UUID.randomUUID().toString());
					add(profDetail);
				}
			};
			existingProfile.put(Constants.PROFESSIONAL_DETAILS, professionalDetails);
		}
		professionalDetails.get(0).put(Constants.DESIGNATION, request.get(Constants.POSITION));
		professionalDetails.get(0).put(Constants.ORGANIZATION_TYPE, Constants.GOVERNMENT);

		Map<String, Object> empDetails;
		if (existingProfile.containsKey(Constants.EMPLOYMENTDETAILS)) {
			empDetails = (Map<String, Object>) existingProfile.get(Constants.EMPLOYMENTDETAILS);
		} else {
			empDetails = new HashMap<String, Object>();
			existingProfile.put(Constants.EMPLOYMENTDETAILS, empDetails);
		}
		empDetails.put(Constants.DEPARTMENTNAME, request.get(Constants.CHANNEL));

		Map<String, Object> updateReqBody = new HashMap<String, Object>();

		Map<String, Object> existingPersonalDetail = (Map<String, Object>) existingProfile
				.get(Constants.PERSONAL_DETAILS);
		if (!ObjectUtils.isEmpty(existingPersonalDetail)) {
			if (StringUtils.isNotBlank((String) request.get(Constants.FIRSTNAME))) {
				existingPersonalDetail.put(Constants.FIRSTNAME.toLowerCase(), request.get(Constants.FIRSTNAME));
				updateReqBody.put(Constants.FIRSTNAME, request.get(Constants.FIRSTNAME));
			}
			if (StringUtils.isNotBlank((String) request.get(Constants.LASTNAME))) {
				existingPersonalDetail.put(Constants.SURNAME, request.get(Constants.LASTNAME));
				updateReqBody.put(Constants.LASTNAME, request.get(Constants.LASTNAME));
			}
		}
		updateReqBody.put(Constants.PROFILE_DETAILS, existingProfile);
		updateReqBody.put(Constants.USER_ID, request.get(Constants.USER_ID));
		Map<String, Object> updateRequest = new HashMap<>();
		updateRequest.put(Constants.REQUEST, updateReqBody);

		Map<String, Object> updateResponse = outboundRequestHandlerService.fetchResultUsingPatch(
				serverConfig.getSbUrl() + serverConfig.getLmsUserUpdatePath(), updateRequest, MapUtils.EMPTY_MAP);
		if (!updateResponse.get(Constants.RESPONSE_CODE).equals(Constants.OK)) {
			Map<String, Object> params = (Map<String, Object>) updateResponse.get(Constants.PARAMS);
			errMsg = String.format("Failed to update user profile. Error: %s", params.get("errmsg"));
		} else {
			errMsg = assignUserRole(request, existingRoles);
		}
		return errMsg;
	}

	private String assignUserRole(Map<String, Object> requestBody, List<String> existingRoles) {
		String errMsg = StringUtils.EMPTY;
		Map<String, Object> assignRoleReq = new HashMap<>();
		Map<String, Object> assignRoleReqBody = new HashMap<String, Object>();
		assignRoleReqBody.put(Constants.ORGANIZATION_ID, requestBody.get(Constants.SB_ORG_ID));
		assignRoleReqBody.put(Constants.USER_ID, requestBody.get(Constants.USER_ID));
		if (existingRoles == null) {
			existingRoles = new ArrayList<String>();
		}
		if (existingRoles.size() == 0) {
			existingRoles.add(Constants.PUBLIC);
		}
		assignRoleReqBody.put(Constants.ROLES, existingRoles);
		assignRoleReq.put(Constants.REQUEST, assignRoleReqBody);

		Map<String, Object> assignRoleResponse = (Map<String, Object>) outboundRequestHandlerService
				.fetchResultUsingPost(serverConfig.getSbUrl() + serverConfig.getSbAssignRolePath(), assignRoleReq,
						MapUtils.EMPTY_MAP);
		if (!Constants.OK.equalsIgnoreCase((String) assignRoleResponse.get(Constants.RESPONSE_CODE))) {
			Map<String, Object> params = (Map<String, Object>) assignRoleResponse.get(Constants.PARAMS);
			errMsg = String.format("Failed to assign roles to User. Error: %s", params.get("errmsg"));
		}
		return errMsg;
	}

	@Override
	public SBApiResponse userSignup(Map<String, Object> request) {
		boolean retValue = false;
		SBApiResponse response = createDefaultResponse(Constants.API_PROFILE_SIGNUP);
		String errMsg = validateSignupRequest(request);
		if (!StringUtils.isEmpty(errMsg)) {
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			return response;
		}
		try {
			Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
			requestBody.put(Constants.EMAIL_VERIFIED, true);
			Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPost(
					props.getSbUrl() + props.getLmsUserSignUpPath(), request, userUtilityService.getDefaultHeaders());
			if (Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
				Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
				String userId = (String) result.get(Constants.USER_ID);
				request.put(Constants.USER_ID, userId);
				Map<String, Object> userData = userUtilityService.getUsersReadData(userId, StringUtils.EMPTY,
						StringUtils.EMPTY);
				if (userData.isEmpty() == Boolean.FALSE) {
					request.put(Constants.USER_NAME, userData.get(Constants.USER_NAME));
					retValue = updateUser(userData);
				} else {
					errMsg = "Problem during updating the user";
				}
			} else {
				errMsg = "Problem during User SignUp";
			}

		} catch (Exception e) {
			errMsg = "Failed to process message. Exception: " + e.getMessage();
		}
		if (StringUtils.isNotBlank(errMsg) || retValue == Boolean.FALSE) {
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
		}
		return response;
	}

	private boolean updateUser(Map<String, Object> requestObject) {
		boolean retValue = false;
		Map<String, Object> updateRequest = new HashMap<>();
		Map<String, Object> updateRequestBody = new HashMap<String, Object>();
		updateRequestBody.put(Constants.USER_ID, requestObject.get(Constants.USER_ID));
		Map<String, Object> profileDetails = new HashMap<String, Object>();
		profileDetails.put(Constants.MANDATORY_FIELDS_EXISTS, false);
		Map<String, Object> employementDetails = new HashMap<String, Object>();
		employementDetails.put(Constants.DEPARTMENTNAME, requestObject.get(Constants.ORG_NAME));
		profileDetails.put(Constants.EMPLOYMENTDETAILS, employementDetails);
		Map<String, Object> personalDetails = new HashMap<String, Object>();
		personalDetails.put(Constants.FIRSTNAME.toLowerCase(), requestObject.get(Constants.FIRST_NAME));
		personalDetails.put(Constants.SURNAME, requestObject.get(Constants.LAST_NAME));
		personalDetails.put(Constants.PRIMARY_EMAIL, requestObject.get(Constants.EMAIL));
		personalDetails.put(Constants.USER_NAME, requestObject.get(Constants.USER_NAME));
		profileDetails.put(Constants.PERSONAL_DETAILS, personalDetails);

		Map<String, Object> professionDetailObj = new HashMap<String, Object>();
		professionDetailObj.put(Constants.ORGANIZATION_TYPE, Constants.GOVERNMENT);
		if (StringUtils.isNotEmpty((String) requestObject.get(Constants.POSITION))) {
			professionDetailObj.put(Constants.DESIGNATION, requestObject.get(Constants.POSITION));
		}
		List<Map<String, Object>> professionalDetailsList = new ArrayList<Map<String, Object>>();
		professionalDetailsList.add(professionDetailObj);
		profileDetails.put(Constants.PROFESSIONAL_DETAILS, professionalDetailsList);

		updateRequestBody.put(Constants.PROFILE_DETAILS, profileDetails);
		updateRequest.put(Constants.REQUEST, updateRequestBody);
		Map<String, Object> updateReadData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPatch(
				props.getSbUrl() + props.getLmsUserUpdatePath(), updateRequest, userUtilityService.getDefaultHeaders());
		if (Constants.OK.equalsIgnoreCase((String) updateReadData.get(Constants.RESPONSE_CODE))) {
			Map<String, Object> roleMap = new HashMap<>();
			roleMap.put(Constants.ORGANIZATION_ID, requestObject.get(Constants.ROOT_ORG_ID));
			roleMap.put(Constants.USER_ID, requestObject.get(Constants.USER_ID));
			retValue = assignRole(roleMap);
		}
		return retValue;
	}

	private boolean assignRole(Map<String, Object> request) {
		boolean retValue = false;
		Map<String, Object> requestObj = new HashMap<>();
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(Constants.ORGANIZATION_ID, request.get(Constants.ORGANIZATION_ID));
		requestBody.put(Constants.USER_ID, request.get(Constants.USER_ID));
		requestBody.put(Constants.ROLES, Arrays.asList(Constants.PUBLIC));
		requestObj.put(Constants.REQUEST, requestBody);
		Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPost(
				props.getSbUrl() + props.getSbAssignRolePath(), requestObj, userUtilityService.getDefaultHeaders());
		if (readData.isEmpty() == Boolean.FALSE) {
			if (Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE)))
				retValue = Boolean.TRUE;
		}
		return retValue;
	}

	private String validateSignupRequest(Map<String, Object> requestData) {
		List<String> params = new ArrayList<String>();
		StringBuilder strBuilder = new StringBuilder();
		Map<String, Object> request = (Map<String, Object>) requestData.get(Constants.REQUEST);
		if (ObjectUtils.isEmpty(request)) {
			strBuilder.append("Request object is empty.");
			return strBuilder.toString();
		}

		if (StringUtils.isEmpty((String) request.get(Constants.FIRSTNAME))) {
			params.add(Constants.FIRST_NAME);
		}
		if (StringUtils.isEmpty((String) request.get(Constants.LASTNAME))) {
			params.add(Constants.LAST_NAME);
		}
		if (StringUtils.isEmpty((String) request.get(Constants.EMAIL))) {
			params.add(Constants.EMAIL);
		}
		if (!params.isEmpty()) {
			strBuilder.append("Invalid Request. Missing params - " + params);
		}

		return strBuilder.toString();
	}
}

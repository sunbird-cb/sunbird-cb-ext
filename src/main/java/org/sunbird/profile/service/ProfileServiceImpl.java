package org.sunbird.profile.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
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
import org.sunbird.user.service.UserUtilityServiceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
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
	CassandraOperation cassandraOperation;

	private CbExtLogger log = new CbExtLogger(getClass().getName());

	@SuppressWarnings("unchecked")
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

	public SBApiResponse userBasicInfo(String userToken, String userId) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_USER_BASIC_INFO);
		Map<String, Object> userData = userUtilityService.getUsersReadData(userId, StringUtils.EMPTY, userToken);
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

		response.put(Constants.IS_UPDATE_REQUIRED, false);
		Map<String, Object> responseMap = new HashMap<String, Object>();
		if (custodianOrgChannel.equalsIgnoreCase((String) responseMap.get(Constants.CHANNEL))
				&& custodianOrgId.equalsIgnoreCase((String) responseMap.get(Constants.ROOT_ORG_ID))) {
			// User has custodian Values, check for profile.
			Map<String, Object> profileData = (Map<String, Object>) userData.get(Constants.PROFILE_DETAILS);
			List<Map<String, Object>> userRole = (List<Map<String, Object>>) profileData.get(Constants.USER_ROLES);
			if (CollectionUtils.isEmpty(userRole)) {
				response.put(Constants.IS_UPDATE_REQUIRED, true);
			}
		}

		response.put(Constants.FIRSTNAME, userData.get(Constants.FIRSTNAME));
		response.put(Constants.LASTNAME, userData.get(Constants.LASTNAME));
		response.put(Constants.ROLES, userData.get(Constants.ROLES));
		response.put(Constants.ROOT_ORG_ID, userData.get(Constants.ROOT_ORG_ID));
		response.put(Constants.CHANNEL, userData.get(Constants.CHANNEL));
		response.put(Constants.USER_ID, userData.get(Constants.USER_ID));

		response.getResult().put(Constants.RESPONSE, responseMap);

		return response;
	}

	public SBApiResponse userBasicProfileUpdate(String userToken, Map<String, Object> request) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_USER_BASIC_PROFILE_UPDATE);
		response.setResponseCode(HttpStatus.NOT_IMPLEMENTED);
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
					String value = (String) personalDetailsMap.get(paramName);
					if (StringUtils.isNotEmpty(value)) {
						switch (paramName) {
						case Constants.FIRST_NAME_LOWER_CASE:
							updatedRequest.put(Constants.FIRSTNAME, value);
							break;
						case Constants.SURNAME:
							updatedRequest.put(Constants.LASTNAME, value);
							break;
						}
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
			if (CollectionUtils.isNotEmpty(existingDataList)) {
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
}

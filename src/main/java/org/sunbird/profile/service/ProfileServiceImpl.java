package org.sunbird.profile.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRespParam;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.IndexerService;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.user.registration.model.UserRegistration;
import org.sunbird.user.registration.model.UserRegistrationInfo;
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
	private ObjectMapper mapper;

	@Autowired
	IndexerService indexerService;

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
	public SBApiResponse orgProfileUpdate(Map<String, Object> request)
			throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.ORG_PROFILE_UPDATE);
		Map<String, Object> requestData = (Map<String, Object>) request.get(Constants.REQUEST);
		String errMsg = validateOrgRegistrationPayload(requestData);
		try {
			String orgId = (String) requestData.get(Constants.ORG_ID);
			Map<String, Object> esOrgProfileMap = getOrgRegistrationForRegCode(orgId);//Fetching ES data corresponding to id if any exist
			Boolean esOrgProfileMapStatus = true;
			if (null == esOrgProfileMap) {
				esOrgProfileMap = new HashMap<>();
				esOrgProfileMapStatus = false;
			}
			Map<String, Object> orgProfileDetailsMap = (Map<String, Object>) requestData.get(Constants.PROFILE_DETAILS);
			for (String keys : orgProfileDetailsMap.keySet()) {
				esOrgProfileMap.put(keys, orgProfileDetailsMap.get(keys));
			}
			RestStatus status = null;
			if (esOrgProfileMapStatus) {
				status = indexerService.updateEntity(serverConfig.getOrgRegistrationIndex(),
						serverConfig.getEsProfileIndexType(), orgId,
						esOrgProfileMap);
			} else {
				status = indexerService.addEntity(serverConfig.getOrgRegistrationIndex(),
						serverConfig.getEsProfileIndexType(), orgId,
						esOrgProfileMap);
			}
			if (status.equals(RestStatus.CREATED) || status.equals(RestStatus.OK)) {
				response.setResponseCode(HttpStatus.ACCEPTED);
				response.getResult().put(Constants.RESULT, esOrgProfileMap);
			} else {
				response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
				response.getParams().setErrmsg("Failed to add details to ES Service");
			}

		} catch (Exception e) {
			log.error(e);
			log.warn(String.format("Exception in %s : %s", "registerUser"));
			errMsg = "Failed to process message. Exception: " + e.getMessage();

		}
		if (org.apache.commons.lang.StringUtils.isNotBlank(errMsg)) {
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
		}

		return response;
	}


	@Override
	public SBApiResponse orgProfileRead(String orgId)
			throws Exception {
		SBApiResponse response = createDefaultResponse(Constants.ORG_REGISTRATION_RETRIEVE_API);
		Map<String, Object> orgRegistration = getOrgRegistrationForRegCode(orgId);
		if (orgRegistration != null) {
			response.getResult().put(Constants.RESULT, orgRegistration);
		} else {
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg("Failed to get response");
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
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

	public Map<String, Object> getOrgRegistrationForRegCode(String registrationCode) {
		try {
			Map<String, Object> esObject = indexerService.readEntity(serverConfig.getOrgRegistrationIndex(),
					serverConfig.getEsProfileIndexType(), registrationCode);
			return esObject;
		} catch (Exception e) {
			log.error(e);
			log.warn(String.format("Exception in %s : %s", "getUserRegistrationDetails"));
		}
		return null;
	}

	private String validateOrgRegistrationPayload(Map<String, Object> orgRegInfo) {
		StringBuffer str = new StringBuffer();
		List<String> errList = new ArrayList<String>();
		if (org.apache.commons.lang.StringUtils.isBlank((String) orgRegInfo.get(Constants.ORG_ID))) {
			errList.add(Constants.ORG_ID);
		}
		if (ObjectUtils.isEmpty(orgRegInfo.get(Constants.PROFILE_DETAILS))) {
			errList.add(Constants.PROFILE_DETAILS);
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
}

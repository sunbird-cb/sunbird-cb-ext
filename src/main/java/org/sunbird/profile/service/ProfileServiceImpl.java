package org.sunbird.profile.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.cache.DataCacheMgr;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRespParam;
import org.sunbird.common.service.ContentService;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.*;
import org.sunbird.core.cipher.DecryptServiceImpl;
import org.sunbird.core.producer.Producer;
import org.sunbird.org.service.ExtendedOrgService;
import org.sunbird.storage.service.StorageServiceImpl;
import org.sunbird.user.report.UserReportService;
import org.sunbird.user.service.UserUtilityService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import io.jsonwebtoken.*;

import static java.util.stream.Collectors.toList;

@Service
@SuppressWarnings({ "unchecked" })
public class ProfileServiceImpl implements ProfileService {

	@Autowired
	CbExtServerProperties serverConfig;

	@Autowired
	OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	@Autowired
	UserUtilityService userUtilityService;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	IndexerService indexerService;

	@Autowired
	CassandraOperation cassandraOperation;

	@Autowired
	ExtendedOrgService extOrgService;

	@Autowired
	StorageServiceImpl storageService;

	@Autowired
	ContentService contentService;

	@Autowired
	UserReportService reportService;

	@Autowired
	DecryptServiceImpl decryptService;

	@Autowired
	Producer kafkaProducer;

	@Autowired
	DataCacheMgr dataCacheMgr;

	@Autowired
    AccessTokenValidator accessTokenValidator;

	private Logger log = LoggerFactory.getLogger(getClass().getName());

	@Override
	public SBApiResponse profileUpdate(Map<String, Object> request, String userToken, String authToken, String rootOrgId) {
		SBApiResponse response = new SBApiResponse(Constants.API_PROFILE_UPDATE);
		try {
			Map<String, Object> requestData = (Map<String, Object>) request.get(Constants.REQUEST);
			if (!validateRequest(requestData)) {
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				response.getParams().setStatus(Constants.FAILED);
				return response;
			}

			String userId = (String) requestData.get(Constants.USER_ID);
			String userIdFromToken = accessTokenValidator.fetchUserIdFromAccessToken(userToken);
			if (!userId.equalsIgnoreCase(userIdFromToken)) {
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				response.getParams().setStatus(Constants.FAILED);
				response.getParams().setErrmsg("Invalid UserId in the request");
				return response;
			}
			Map<String, Object> profileDetailsMap = (Map<String, Object>) requestData.get(Constants.PROFILE_DETAILS);
			String validatePhoneEmailErrMsg = validateExistingPhoneEmail(profileDetailsMap);
			if (!validatePhoneEmailErrMsg.isEmpty()) {
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				response.getParams().setStatus(Constants.FAILED);
				response.getParams().setErrmsg(validatePhoneEmailErrMsg);
				return response;
			}
			List<String> approvalFieldList = approvalFields();
			String newDeptName = checkDepartment(profileDetailsMap);
			Map<String, Object> transitionData = new HashMap<>();
			for (String approvalList : approvalFieldList) {
				if (profileDetailsMap.containsKey(approvalList)) {
					transitionData.put(approvalList, profileDetailsMap.get(approvalList));
					profileDetailsMap.remove(approvalList);
				}
			}
			Map<String, Object> responseMap = userUtilityService.getUsersReadData(userId, StringUtils.EMPTY,
					StringUtils.EMPTY);
			String deptName = (String) responseMap.get(Constants.CHANNEL);
			Map<String, Object> existingProfileDetails = (Map<String, Object>) responseMap
					.get(Constants.PROFILE_DETAILS);
			HashMap<String, String> headerValues = new HashMap<>();
			headerValues.put(Constants.AUTH_TOKEN, authToken);
			headerValues.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
			headerValues.put(Constants.X_AUTH_TOKEN, userToken);
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
					} else if (profileDetailsMap.get(changedObj) instanceof Boolean) {
						existingProfileDetails.put(changedObj, profileDetailsMap.get(changedObj));
					} else if (profileDetailsMap.get(changedObj) instanceof String) {
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
				//This field is updated via approval
				/*if (validateJsonAgainstSchema(existingProfileDetails)) {
					existingProfileDetails.put(Constants.VERIFIED_KARMAYOGI, true);
				} else {
					existingProfileDetails.put(Constants.VERIFIED_KARMAYOGI, false);
				}*/
				Map<String, Object> updateRequestValue = requestData;
				updateRequestValue.put(Constants.PROFILE_DETAILS, existingProfileDetails);
				Map<String, Object> updateRequest = new HashMap<>();
				updateRequest.put(Constants.REQUEST, updateRequestValue);
				updateResponse = outboundRequestHandlerService.fetchResultUsingPatch(
						serverConfig.getSbUrl() + serverConfig.getLmsUserUpdatePath(), updateRequest, headerValues);
				if (Constants.OK.equalsIgnoreCase((String) updateResponse.get(Constants.RESPONSE_CODE))) {
					response.setResponseCode(HttpStatus.OK);
					response.getResult().put(Constants.RESPONSE, Constants.SUCCESS);
					response.getParams().setStatus(Constants.SUCCESS);
				} else {
					if (updateResponse != null && Constants.CLIENT_ERROR
							.equalsIgnoreCase((String) updateResponse.get(Constants.RESPONSE_CODE))) {
						response.setResponseCode(HttpStatus.BAD_REQUEST);
					} else {
						response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
					}
					response.getParams().setStatus(Constants.FAILED);
					String errMsg = (String) ((Map<String, Object>) updateResponse.get(Constants.PARAMS))
							.get(Constants.ERROR_MESSAGE);
					errMsg = PropertiesCache.getInstance().readCustomError(errMsg);
					response.getParams().setErrmsg(errMsg);
					log.error(errMsg, new Exception(errMsg));
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
					} else if (transitionData.get(listTransition) instanceof Boolean) {
						Boolean transListObject;
						transListObject = (Boolean) transitionData.get(listTransition);
						Map<String, Object> updatedTransitionData = new HashMap<>();
						Map<String, Object> fromValue = new HashMap<>();
						Map<String, Object> toValue = new HashMap<>();
						toValue.put(Constants.VERIFIED_KARMAYOGI, transListObject);
						fromValue.put(Constants.VERIFIED_KARMAYOGI, transListObject);
						updatedTransitionData.put(Constants.FROM_VALUE, fromValue);
						updatedTransitionData.put(Constants.TO_VALUE, toValue);
						updatedTransitionData.put(Constants.FIELD_KEY, listTransition);
						finalTransitionList.add(updatedTransitionData);
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
				transitionRequests.put(Constants.ROOT_ORG_ID, rootOrgId);
				transitionRequests.put(Constants.COMMENT, "");
				transitionRequests.put(Constants.WFID, "");
				if (null != newDeptName) {
					transitionRequests.put(Constants.DEPT_NAME, newDeptName);
				} else {
					transitionRequests.put(Constants.DEPT_NAME, deptName);
				}
				transitionRequests.put(Constants.UPDATE_FIELD_VALUES, finalTransitionList);
				headerValues.put(Constants.ROOT_ORG_CONSTANT, Constants.IGOT);
				headerValues.put(Constants.ORG_CONSTANT, Constants.DOPT);
				if (headerValues.containsKey(Constants.X_AUTH_TOKEN)) {
					headerValues.remove(Constants.X_AUTH_TOKEN);
				}
				workflowResponse = outboundRequestHandlerService.fetchResultUsingPost(
						serverConfig.getWfServiceHost() + serverConfig.getWfServiceTransitionPath(), transitionRequests,
						headerValues);

				Map<String, Object> resultValue = (Map<String, Object>) workflowResponse.get(Constants.RESULT);
				if (Constants.OK.equalsIgnoreCase((String) resultValue.get(Constants.STATUS))) {
					response.getResult().put(Constants.RESPONSE, Constants.SUCCESS);
					response.getParams().setStatus(Constants.SUCCESS);
					response.setResponseCode(HttpStatus.OK);
				} else {
					response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
					response.getParams().setStatus(Constants.FAILED);
					String errMsg = "Failed to raise workflow transition request.";
					response.getParams().setErrmsg(errMsg);
					log.error(errMsg, new Exception(errMsg));
				}
			}
		} catch (Exception e) {
			log.error("Failed to process profile update. Exception: ", e);
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErr(e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	public boolean validateJsonAgainstSchema(Map<String, Object> existingProfileDetails) {
		try {
			String jsonData = new Gson().toJson(existingProfileDetails);
			String jsonSchema = getVerifiedProfileSchema();
			JSONObject rawSchema = new JSONObject(new JSONTokener(jsonSchema));
			JSONObject data = new JSONObject(new JSONTokener(jsonData));
			Schema schema = SchemaLoader.load(rawSchema);
			schema.validate(data);
		} catch (JSONException e) {
			log.error("Failed to parse json schema. Exception: ", e);
			throw new RuntimeException("Can't parse json schema: " + e.getMessage(), e);
		} catch (ValidationException ex) {
			log.warn("Validation against Json schema failed. Exception : %s", ex);
			return false;
		}
		return true;
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
				log.error("Failed to process orgProfileUpdate. Exception: ", e);
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
	public SBApiResponse migrateUser(Map<String, Object> request, String userToken, String authToken) {
		SBApiResponse response = new SBApiResponse(Constants.ORG_PROFILE_UPDATE);
		// Initializing default error
		response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		response.getParams().setStatus(Constants.FAILED);

		String errMsg = validateMigrateRequest(request);
		if (StringUtils.isNotEmpty(errMsg)) {
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			return response;
		}

		HashMap<String, String> headerValues = new HashMap<>();
		headerValues.put(Constants.AUTH_TOKEN, authToken);
		headerValues.put(Constants.X_AUTH_TOKEN, userToken);
		headerValues.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);

		Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
		String userId = (String) requestBody.get(Constants.USER_ID);
		String orgName = (String) requestBody.get(Constants.CHANNEL);
		errMsg = executeMigrateUser(getUserMigrateRequest(userId, orgName, false), headerValues);
		if (StringUtils.isNotEmpty(errMsg)) {
			setErrorData(response, errMsg);
			return response;
		}
		log.info(String.format("Successfully migrated user. UserId: %s, Channel: %s", userId, orgName));

		Map<String, Object> userData = getUserDetailsForId(userId);
		if (ObjectUtils.isEmpty(userData)) {
			response.getParams().setErrmsg(String.format("Failed to get User record from DB. UserId: %s", userId));
			return response;
		}

		Map<String, Object> updateDBRequest = new HashMap<>();
		updateDBRequest.put(Constants.CHANNEL, orgName);

		String profileDetailsStr = (String) userData.get(Constants.PROFILE_DETAILS_LOWER);
		if (StringUtils.isEmpty(profileDetailsStr)) {
			response.getParams().setErrmsg("ProfileDetails is null for User.");
			return response;
		}
		try {
			Map<String, Object> profileDetails = mapper.readValue(profileDetailsStr,
					new TypeReference<Map<String, Object>>() {
					});
			if (profileDetails.containsKey(Constants.EMPLOYMENT_DETAILS)) {
				Map<String, Object> empDetails = (Map<String, Object>) profileDetails.get(Constants.EMPLOYMENT_DETAILS);
				empDetails.put(Constants.DEPARTMENTNAME, orgName);
				empDetails.put(Constants.DEPARTMENT_ID, (String) userData.get(Constants.ROOT_ORG_ID_LOWER));
				profileDetails.put(Constants.EMPLOYMENT_DETAILS, empDetails);
			}

			Map<String, Object> professionalDetail = null;
			if (profileDetails.containsKey(Constants.PROFESSIONAL_DETAILS)
					&& !ObjectUtils.isEmpty(profileDetails.get(Constants.PROFESSIONAL_DETAILS))) {
				professionalDetail = ((List<Map<String, Object>>) profileDetails.get(Constants.PROFESSIONAL_DETAILS))
						.get(0);
			} else {
				professionalDetail = new HashMap<>();
				professionalDetail.put(Constants.OSID, UUID.randomUUID().toString());
			}

			professionalDetail.put(Constants.NAME, orgName);
			professionalDetail.put(Constants.ID, (String) userData.get(Constants.ROOT_ORG_ID_LOWER));
			profileDetails.put(Constants.PROFESSIONAL_DETAILS, Arrays.asList(professionalDetail));

			updateDBRequest.put(Constants.PROFILE_DETAILS_LOWER, mapper.writeValueAsString(profileDetails));
		} catch (Exception e) {
			errMsg = String.format("Failed to parse profileDetails object for userId: %s. Exception: ",
					(String) requestBody.get(Constants.USER_ID));
			log.error(errMsg, e);
			response.getParams().setErrmsg(errMsg);
			return response;
		}

		Map<String, Object> compositeKey = new HashMap<String, Object>() {
			private static final long serialVersionUID = 1L;
			{
				put(Constants.ID, userId);
			}
		};
		Map<String, Object> updateDBResponse = cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD,
				Constants.TABLE_USER, updateDBRequest, compositeKey);
		if (updateDBResponse != null
				&& !Constants.SUCCESS.equalsIgnoreCase((String) updateDBResponse.get(Constants.RESPONSE))) {
			errMsg = String.format("Failed to update profileDetails for UserId : %s", userId);
			response.getParams().setErrmsg(errMsg);
			return response;
		}

		boolean assignValue = userUtilityService.assignRole((String) userData.get(Constants.ROOT_ORG_ID), userId,
				StringUtils.EMPTY);

		if (assignValue) {
			errMsg = syncUserData(userId);
		} else {
			response.getParams().setErrmsg("Failed to assign PUBLIC role to user. UserId: " + userId);
			return response;
		}

		Map<String, Object> workflowResponse;
		if (StringUtils.isEmpty(errMsg)) {
			HashMap<String, String> wfHeaders = new HashMap<>();
			wfHeaders.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
			wfHeaders.put(Constants.ROOT_ORG_CONSTANT, Constants.IGOT);
			wfHeaders.put(Constants.ORG_CONSTANT, Constants.DOPT);

			Map<String, Object> wfRequestBody = new HashMap<>();
			wfRequestBody.put(Constants.USER_ID, requestBody.get(Constants.USER_ID));
			wfRequestBody.put(Constants.DEPARTMENTNAME, requestBody.get(Constants.CHANNEL));
			wfRequestBody.put(Constants.FORCE_MIGRATION, true);
			Map<String, Object> wfRequest = new HashMap<>();
			wfRequest.put(Constants.REQUEST, wfRequestBody);

			workflowResponse = outboundRequestHandlerService.fetchResultUsingPost(
					serverConfig.getWfServiceHost() + serverConfig.getPendingRequestsToNewMDOPath(), wfRequest,
					wfHeaders);
			Map<String, Object> result = (Map<String, Object>) workflowResponse.get(Constants.RESULT);
			if (!ObjectUtils.isEmpty(workflowResponse) && !Constants.OK.equalsIgnoreCase((String) result.get(Constants.STATUS))) {
				response.getParams().setErrmsg((String) workflowResponse.get(Constants.ERROR_MESSAGE));
				return response;
			}
			log.info("Successfully transferred the pending approval requests to the new MDO");

		} else {
			response.getParams().setErrmsg(errMsg);
			return response;
		}

		response.setResponseCode(HttpStatus.OK);
		response.getResult().put(Constants.RESPONSE, Constants.SUCCESS);
		response.getParams().setStatus(Constants.SUCCESS);
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
					if (personalDetail.containsKey(Constants.MOBILE)) {
						responseMap.put(Constants.PHONE, String.valueOf(personalDetail.get(Constants.MOBILE)));
					}
				}

				responseMap.put(Constants.FIRSTNAME, userData.get(Constants.FIRSTNAME));
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

	@Override
	public SBApiResponse userAutoComplete(String searchTerm) {
		SBApiResponse response = new SBApiResponse();
		response.setResponseCode(HttpStatus.BAD_REQUEST);
		response.getParams().setStatus(Constants.FAILED);
		if (StringUtils.isEmpty(searchTerm)) {
			response.getParams().setErrmsg("Invalid Search Term");
			return response;
		}

		response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		Map<String, Object> resultResp = new HashMap<>();
		try {
			List<Map<String, Object>> userData = getUserSearchData(searchTerm);
			resultResp.put(Constants.CONTENT, userData);
			resultResp.put(Constants.COUNT, userData.size());
			response.setResponseCode(HttpStatus.OK);
			response.getParams().setStatus(Constants.SUCCESS);
			response.put(Constants.RESPONSE, resultResp);
		} catch (Exception e) {
			response.getParams().setErrmsg("Failed to get user details from ES. Exception: " + e.getMessage());
		}
		return response;
	}

	@Override
	public SBApiResponse userAdminAutoComplete(String searchTerm,String rooOrgId) {
		SBApiResponse response = new SBApiResponse();
		response.setResponseCode(HttpStatus.BAD_REQUEST);
		response.getParams().setStatus(Constants.FAILED);
		if (StringUtils.isEmpty(searchTerm) || StringUtils.isEmpty(rooOrgId)) {
			response.getParams().setErrmsg("Invalid Search Term or Root Org Id");
			return response;
		}
		response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		Map<String, Object> resultResp = new HashMap<>();
		try {
			List<Map<String, Object>> userData = getAdminUserSearchData(searchTerm, rooOrgId);
			resultResp.put(Constants.CONTENT, userData);
			resultResp.put(Constants.COUNT, userData.size());
			response.setResponseCode(HttpStatus.OK);
			response.getParams().setStatus(Constants.SUCCESS);
			response.put(Constants.RESPONSE, resultResp);
		} catch (Exception e) {
			response.getParams().setErrmsg("Failed to get user details from ES. Exception: " + e.getMessage());
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
			log.error("Failed to do user basic profile update. Exception: ", e);
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

	public SBApiResponse userSignup(Map<String, Object> request) {
		boolean retValue = false;
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_USER_SIGNUP);

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
					serverConfig.getSbUrl() + serverConfig.getLmsUserSignUpPath(), request,
					ProjectUtil.getDefaultHeaders());
			if (Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
				Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
				String userId = (String) result.get(Constants.USER_ID);
				request.put(Constants.USER_ID, userId);
				Map<String, Object> userData = userUtilityService.getUsersReadData(userId, StringUtils.EMPTY,
						StringUtils.EMPTY);
				if (!userData.isEmpty()) {
					request.put(Constants.USER_NAME, userData.get(Constants.USER_NAME));
					request.put(Constants.ROOT_ORG_ID, userData.get(Constants.ROOT_ORG_ID));
					request.put(Constants.ORG_NAME, userData.get(Constants.CHANNEL));
					retValue = updateUser(request);
					if (retValue) {
						response.getResult().put(Constants.RESPONSE, Constants.SUCCESS);
						response.getResult().put(Constants.USER_ID, userId);
					}
				} else {
					errMsg = "Failed to read the user data after Signup.";
				}
			} else {
				errMsg = "Failed to signup the user account";
			}
		} catch (Exception e) {
			errMsg = "Failed to process message. Exception: " + e.getMessage();
		}
		if (StringUtils.isNotBlank(errMsg) || !retValue) {
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
		}

		return response;
	}

	@Override
	public SBApiResponse bulkUpload(MultipartFile mFile, String orgId, String channel, String userId, String userAuthToken) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_USER_BULK_UPLOAD);
		try {
			if (isFileExistForProcessingForMDO(orgId)) {
				setErrorDataForMdo(response, "Failed to upload for another request as previous request is in processing state, please try after some time.");
				return response;
			}
			SBApiResponse uploadResponse = storageService.uploadFile(mFile, serverConfig.getBulkUploadContainerName());
			if (!HttpStatus.OK.equals(uploadResponse.getResponseCode())) {
				setErrorData(response, String.format("Failed to upload file. Error: %s",
						(String) uploadResponse.getParams().getErrmsg()));
				return response;
			}

			Map<String, Object> uploadedFile = new HashMap<>();
			uploadedFile.put(Constants.ROOT_ORG_ID, orgId);
			uploadedFile.put(Constants.IDENTIFIER, UUID.randomUUID().toString());
			uploadedFile.put(Constants.FILE_NAME, uploadResponse.getResult().get(Constants.NAME));
			uploadedFile.put(Constants.FILE_PATH, uploadResponse.getResult().get(Constants.URL));
			uploadedFile.put(Constants.DATE_CREATED_ON, new Timestamp(System.currentTimeMillis()));
			uploadedFile.put(Constants.STATUS, Constants.INITIATED_CAPITAL);
			uploadedFile.put(Constants.COMMENT, StringUtils.EMPTY);
			uploadedFile.put(Constants.CREATED_BY, userId);

			SBApiResponse insertResponse = cassandraOperation.insertRecord(Constants.DATABASE,
					Constants.TABLE_USER_BULK_UPLOAD, uploadedFile);

			if (!Constants.SUCCESS.equalsIgnoreCase((String) insertResponse.get(Constants.RESPONSE))) {
				setErrorData(response, "Failed to update database with user bulk upload file details.");
				return response;
			}

			response.getParams().setStatus(Constants.SUCCESSFUL);
			response.setResponseCode(HttpStatus.OK);
			response.getResult().putAll(uploadedFile);
			uploadedFile.put(Constants.ORG_NAME, channel);
			uploadedFile.put(Constants.X_AUTH_TOKEN, userAuthToken);
			kafkaProducer.pushWithKey(serverConfig.getUserBulkUploadTopic(), uploadedFile, orgId);
			sendBulkUploadNotification(orgId, channel, (String) uploadResponse.getResult().get(Constants.URL));
		} catch (Exception e) {
			setErrorData(response,
					String.format("Failed to process user bulk upload request. Error: ", e.getMessage()));
		}
		return response;
	}

	@Override
	public SBApiResponse getBulkUploadDetails(String orgId) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_USER_BULK_UPLOAD_STATUS);
		try {
			Map<String, Object> propertyMap = new HashMap<>();
			if (StringUtils.isNotBlank(orgId)) {
				propertyMap.put(Constants.ROOT_ORG_ID, orgId);
			}
			List<Map<String, Object>> bulkUploadList = cassandraOperation.getRecordsByProperties(Constants.DATABASE,
					Constants.TABLE_USER_BULK_UPLOAD, propertyMap, serverConfig.getBulkUploadStatusFields());
			response.getParams().setStatus(Constants.SUCCESSFUL);
			response.setResponseCode(HttpStatus.OK);
			response.getResult().put(Constants.CONTENT, bulkUploadList);
			response.getResult().put(Constants.COUNT, bulkUploadList != null ? bulkUploadList.size() : 0);
		} catch (Exception e) {
			setErrorData(response,
					String.format("Failed to get user bulk upload request status. Error: ", e.getMessage()));
		}
		return response;
	}

	public List<String> approvalFields() {
		List<String> approvalFields = (List<String>) dataCacheMgr
				.getObjectFromCache(Constants.PROFILE_APPROVAL_FIELDS_KEY);
		if (CollectionUtils.isEmpty(approvalFields)) {
			Map<String, Object> searchRequest = new HashMap<String, Object>();
			searchRequest.put(Constants.ID, Constants.PROFILE_APPROVAL_FIELDS_KEY);

			List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
					Constants.KEYSPACE_SUNBIRD, Constants.TABLE_SYSTEM_SETTINGS, searchRequest, null);
			if (CollectionUtils.isNotEmpty(existingDataList)) {
				Map<String, Object> data = existingDataList.get(0);
				String strApprovalFields = (String) data.get(Constants.VALUE);

				if (StringUtils.isNotBlank(strApprovalFields)) {
					String strArray[] = strApprovalFields.split(",", -1);
					approvalFields = Arrays.asList(strArray);
					dataCacheMgr.putObjectInCache(Constants.PROFILE_APPROVAL_FIELDS_KEY, approvalFields);
					return approvalFields;
				}
			}
		} else {
			return approvalFields;
		}
		return new ArrayList<>();
	}

	public String getVerifiedProfileSchema() {
		String strSchema = dataCacheMgr.getStringFromCache(Constants.VERIFIED_PROFILE_FIELDS_KEY);
		if (StringUtils.isEmpty(strSchema)) {
			Map<String, Object> searchRequest = new HashMap<String, Object>();
			searchRequest.put(Constants.ID, Constants.VERIFIED_PROFILE_FIELDS_KEY);

			List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
					Constants.KEYSPACE_SUNBIRD, Constants.TABLE_SYSTEM_SETTINGS, searchRequest, null);
			if (CollectionUtils.isNotEmpty(existingDataList)) {
				Map<String, Object> data = existingDataList.get(0);
				strSchema = (String) data.get(Constants.VALUE);
			}
			dataCacheMgr.putStringInCache(Constants.VERIFIED_PROFILE_FIELDS_KEY, strSchema);
		}
		return strSchema;
	}

	public String checkDepartment(Map<String, Object> requestProfile) throws Exception {
		String requestDeptName = null;
		if (requestProfile.containsKey(Constants.PROFESSIONAL_DETAILS)) {
			List<Map<String, Object>> profDetails = (List<Map<String, Object>>) requestProfile
					.get(Constants.PROFESSIONAL_DETAILS);
			if (profDetails.get(0).containsKey(Constants.NAME)) {
				requestDeptName = (String) profDetails.get(0).get(Constants.NAME);
			}
		}
		return requestDeptName;
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
					} else if (Constants.MOBILE.equalsIgnoreCase(paramName)) {
						updatedRequest.put(Constants.PHONE, String.valueOf(personalDetailsMap.get(paramName)));
					} else if (Constants.PRIMARY_EMAIL.equalsIgnoreCase(paramName)) {
						updatedRequest.put(Constants.EMAIL, String.valueOf(personalDetailsMap.get(paramName)));
					}
				}
			}
		} catch (Exception e) {
			log.error("Exception while verifying profile details. ", e);
		}
	}

	public Map<String, Object> getOrgProfileForOrgId(String registrationCode) {
		try {
			Map<String, Object> esObject = indexerService.readEntity(serverConfig.getOrgOnboardingIndex(),
					serverConfig.getEsProfileIndexType(), registrationCode);
			return esObject;
		} catch (Exception e) {
			log.error("Failed to get Org Profile. Exception: ", e);
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
		response.setParams(new SunbirdApiRespParam(UUID.randomUUID().toString()));
		response.getParams().setStatus(Constants.SUCCESS);
		response.setResponseCode(HttpStatus.OK);
		response.setTs(DateTime.now().toString());
		return response;
	}

	public String getCustodianOrgId() {
		String custodianOrgId = dataCacheMgr.getStringFromCache(Constants.CUSTODIAN_ORG_ID);
		if (StringUtils.isEmpty(custodianOrgId)) {
			Map<String, Object> searchRequest = new HashMap<String, Object>();
			searchRequest.put(Constants.ID, Constants.CUSTODIAN_ORG_ID);

			List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
					Constants.KEYSPACE_SUNBIRD, Constants.TABLE_SYSTEM_SETTINGS, searchRequest, null);
			if (CollectionUtils.isNotEmpty(existingDataList)) {
				Map<String, Object> data = existingDataList.get(0);
				custodianOrgId = (String) data.get(Constants.VALUE.toLowerCase());
			}
			dataCacheMgr.putStringInCache(Constants.CUSTODIAN_ORG_ID, custodianOrgId);
		}
		return custodianOrgId;
	}

	public String getCustodianOrgChannel() {
		String custodianOrgChannel = dataCacheMgr.getStringFromCache(Constants.CUSTODIAN_ORG_CHANNEL);
		if (StringUtils.isEmpty(custodianOrgChannel)) {
			Map<String, Object> searchRequest = new HashMap<String, Object>();
			searchRequest.put(Constants.ID, Constants.CUSTODIAN_ORG_CHANNEL);

			List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
					Constants.KEYSPACE_SUNBIRD, Constants.TABLE_SYSTEM_SETTINGS, searchRequest, null);
			if (CollectionUtils.isNotEmpty(existingDataList)) {
				Map<String, Object> data = existingDataList.get(0);
				custodianOrgChannel = (String) data.get(Constants.VALUE.toLowerCase());
			}
			dataCacheMgr.putStringInCache(Constants.CUSTODIAN_ORG_CHANNEL, custodianOrgChannel);
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
			List<String> keys = Arrays.asList(Constants.USER_ID, Constants.GROUP, Constants.CHANNEL,
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
		requestBody.put(Constants.ORG_NAME, request.get(Constants.ORG_NAME));
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

	private Map<String, Object> getUserMigrateRequest(String userId, String channel, boolean isSelfMigrate) {
		Map<String, Object> requestBody = new HashMap<String, Object>() {
			{
				put(Constants.USER_ID, userId);
				put(Constants.CHANNEL, channel);
				put(Constants.SOFT_DELETE_OLD_ORG, true);
				put(Constants.NOTIFY_MIGRATION, false);
				if (!isSelfMigrate) {
					put(Constants.FORCE_MIGRATION, true);
				}
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
			SBApiResponse orgResponse = extOrgService.createOrgForUserRegistration(getOrgCreateRequest(requestBody));
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
				serverConfig.getSbUrl() + serverConfig.getLmsUserMigratePath(),
				getUserMigrateRequest((String) requestBody.get(Constants.USER_ID),
						(String) requestBody.get(Constants.CHANNEL), true),
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
		professionalDetails.get(0).put(Constants.GROUP, request.get(Constants.GROUP));
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
		}
		updateReqBody.put(Constants.PROFILE_DETAILS, existingProfile);
		updateReqBody.put(Constants.USER_ID, request.get(Constants.USER_ID));
		Map<String, Object> updateRequest = new HashMap<>();
		updateRequest.put(Constants.REQUEST, updateReqBody);

		Map<String, Object> updateResponse = outboundRequestHandlerService.fetchResultUsingPatch(
				serverConfig.getSbUrl() + serverConfig.getLmsUserUpdatePrivatePath(), updateRequest, MapUtils.EMPTY_MAP);
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

	private String validateMigrateRequest(Map<String, Object> requestBody) {
		StringBuffer str = new StringBuffer();
		List<String> errObjList = new ArrayList<String>();

		Map<String, Object> request = (Map<String, Object>) requestBody.get(Constants.REQUEST);
		if (ObjectUtils.isEmpty(request)) {
			str.append("Request object is empty.");
			return str.toString();
		}
		if (StringUtils.isEmpty((String) request.get(Constants.USER_ID))) {
			errObjList.add(Constants.USER_ID);
		}
		if (StringUtils.isEmpty((String) request.get(Constants.CHANNEL))) {
			errObjList.add(Constants.CHANNEL);
		}

		if (!errObjList.isEmpty()) {
			str.append("Failed to Register User Details. Missing Params - [").append(errObjList.toString()).append("]");
		}
		return str.toString();
	}

	private void setErrorData(SBApiResponse response, String errMsg) {
		response.getParams().setStatus(Constants.FAILED);
		response.getParams().setErrmsg(errMsg);
		response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private String executeMigrateUser(Map<String, Object> request, Map<String, String> headers) {
		String errMsg = StringUtils.EMPTY;
		Map<String, Object> migrateResponse = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPatch(
				serverConfig.getSbUrl() + serverConfig.getLmsUserMigratePath(), request, headers);
		if (migrateResponse == null
				|| !Constants.OK.equalsIgnoreCase((String) migrateResponse.get(Constants.RESPONSE_CODE))) {
			errMsg = migrateResponse == null ? "Failed to migrate User."
					: (String) ((Map<String, Object>) migrateResponse.get(Constants.PARAMS))
							.get(Constants.ERROR_MESSAGE);
		}
		return errMsg;
	}

	private Map<String, Object> getUserDetailsForId(String userId) {
		Map<String, Object> request = new HashMap<>();
		request.put(Constants.ID, userId);
		List<Map<String, Object>> userList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(Constants.KEYSPACE_SUNBIRD,
				Constants.TABLE_USER, request, null);
		if (CollectionUtils.isNotEmpty(userList)) {
			return userList.get(0);
		} else {
			return MapUtils.EMPTY_MAP;
		}
	}

	private String syncUserData(String userId) {
		String errMsg = null;
		Map<String, Object> requestBody = new HashMap<String, Object>();
		Map<String, Object> request = new HashMap<String, Object>();
		request.put(Constants.OPERATION_TYPE, Constants.SYNC);
		request.put(Constants.OBJECT_IDS, Arrays.asList(userId));
		request.put(Constants.OBJECT_TYPE, Constants.USER);
		requestBody.put(Constants.REQUEST, request);

		Map<String, Object> syncDataResp = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPost(
				serverConfig.getSbUrl() + serverConfig.getLmsDataSyncPath(), requestBody, MapUtils.EMPTY_MAP);
		if (syncDataResp == null
				|| !Constants.OK.equalsIgnoreCase((String) syncDataResp.get(Constants.RESPONSE_CODE))) {
			errMsg = "Failed to call Data Sync after updating Profile for User: " + userId;
		}
		return errMsg;
	}

	public List<Map<String, Object>> getUserSearchData(String searchTerm) throws Exception {
		List<Map<String, Object>> resultArray = new ArrayList<>();
		Map<String, Object> result;
		final BoolQueryBuilder query = QueryBuilders.boolQuery();
		final BoolQueryBuilder finalQuery = QueryBuilders.boolQuery();
		for (String field : serverConfig.getEsAutoCompleteSearchFields()) {
			query.should(QueryBuilders.matchPhrasePrefixQuery(field, searchTerm));
		}
		finalQuery.must(QueryBuilders.termQuery(Constants.STATUS_RAW, 1)).must(query);
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(finalQuery);
		sourceBuilder.size(serverConfig.getEsDefaultResultLimit());
		sourceBuilder.fetchSource(serverConfig.getEsAutoCompleteIncludeFields(), new String[]{});
		SearchResponse searchResponse = indexerService.getEsResult(serverConfig.getSbEsUserProfileIndex(),
				serverConfig.getEsProfileIndexType(), sourceBuilder, true);
		for (SearchHit hit : searchResponse.getHits()) {
			result = hit.getSourceAsMap();
			resultArray.add(result);
		}
		return resultArray;
	}

	public List<Map<String, Object>> getAdminUserSearchData(String searchTerm, String rootOrgId) throws Exception {
		List<Map<String, Object>> resultArray = new ArrayList<>();
		Map<String, Object> result;
		final BoolQueryBuilder nestedQuery = QueryBuilders.boolQuery();
		final BoolQueryBuilder finalQuery = QueryBuilders.boolQuery();
		searchTerm = searchTerm.toLowerCase();
		MatchQueryBuilder matchQueryRootOrgId = QueryBuilders.matchQuery(Constants.ROOT_ORG_ID_RAW, rootOrgId);
		for (String field : serverConfig.getEsAutoCompleteSearchFields()) {
			nestedQuery.should(new WildcardQueryBuilder(field, "*" + searchTerm + "*"));
		}
		TermQueryBuilder termQueryStatus = QueryBuilders.termQuery(Constants.STATUS_RAW, 1);
		finalQuery.must(matchQueryRootOrgId);
		finalQuery.must(nestedQuery);
		finalQuery.must(termQueryStatus);
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(finalQuery);
		sourceBuilder.fetchSource(serverConfig.getEsAutoCompleteIncludeFields(), new String[]{});
		SearchResponse searchResponse = indexerService.getEsResult(serverConfig.getSbEsUserProfileIndex(),
				serverConfig.getEsProfileIndexType(), sourceBuilder, true);
		for (SearchHit hit : searchResponse.getHits()) {
			result = hit.getSourceAsMap();
			resultArray.add(result);
		}
		return resultArray;
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
		Map<String, Object> requestBody = (Map<String, Object>) requestObject.get(Constants.REQUEST);
		personalDetails.put(Constants.FIRSTNAME.toLowerCase(), requestBody.get(Constants.FIRSTNAME));
		personalDetails.put(Constants.PRIMARY_EMAIL, requestBody.get(Constants.EMAIL));
		if (requestBody.containsKey(Constants.PHONE)) {
			String incomingPhoneValue = "";
			try {
				incomingPhoneValue = (String) requestBody.get(Constants.PHONE);
				long mobileNumber = Long.parseLong(incomingPhoneValue);
				personalDetails.put(Constants.MOBILE, mobileNumber);
			} catch (NumberFormatException e) {
				log.error("Failed to parse mobile number from signup request. Received Phone: " + incomingPhoneValue
						+ ", Exception: " + e.getMessage(), e);
			}
		}
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
				serverConfig.getSbUrl() + serverConfig.getLmsUserUpdatePrivatePath(), updateRequest,
				ProjectUtil.getDefaultHeaders());
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
				serverConfig.getSbUrl() + serverConfig.getSbAssignRolePath(), requestObj,
				ProjectUtil.getDefaultHeaders());
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
		if (StringUtils.isEmpty((String) request.get(Constants.EMAIL))) {
			params.add(Constants.EMAIL);
		}
		if (!params.isEmpty()) {
			strBuilder.append("Invalid Request. Missing params - " + params);
		}

		return strBuilder.toString();
	}

	private void sendBulkUploadNotification(String orgId, String orgName, String fileUrl) {
		for (String email : serverConfig.getBulkUploadEmailNotificationList()) {
			if (StringUtils.isBlank(email)) {
				return;
			}
		}
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(Constants.BODY, Constants.HELLO);
		requestBody.put(Constants.EMAIL_TEMPLATE_TYPE, serverConfig.getBulkUploadEmailTemplate());
		requestBody.put(Constants.LINK, fileUrl);
		requestBody.put(Constants.MODE, Constants.EMAIL);
		requestBody.put(Constants.ORG_NAME, orgName);
		requestBody.put(Constants.ORG_ID, orgId);
		requestBody.put(Constants.RECIPIENT_EMAILS, serverConfig.getBulkUploadEmailNotificationList());
		requestBody.put(Constants.SET_PASSWORD_LINK, true);
		requestBody.put(Constants.SUBJECT, serverConfig.getBulkUploadEmailNotificationSubject());

		request.put(Constants.REQUEST, requestBody);

		outboundRequestHandlerService.fetchResultUsingPost(
				serverConfig.getSbUrl() + serverConfig.getSbSendNotificationEmailPath(), request,
				ProjectUtil.getDefaultHeaders());
	}

	@Override
	public SBApiResponse getUserEnrollmentReport() {
		log.info("Starting user enrolment report...");
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_USER_ENROLMENT_REPORT);
		Map<String, Object> propertyMap = new HashMap<>();
		propertyMap.put(Constants.ACTIVE, Boolean.TRUE);

		// Use the following map to construct the excel report
		Map<String, Map<String, String>> userInfoMap = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> courseInfoMap = new HashMap<String, Map<String, String>>();
		Map<String, String> orgInfoMap = new HashMap<String, String>();

		try {
			List<Map<String, Object>> userEnrolmentList = cassandraOperation.getRecordsByProperties(
					Constants.KEYSPACE_SUNBIRD_COURSES, Constants.TABLE_USER_ENROLMENT, propertyMap,
					Arrays.asList(Constants.USER_ID_CONSTANT, Constants.COURSE_ID, Constants.BATCH_ID,
							Constants.COMPLETION_PERCENTAGE, Constants.PROGRESS, Constants.STATUS,
							Constants.CONTENT_STATUS));
			if (CollectionUtils.isEmpty(userEnrolmentList)) {
				log.info("No records found in the user enolment table.");
				return response;
			}
			log.info(String.format("Found %s records in user enrolment table.", userEnrolmentList.size()));

			List<String> enrolledUserIdList = userEnrolmentList.stream()
					.map(obj -> (String) obj.get(Constants.USER_ID_CONSTANT)).collect(Collectors.toList());
			List<String> userIdsDistinct = enrolledUserIdList.stream().distinct().collect(toList());
			log.info(String.format("Found %s unique users in user enrolment table.", userIdsDistinct.size()));
			enrichUserDetails(userIdsDistinct, userInfoMap, orgInfoMap);
			log.info(String.format("Enriched %s records in userInfo and %s records in orgInfo", userInfoMap.size(),
					orgInfoMap.size()));

			List<String> enrolledCourseIdList = userEnrolmentList.stream()
					.map(obj -> (String) obj.get(Constants.COURSE_ID)).collect(Collectors.toList());
			List<String> courseIdsDistinct = enrolledCourseIdList.stream().distinct().collect(toList());
			log.info(String.format("Found %s unique courses in user enrolment table.", courseIdsDistinct.size()));
			enrichCourseDetails(courseIdsDistinct, courseInfoMap, orgInfoMap);
			log.info(String.format("Enriched %s records in courseInfo and %s records in orgInfo", courseInfoMap.size(),
					orgInfoMap.size()));

			Map<String, Map<String, String>> userEnrolmentMap = new HashMap<String, Map<String, String>>();
			// Construct the userEnrolment Map;
			for (Map<String, Object> enrolment : userEnrolmentList) {
				Map<String, String> enrolmentReport = new HashMap<String, String>();
				// Get user details
				String userId = (String) enrolment.get(Constants.USER_ID);
				String courseId = (String) enrolment.get(Constants.COURSE_ID);
				String batchId = (String) enrolment.get(Constants.BATCH_ID);
				String enrolmentId = String.format("%s:%s:%s", userId, courseId, batchId);

				boolean isInfoAvailable = true;
				if (userInfoMap.containsKey(userId)) {
					copyReportDetails(enrolmentReport, userInfoMap.get(userId), Constants.USER_CONST);
				} else {
					log.error(String.format(
							"Failed to get user details for Id: %s, this user may have deactivated. Skipping user. ",
							userId));
					isInfoAvailable = false;
				}

				if (courseInfoMap.containsKey(courseId)) {
					copyReportDetails(enrolmentReport, courseInfoMap.get(courseId), Constants.COURSE);
				} else {
					log.error(String.format(
							"Failed to get course details for Id: %s, this user may have deactivated. Skipping record. ",
							courseId));
					isInfoAvailable = false;
				}

				if (!isInfoAvailable) {
					log.error(String.format(
							"Failed to enrich basic Details. Skipping record for UserId: %s, CourseId: %s, BatchId: %s",
							userId, courseId, batchId));
					continue;
				}

				userEnrolmentMap.put(enrolmentId, enrolmentReport);

				Integer status = (Integer) enrolment.get(Constants.STATUS);
				String strStatus = StringUtils.EMPTY;
				switch (status) {
				case 0:
					strStatus = Constants.STATUS_ENROLLED;
					break;
				case 1:
					strStatus = Constants.STATUS_IN_PROGRESS;
					break;
				case 2:
					strStatus = Constants.STATUS_COMPLETED;
					break;
				default:
					strStatus = "NA";
				}
				enrolmentReport.put(Constants.STATUS, strStatus);

				Map<String, Integer> contentStatus = (Map<String, Integer>) enrolment.get(Constants.CONTENT_STATUS);
				if (ObjectUtils.isEmpty(contentStatus)) {
					enrolmentReport.put(Constants.COMPLETION_PERCENTAGE, Integer.toString(0));
				} else {
					int leafNodeCount = 1;
					if (courseInfoMap.containsKey(courseId)) {
						String strLeafNode = courseInfoMap.get(courseId).get(Constants.LEAF_NODES_COUNT);
						if (StringUtils.isNotBlank(strLeafNode)) {
							try {
								leafNodeCount = Integer.parseInt(strLeafNode);
							} catch (NumberFormatException nfe) {
							}
						}
					}

					float completionPercentage = ((float) contentStatus.size() / leafNodeCount) * 100f;
					enrolmentReport.put(Constants.COMPLETION_PERCENTAGE, Float.toString(completionPercentage));
				}
			}

			List<String> reportFields = new ArrayList<String>();
			reportFields.addAll(Constants.USER_ENROLMENT_REPORT_FIELDS);
			reportFields.addAll(Constants.COURSE_ENROLMENT_REPORT_FIELDS);
			reportFields.addAll(Constants.USER_ENROLMENT_COMMON_FIELDS);
			reportService.generateUserEnrolmentReport(userEnrolmentMap, reportFields, response);
		} catch (Exception e) {
			log.error("Failed to generate report. Exception: ", e);
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg("Failed to generate report.");
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	public SBApiResponse getUserReport() {
		log.info("Starting user report...");
		long startTime = System.currentTimeMillis();
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_USER_REPORT);
		try {
			List<String> fields = new ArrayList<String>();
			fields.addAll(Constants.USER_ENROLMENT_REPORT_FIELDS);
			fields.add(Constants.ROLES);
			int index = 0;
			int size = 500;
			boolean isCompleted = false;

			List<Map<String, Object>> resultArray = new ArrayList<>();
			Map<String, Map<String, String>> userInfoMap = new HashMap<String, Map<String, String>>();
			Map<String, Object> result;

			final BoolQueryBuilder finalQuery = QueryBuilders.boolQuery();
			finalQuery.must(QueryBuilders.termQuery(Constants.STATUS, 1));
			SearchSourceBuilder sourceBuilder = null;
			Workbook wb = null;
			long userCount = 0l;
			do {
				sourceBuilder = new SearchSourceBuilder().query(finalQuery).from(index).size(size);
				sourceBuilder.fetchSource(serverConfig.getEsUserReportIncludeFields(), new String[] {});
				SearchResponse searchResponse = indexerService.getEsResult(serverConfig.getSbEsUserProfileIndex(),
						serverConfig.getEsProfileIndexType(), sourceBuilder, true);

				if (index == 0) {
					userCount = searchResponse.getHits().getTotalHits();
					log.info(String.format("Number of users in ES index : %s", userCount));
					wb = reportService.createReportWorkbook(fields);
				}
				for (SearchHit hit : searchResponse.getHits()) {
					result = hit.getSourceAsMap();
					resultArray.add(result);
				}
				processUserDetails(resultArray, userInfoMap);
				wb = reportService.appendData(wb, fields, userInfoMap);
				resultArray.clear();
				userInfoMap.clear();

				index = (int) Math.min(userCount, index + size);

				if (index == userCount) {
					isCompleted = true;
				}
			} while (!isCompleted);

			reportService.completeReportWorkbook(wb, response);

		} catch (Exception e) {
			log.error("Failed to generate user report. Exception: ", e);
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg("Failed to generate report.");
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info(String.format("Generate User Report Competed Oeration in %s seconds",
				TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)));
		return response;
	}

	private void processUserDetails(List<Map<String, Object>> userMapList,
			Map<String, Map<String, String>> userInfoMap) {
		for (Map<String, Object> user : userMapList) {
			Map<String, String> userInfo = new HashMap<String, String>();
			userInfo.put(Constants.USER_ID, (String) user.get(Constants.USER_ID));
			userInfo.put(Constants.FIRSTNAME, (String) user.get(Constants.FIRSTNAME));
			userInfo.put(Constants.ROOT_ORG_ID, (String) user.get(Constants.ROOT_ORG_ID));
			userInfo.put(Constants.CHANNEL, (String) user.get(Constants.CHANNEL));
			if (StringUtils.isNotBlank((String) user.get(Constants.EMAIL))) {
				String value = decryptService.decryptString((String) user.get(Constants.EMAIL));
				userInfo.put(Constants.EMAIL, value);
			}
			if (StringUtils.isNotBlank((String) user.get(Constants.PHONE))) {
				userInfo.put(Constants.PHONE, decryptService.decryptString((String) user.get(Constants.PHONE)));
			}
			String strRoles = "";
			List<Map<String, Object>> roles = (List<Map<String, Object>>) user.get(Constants.ROLES);
			for (Map<String, Object> role : roles) {
				String strRole = (String) role.get(Constants.ROLE);
				if (StringUtils.isNotBlank(strRoles)) {
					strRoles = strRoles.concat(", ").concat(strRole);
				}
				else {
					strRoles = StringUtils.isBlank(strRole) ? "" : strRole;
				}
			}
			userInfo.put(Constants.ROLES, strRoles);
			userInfoMap.put(userInfo.get(Constants.USER_ID), userInfo);
		}
	}

	private void enrichUserDetails(List<String> userIdList, Map<String, Map<String, String>> userInfoMap,
			Map<String, String> orgInfoMap) {
		long startTime = System.currentTimeMillis();
		userUtilityService.getUserDetailsFromDB(userIdList, Constants.USER_ENROLMENT_REPORT_FIELDS, userInfoMap);
		log.info(String.format("User enrichment took %s seconds", (System.currentTimeMillis() - startTime) / 1000));
		startTime = System.currentTimeMillis();
		Iterator<Entry<String, Map<String, String>>> it = userInfoMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Map<String, String>> item = it.next();
			String orgId = item.getValue().get(Constants.ROOT_ORG_ID);
			if (!orgInfoMap.containsKey(orgId)) {
				orgInfoMap.put(orgId, item.getValue().get(Constants.CHANNEL));
			}
		}
		log.info(String.format("Org enrichment took %s seconds", (System.currentTimeMillis() - startTime) / 1000));
	}

	private void enrichCourseDetails(List<String> courseIdList, Map<String, Map<String, String>> courseInfoMap,
			Map<String, String> orgInfoMap) {

		contentService.getLiveContentDetails(courseIdList,
				Arrays.asList(Constants.IDENTIFIER, Constants.NAME, Constants.CREATED_FOR, Constants.LEAF_NODES_COUNT),
				courseInfoMap);

		Iterator<Entry<String, Map<String, String>>> it = courseInfoMap.entrySet().iterator();

		List<String> orgIdList = new ArrayList<String>();
		while (it.hasNext()) {
			Entry<String, Map<String, String>> item = it.next();
			String orgId = item.getValue().get(Constants.COURSE_ORG_ID);
			if (orgInfoMap.containsKey(orgId)) {
				item.getValue().put(Constants.COURSE_ORG_NAME, orgInfoMap.get(orgId));
			} else if (StringUtils.isNotBlank(orgId)) {
				orgIdList.add(orgId);
			}
		}

		if (orgIdList.size() > 0) {
			extOrgService.getOrgDetailsFromDB(orgIdList, orgInfoMap);
			it = courseInfoMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Map<String, String>> item = it.next();
				String orgId = item.getValue().get(Constants.COURSE_ORG_ID);
				if (orgInfoMap.containsKey(orgId)) {
					item.getValue().put(Constants.COURSE_ORG_NAME, orgInfoMap.get(orgId));
				}
			}
		}
	}

	private void copyReportDetails(Map<String, String> enrolmentReport, Map<String, String> objectInfo,
			String objectType) {
		List<String> fields = ListUtils.EMPTY_LIST;
		switch (objectType) {
		case Constants.USER_CONST: {
			fields = Constants.USER_ENROLMENT_REPORT_FIELDS;
			break;
		}
		case Constants.COURSE: {
			fields = Constants.COURSE_ENROLMENT_REPORT_FIELDS;
			break;
		}
		}
		for (String field : fields) {
			if (objectInfo.containsKey(field)) {
				enrolmentReport.put(field, objectInfo.get(field));
			} else {
				enrolmentReport.put(field, StringUtils.EMPTY);
			}
		}
	}

	@Override
	public ResponseEntity<Resource> downloadFile(String fileName) {
		try {
			storageService.downloadFile(fileName);
			Path tmpPath = Paths.get(Constants.LOCAL_BASE_PATH + fileName);
			ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(tmpPath));
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
			return ResponseEntity.ok()
					.headers(headers)
					.contentLength(tmpPath.toFile().length())
					.contentType(MediaType.parseMediaType(MediaType.MULTIPART_FORM_DATA_VALUE))
					.body(resource);
		} catch (IOException e) {
			log.error("Failed to read the downloaded file: " + fileName + ", Exception: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} finally {
			try {
				File file = new File(Constants.LOCAL_BASE_PATH + fileName);
				if(file.exists()) {
					file.delete();
				}
			} catch(Exception e1) {
			}
		}
	}

	@Override
	public SBApiResponse getGroupList() {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.USER_REGISTRATION_GROUP_LIST);
		List<String> groupList = serverConfig.getBulkUploadGroupValue();
		if (CollectionUtils.isNotEmpty(groupList)) {
			response.getResult().put(Constants.COUNT, groupList.size());
			response.getResult().put(Constants.RESPONSE, groupList);
		} else {
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			response.getParams().setStatus(Constants.FAILED);
		}
		return response;
	}

	@Override
	public SBApiResponse profileMDOAdminUpdate(Map<String, Object> request, String userToken, String authToken, String rootOrgId) throws Exception {
		SBApiResponse response = new SBApiResponse(Constants.API_PROFILE_UPDATE);
		try {
			Map<String, Object> requestData = (Map<String, Object>) request.get(Constants.REQUEST);
			if (!validateRequest(requestData)) {
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				response.getParams().setStatus(Constants.FAILED);
				return response;
			}
			String userId = (String) requestData.get(Constants.USER_ID);
			Map<String, Object> profileDetailsMap = (Map<String, Object>) requestData.get(Constants.PROFILE_DETAILS);
			List<String> allowedAdminUpdateFields = adminApprovalFields();
			Map<String, Object> adminUpdateMap = new HashMap<>();
			for (String key : profileDetailsMap.keySet()) {
				if (allowedAdminUpdateFields.contains(key)) {
					adminUpdateMap.put(key, profileDetailsMap.get(key));
				}
			}


			Map<String, String> headerValues = new HashMap<>();
			headerValues.put(Constants.AUTH_TOKEN, authToken);
			headerValues.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);

			Map<String, Object> responseMap = userUtilityService.getUsersReadData(userId, StringUtils.EMPTY,
					StringUtils.EMPTY);
			Map<String, Object> existingProfileDetails = (Map<String, Object>) responseMap.get(Constants.PROFILE_DETAILS);
			String updatedProfileStatus = null;
			String updatedGroupVal = null;
			String updatedDesignationVal = null;

			if (!profileDetailsMap.isEmpty()) {
				List<String> listOfChangedDetails = new ArrayList<>();
				for (String keys : profileDetailsMap.keySet()) {
					listOfChangedDetails.add(keys);
				}
				boolean isGroupOrDesignationUpdated = false;
				for (String changedObj : listOfChangedDetails) {
					if (profileDetailsMap.get(changedObj) instanceof String) {
						existingProfileDetails.put(changedObj, profileDetailsMap.get(changedObj));
						if (Constants.PROFILE_STATUS.equalsIgnoreCase(changedObj)) {
							updatedProfileStatus = (String) profileDetailsMap.get(changedObj);
						}
					} else if (profileDetailsMap.get(changedObj) instanceof ArrayList) {
						// KB-3718
						if (Constants.PROFESSIONAL_DETAILS.equalsIgnoreCase(changedObj)) {
							List<Map<String, Object>> professionalList = (List<Map<String, Object>>) existingProfileDetails
									.get(Constants.PROFESSIONAL_DETAILS);
							Map<String, Object> existingProfessionalDetailsMap = null;
							
							// professional detail is empty... just replace...
							if (CollectionUtils.isEmpty(professionalList)) {
								existingProfileDetails.put(changedObj, profileDetailsMap.get(changedObj));
								professionalList = (List<Map<String, Object>>) existingProfileDetails
										.get(Constants.PROFESSIONAL_DETAILS);
								existingProfessionalDetailsMap = professionalList.get(0);
								isGroupOrDesignationUpdated = true;
							} else {
								existingProfessionalDetailsMap = professionalList.get(0);
								Map<String, Object> updatedProfessionalDetailsMap = ((List<Map<String, Object>>) profileDetailsMap
										.get(changedObj)).get(0);
								for (String childKey : updatedProfessionalDetailsMap.keySet()) {
									String updatedValue = (String) updatedProfessionalDetailsMap.get(childKey);
									if ((Constants.GROUP.equalsIgnoreCase(childKey)
											|| Constants.DESIGNATION.equalsIgnoreCase(childKey)) &&
											!updatedValue.equalsIgnoreCase(
													(String) existingProfessionalDetailsMap.get(childKey))) {
										isGroupOrDesignationUpdated = true;
									}
									existingProfessionalDetailsMap.put(childKey,
											updatedProfessionalDetailsMap.get(childKey));
								}
							}
							updatedGroupVal = (String) existingProfessionalDetailsMap.get(Constants.GROUP);
							updatedDesignationVal = (String) existingProfessionalDetailsMap.get(Constants.DESIGNATION);
						} else {
							existingProfileDetails.put(changedObj, profileDetailsMap.get(changedObj));
						}
					} else if (profileDetailsMap.get(changedObj) instanceof Boolean) {
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

				if (Constants.VERIFIED.equalsIgnoreCase(updatedProfileStatus)) {
					existingProfileDetails.put(Constants.PROFILE_GROUP_STATUS, Constants.VERIFIED);
					existingProfileDetails.put(Constants.PROFILE_DESIGNATION_STATUS, Constants.VERIFIED);
					isGroupOrDesignationUpdated = true;
				} else if (Constants.NOT_VERIFIED.equalsIgnoreCase(updatedProfileStatus)
						|| Constants.NOT_MY_USER.equalsIgnoreCase(updatedProfileStatus)) {
					//If marked as "NOT-VERIFIED" then no need to change the details.
					isGroupOrDesignationUpdated = false;
					existingProfileDetails.put(Constants.PROFILE_GROUP_STATUS, Constants.NOT_VERIFIED);
					existingProfileDetails.put(Constants.PROFILE_DESIGNATION_STATUS, Constants.NOT_VERIFIED);
				}
				if (isGroupOrDesignationUpdated) {
					if (StringUtils.isNotBlank(updatedGroupVal)) {
						existingProfileDetails.put(Constants.PROFILE_GROUP_STATUS, Constants.VERIFIED);
					} else {
						existingProfileDetails.put(Constants.PROFILE_GROUP_STATUS,
								Constants.NOT_VERIFIED);
					}
					if (StringUtils
							.isNotBlank(updatedDesignationVal)) {
						existingProfileDetails.put(Constants.PROFILE_DESIGNATION_STATUS,
								Constants.VERIFIED);
					} else {
						existingProfileDetails.put(Constants.PROFILE_DESIGNATION_STATUS,
								Constants.NOT_VERIFIED);
					}

					if (Constants.VERIFIED.equalsIgnoreCase(
							(String) existingProfileDetails.get(Constants.PROFILE_GROUP_STATUS)) &&
							Constants.VERIFIED.equalsIgnoreCase((String) existingProfileDetails
									.get(Constants.PROFILE_DESIGNATION_STATUS))) {
						existingProfileDetails.put(Constants.PROFILE_STATUS, Constants.VERIFIED);
					} else {
						existingProfileDetails.put(Constants.PROFILE_STATUS, Constants.NOT_VERIFIED);
					}
				
					SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
					sdf.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
					String timeStamp = sdf.format(new java.util.Date());
					existingProfileDetails.put(Constants.PROFILE_STATUS_UPDATED_ON, timeStamp);
					Map<String, Object> additionalProperties = (Map<String, Object>) existingProfileDetails
							.get(Constants.ADDITIONAL_PROPERTIES);
					if (ObjectUtils.isEmpty(additionalProperties)) {
						additionalProperties = new HashMap<>();
					}
					additionalProperties.put(Constants.PROFILE_STATUS_UPDATED_MSG_VIEWED, false);
					existingProfileDetails.put(Constants.ADDITIONAL_PROPERTIES, additionalProperties);
				}

				HashMap<String, String> headerValue = new HashMap<>();
				headerValues.put(Constants.AUTH_TOKEN, authToken);
				headerValues.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
				String updatedUrl = serverConfig.getSbUrl() + serverConfig.getLmsUserUpdatePrivatePath();
				Map<String, Object> updateRequestValue = requestData;
				updateRequestValue.put(Constants.PROFILE_DETAILS, existingProfileDetails);
				Map<String, Object> updateRequest = new HashMap<>();
				updateRequest.put(Constants.REQUEST, updateRequestValue);
				Map<String, Object> updateResponse = outboundRequestHandlerService.fetchResultUsingPatch(updatedUrl, updateRequest, headerValue);

				if (Constants.OK.equalsIgnoreCase((String) updateResponse.get(Constants.RESPONSE_CODE))) {
					response.setResponseCode(HttpStatus.OK);
					response.getResult().put(Constants.RESPONSE, Constants.SUCCESS);
					response.getParams().setStatus(Constants.SUCCESS);
				} else {
					if (updateResponse != null && Constants.CLIENT_ERROR.equalsIgnoreCase((String) updateResponse.get(Constants.RESPONSE_CODE))) {
						response.setResponseCode(HttpStatus.BAD_REQUEST);
					} else {
						response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
					}
					response.getParams().setStatus(Constants.FAILED);
					String errMsg = (String) ((Map<String, Object>) updateResponse.get(Constants.PARAMS)).get(Constants.ERROR_MESSAGE);
					errMsg = PropertiesCache.getInstance().readCustomError(errMsg);
					response.getParams().setErrmsg(errMsg);
					log.error(errMsg, new Exception(errMsg));
					return response;
				}
			}
		} catch (Exception e) {
			log.error("Failed to process profile update. Exception: ", e);
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErr(e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;

	}

	@Override
	public SBApiResponse profileExternalSystemUpdate(Map<String, Object> request, String authToken) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_PROFILE_EXTERNAL_SYSTEM_UPDATE);
		try {
			Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
			if (!validateSystemUpdateRequest(requestBody)) {
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				response.getParams().setStatus(Constants.FAILED);
				return response;
			}
			String userIdFromToken = accessTokenValidator.fetchUserIdFromAccessToken(authToken);
			String email = (String)requestBody.get(Constants.EMAIL);
			Map<String, Object> userLookupInfo = userUtilityService.getUsersDataFromLookup(email, authToken);
			if (ObjectUtils.isEmpty(userLookupInfo)) {
				response.getParams().setStatus(Constants.FAILED);
				response.getParams().setErr("Not able to find user with email: " + email);
				response.setResponseCode(HttpStatus.NOT_FOUND);
				return response;
			}
			String userId = (String)userLookupInfo.get(Constants.ID);
			if (StringUtils.isNotEmpty(userId)) {
				Map<String, Map<String, String>> userInfoMap = new HashMap<>();
				userUtilityService.getUserDetailsFromDB(Arrays.asList(userId), Arrays.asList(Constants.PROFILE_DETAILS_LOWER, Constants.USER_ID), userInfoMap);
				if (!(ObjectUtils.isEmpty(userInfoMap))) {
					Map<String, String> userInfo = userInfoMap.get(userId);
					String profileDetails = (String) userInfo.get(Constants.PROFILE_DETAILS_LOWER);
					Map<String, Object> profileDetailsMap = new HashMap<>();
					if (StringUtils.isNotEmpty(profileDetails)) {
						profileDetailsMap = mapper.readValue(profileDetails, new TypeReference<HashMap<String, Object>>() {
						});
					}
					Map<String, Object> additionalProperties = (Map<String, Object>)profileDetailsMap.get(Constants.ADDITIONAL_PROPERTIES);
					if (ObjectUtils.isEmpty(additionalProperties)) {
						additionalProperties = new HashMap<>();
					}
					additionalProperties.put(Constants.EXTERNAL_SYSTEM, requestBody.get(Constants.EXTERNAL_SYSTEM));
					additionalProperties.put(Constants.EXTERNAL_SYSTEM_ID, requestBody.get(Constants.EXTERNAL_SYSTEM_ID));
					profileDetailsMap.put(Constants.ADDITIONAL_PROPERTIES, additionalProperties);
					Map<String, Object> compositeKey = new HashMap<>();
					compositeKey.put(Constants.ID, userId);

					Map<String, Object> userInfoUpdated = new HashMap<>();
					userInfoUpdated.put(Constants.PROFILE_DETAILS_KEY, mapper.writeValueAsString(profileDetailsMap));
					userInfoUpdated.put(Constants.UPDATED_BY, userIdFromToken);
					userInfoUpdated.put(Constants.UPDATED_DATE, ProjectUtil.getFormattedDate());
					Map<String, Object> resp = cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD,
							Constants.TABLE_USER, userInfoUpdated, compositeKey);
					if (resp.get(Constants.RESPONSE).equals(Constants.SUCCESS)) {
						String errMsg = syncUserData(userId);
						if (StringUtils.isEmpty(errMsg)) {
							response.setResponseCode(HttpStatus.OK);
							response.getResult().put(Constants.RESPONSE, Constants.SUCCESS);
							response.getParams().setStatus(Constants.SUCCESS);
							return response;
						} else {
							response.getParams().setStatus(Constants.FAILED);
							response.getParams().setErr(errMsg);
							response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
						}
					} else {
						response.getParams().setStatus(Constants.FAILED);
						response.getParams().setErr((String) resp.get(Constants.ERROR_MESSAGE));
						response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
			}
		} catch (Exception e) {
			log.error("Failed to process profile update. Exception: ", e);
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErr(e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	private boolean validateSystemUpdateRequest(Map<String, Object> requestBody) {
		if (ObjectUtils.isEmpty(requestBody.get(Constants.EMAIL))
				 || ObjectUtils.isEmpty(requestBody.get(Constants.EXTERNAL_SYSTEM))  || ObjectUtils.isEmpty(requestBody.get(Constants.EXTERNAL_SYSTEM_ID))) {
			return false;
		} else {
			return true;
		}
	}

	public List<String> adminApprovalFields() {
		List<String> adminApprovalFields = (List<String>) dataCacheMgr
				.getObjectFromCache(serverConfig.getMdoAdminUpdateUsers());
		if (CollectionUtils.isEmpty(adminApprovalFields)) {
			Map<String, Object> searchRequest = new HashMap<String, Object>();
			searchRequest.put(Constants.ID, serverConfig.getMdoAdminUpdateUsers());

			List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
					Constants.KEYSPACE_SUNBIRD, Constants.TABLE_SYSTEM_SETTINGS, searchRequest, null);
			if (CollectionUtils.isNotEmpty(existingDataList)) {
				Map<String, Object> data = existingDataList.get(0);
				String strAdminApprovalFields = (String) data.get(Constants.VALUE);

				if (StringUtils.isNotBlank(strAdminApprovalFields)) {
					String strArray[] = strAdminApprovalFields.split(",", -1);
					adminApprovalFields = Arrays.asList(strArray);
					dataCacheMgr.putObjectInCache(serverConfig.getMdoAdminUpdateUsers(), adminApprovalFields);
					return adminApprovalFields;
				}
			}

		} else {
			return adminApprovalFields;
		}
		return new ArrayList<>();
	}

	private boolean isFileExistForProcessingForMDO(String mdoId) {
		Map<String, Object> bulkUplaodPrimaryKey = new HashMap<String, Object>();
		bulkUplaodPrimaryKey.put(Constants.ROOT_ORG_ID, mdoId);
		List<String> fields = Arrays.asList(Constants.ROOT_ORG_ID, Constants.IDENTIFIER, Constants.STATUS);

		List<Map<String, Object>> bulkUploadMdoList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
				Constants.KEYSPACE_SUNBIRD, Constants.TABLE_USER_BULK_UPLOAD, bulkUplaodPrimaryKey, fields);
		if (CollectionUtils.isEmpty(bulkUploadMdoList)) {
			return false;
		}
		return bulkUploadMdoList.stream()
				.anyMatch(entry -> Constants.STATUS_IN_PROGRESS_UPPERCASE.equalsIgnoreCase((String) entry.get(Constants.STATUS)));
	}

	private void setErrorDataForMdo(SBApiResponse response, String errMsg) {
		response.getParams().setStatus(Constants.FAILED);
		response.getParams().setErrmsg(errMsg);
		response.setResponseCode(HttpStatus.TOO_MANY_REQUESTS);
	}


	/**
	 * Updates the user profile with version 2.
	 *
	 * @param request   The request containing profile update data.
	 * @param userToken The user token for authentication.
	 * @param authToken The authentication token.
	 * @param rootOrgId The root organization ID.
	 * @return SBApiResponse object containing the response of the profile update.
	 */
	@Override
	public SBApiResponse profileUpdateV2(Map<String, Object> request, String userToken, String authToken, String rootOrgId) {
		SBApiResponse response = new SBApiResponse(Constants.API_PROFILE_UPDATE);
		// Extracting the request map from the input request
		Map<String, Object> requestMap = (Map<String, Object>) request.get(Constants.REQUEST);
		// Validating the token from the request map
		try {
			String contextTokenValue=validateToken((String) requestMap.get(Constants.CONTEXT_TOKEN));
			if(contextTokenValue.equalsIgnoreCase(Constants.CONTEXT_TYPE)){
				return profileUpdate(request, userToken, authToken, rootOrgId);
			}else{
				log.error("Failed to process the request since the parameter is missing: " + Constants.CONTEXT_TYPE);
				response.getParams().setStatus(Constants.FAILED);
				response.getParams().setErr("Failed to process the request since the parameter is missing: " + Constants.CONTEXT_TYPE);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			log.error("Failed to validate the token: ", e);
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErr(e.getMessage());
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}


	/**
	 * Validates the given JWT token.
	 *
	 * @param token The JWT token to validate.
	 * @return True if the token is valid; false otherwise.
	 * @throws Exception if any error occurs during token validation.
	 */
	public String validateToken(String token) throws Exception {
		try {
			// Parsing the token and extracting its subject (claims)
			String tokens = Jwts.parser().setSigningKey(serverConfig.getSecretKeyTokenValidation()).parseClaimsJws(token).getBody().getSubject();
			// Parsing the subject (claims) into a map using Jackson ObjectMapper
			Map<String, String> parsedMap = mapper.readValue(tokens, new TypeReference<Map<String, String>>() {
			});
			if (!org.apache.commons.lang.StringUtils.isEmpty(parsedMap.get(Constants.CONTEXT_TYPE))) {
				return Constants.CONTEXT_TYPE;
			}else {
				return Constants.ERROR;
			}
		} catch (ExpiredJwtException e) {
			// Handling ExpiredJwtException
			throw new Exception("ExpiredJwt exception has occurred: " , e);
		} catch (UnsupportedJwtException e) {
			// Handling UnsupportedJwtException
			throw new Exception("UnsupportedJwt exception has occurred: " , e);
		} catch (MalformedJwtException e) {
			// Handling MalformedJwtException
			throw new Exception("MalformedJwt exception has occurred: " , e);
		} catch (SignatureException e) {
			// Handling SignatureException
			throw new Exception("Signature exception has occurred: " , e);
		} catch (IllegalArgumentException e) {
			// Handling IllegalArgumentException
			throw new Exception("IllegalArgumentException has occurred: " , e);
		} catch (JsonMappingException e) {
			// Handling JsonMappingException
			throw new Exception("Json Mapping exception has occurred: " , e);
		} catch (JsonParseException e) {
			// Handling JsonParseException
			throw new Exception("Json Parsing exception has occurred: " , e);
		} catch (IOException e) {
			// Handling IOException
			throw new Exception("IOException has occurred: " , e);
		}
    }

	/**
	 * Method to validate if the provided email or phone number already exists in the system.
	 *
	 * @param profileDetailsMap A map containing the profile details.
	 * @return Constants.OK if neither email nor phone number exists, Constants.EMAIL_EXIST_ERROR if email already exists,
	 *         Constants.PHONE_NUMBER_EXIST_ERROR if phone number already exists.
	 */
	private String validateExistingPhoneEmail(Map<String, Object> profileDetailsMap) {
		if (profileDetailsMap.containsKey(Constants.PERSONAL_DETAILS)) {
			Map<String, Object> personalDetails = (Map<String, Object>) profileDetailsMap.get(Constants.PERSONAL_DETAILS);
			if (personalDetails.containsKey(Constants.PRIMARY_EMAIL) && (userUtilityService.isUserExist(Constants.EMAIL, (String) personalDetails.get(Constants.PRIMARY_EMAIL)))) {
				return Constants.EMAIL_EXIST_ERROR;
			}
			if (personalDetails.containsKey(Constants.PHONE) && (userUtilityService.isUserExist(Constants.PHONE, (String) personalDetails.get(Constants.PHONE)))) {
				return Constants.PHONE_NUMBER_EXIST_ERROR;
			}
		}
		return "";
	}

}







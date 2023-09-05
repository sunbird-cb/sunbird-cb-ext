package org.sunbird.progress.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SearchUserApiContent;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.model.SunbirdUserProfileDetail;
import org.sunbird.common.service.ContentServiceImpl;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.progress.model.BatchEnrolment;
import org.sunbird.progress.model.MandatoryContentInfo;
import org.sunbird.progress.model.MandatoryContentResponse;
import org.sunbird.progress.model.UserProgressRequest;
import org.sunbird.user.service.UserUtilityServiceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MandatoryContentServiceImpl implements MandatoryContentService {

	@Autowired
	private OutboundRequestHandlerServiceImpl outboundReqService;

	@Autowired
	private CbExtServerProperties cbExtServerProperties;

	@Autowired
	private CassandraOperation cassandraOperation;

	@Autowired
	private UserUtilityServiceImpl userUtilService;

	@Autowired
	private ContentServiceImpl contentService;

	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	private ObjectMapper mapper = new ObjectMapper();

	@Override
	public MandatoryContentResponse getMandatoryContentStatusForUser(String authUserToken, String rootOrg, String org,
			String userId) {
		MandatoryContentResponse response = new MandatoryContentResponse();

		Map<String, Object> propertyMap = new HashMap<>();
		propertyMap.put(Constants.ROOT_ORG, rootOrg);
		propertyMap.put(Constants.ORG, org);
		List<Map<String, Object>> contentList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
				Constants.TABLE_MANDATORY_USER_CONTENT, propertyMap, new ArrayList<>());

		if (CollectionUtils.isEmpty(contentList)) {
			logger.info("getMandatoryContentStatusForUser: There are no mandatory Content set in DB.");
			return response;
		}

		for (Map<String, Object> content : contentList) {
			String contentId = (String) content.get(Constants.CONTENT_ID);
			content.remove(Constants.CONTENT_ID);
			MandatoryContentInfo info = mapper.convertValue(content, MandatoryContentInfo.class);
			response.addContentInfo(contentId, info);
		}

		try {
			logger.info("getMandatoryContentStatusForUser: MandatoryCourse Details : "
					+ new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(response));
		} catch (JsonProcessingException e) {
			logger.error(e);
		}
		enrichProgressDetails(authUserToken, response, userId);
		try {
			logger.info("getMandatoryContentStatusForUser: Ret Value is: "
					+ new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(response));
		} catch (JsonProcessingException e) {
			logger.error(e);
		}
		Iterator<MandatoryContentInfo> entries = response.getContentDetails().values().iterator();
		boolean isCompleted = false;
		while (entries.hasNext()) {
			MandatoryContentInfo entry = entries.next();
			if (entry.getUserProgress() < entry.getMinProgressForCompletion()) {
				response.setMandatoryCourseCompleted(false);
				isCompleted = false;
				break;
			} else {
				isCompleted = true;
			}
		}
		if (isCompleted) {
			response.setMandatoryCourseCompleted(true);
		}
		return response;
	}

	public void enrichProgressDetails(String authUserToken, MandatoryContentResponse mandatoryContentInfo,
			String userId) {
		HashMap<String, Object> req;
		HashMap<String, Object> reqObj;
		List<String> fields = Arrays.asList("progressdetails");
		HashMap<String, String> headersValues = new HashMap<>();
		headersValues.put("X-Authenticated-User-Token", authUserToken);
		headersValues.put("Authorization", cbExtServerProperties.getSbApiKey());
		for (Map.Entry<String, MandatoryContentInfo> infoMap : mandatoryContentInfo.getContentDetails().entrySet()) {
			try {
				req = new HashMap<>();
				reqObj = new HashMap<>();
				reqObj.put("userId", userId);
				reqObj.put("courseId", infoMap.getKey());
				reqObj.put("batchId", infoMap.getValue().getBatchId());
				reqObj.put("fields", fields);
				req.put("request", reqObj);
				Map<String, Object> response = outboundReqService.fetchResultUsingPost(
						cbExtServerProperties.getCourseServiceHost() + cbExtServerProperties.getProgressReadEndPoint(),
						req, headersValues);
				if (response.get("responseCode").equals("OK")) {
					List<Object> result = (List<Object>) ((HashMap<String, Object>) response.get("result"))
							.get("contentList");
					if (!CollectionUtils.isEmpty(result)) {
						Optional<Object> optionResult = result.stream().findFirst();
						if (optionResult.isPresent()) {
							Map<String, Object> content = (Map<String, Object>) optionResult.get();
							BigDecimal progress = new BigDecimal(content.get("completionPercentage").toString());
							mandatoryContentInfo.getContentDetails().get(infoMap.getKey())
									.setUserProgress(progress.floatValue());
						}
					}
				}
			} catch (Exception ex) {
				logger.error(ex);
			}
		}
	}

	public Map<String, Object> getUserProgress(SunbirdApiRequest requestBody, String authUserToken, String rootOrgId, String userChannel) {
		Map<String, Object> result = new HashMap<>();
		try {
			UserProgressRequest requestData = validateGetBatchEnrolment(requestBody);
			if (ObjectUtils.isEmpty(requestData)) {
				result.put(Constants.STATUS, Constants.FAILED);
				result.put(Constants.MESSAGE, "check your request params");
				return result;
			}
			int courseLeafCount = getLeafCountForTheCourse(rootOrgId, requestData.getCourseId(), userChannel);
			// get all enrolled details
			List<String> enrollmentIdList = null;
			List<Map<String, Object>> userEnrolmentList = new ArrayList<>();
			for (BatchEnrolment request : requestData.getBatchList()) {
				Map<String, Object> propertyMap = new HashMap<>();
				propertyMap.put(Constants.BATCH_ID, request.getBatchId());
				if (request.getUserList() != null && !request.getUserList().isEmpty()) {
					enrollmentIdList = request.getUserList();
				} else{
					List<Map<String, Object>> usersEnrolledInTheBatch = cassandraOperation.getRecordsByPropertiesWithoutFiltering(Constants.KEYSPACE_SUNBIRD_COURSES,
							Constants.TABLE_ENROLLMENT_BATCH_LOOKUP,
							propertyMap,
							Arrays.asList(Constants.USER_ID, Constants.BATCH_ID,Constants.ACTIVE));
					enrollmentIdList = usersEnrolledInTheBatch.stream().filter(obj -> (Boolean) obj.get(Constants.ACTIVE)).map(obj -> (String) obj.get(Constants.USER_ID_CONSTANT)).collect(Collectors.toList());
				}
				propertyMap.put(Constants.USER_ID, enrollmentIdList);
				propertyMap.put(Constants.COURSE_ID,requestData.getCourseId());
				userEnrolmentList.addAll(cassandraOperation.getRecordsByPropertiesWithoutFiltering(Constants.KEYSPACE_SUNBIRD_COURSES,
						Constants.TABLE_USER_ENROLMENT, propertyMap,
						Arrays.asList(Constants.USER_ID_CONSTANT, Constants.COURSE_ID,
								Constants.BATCH_ID, Constants.COMPLETION_PERCENTAGE, Constants.PROGRESS,
								Constants.STATUS, Constants.ISSUED_CERTIFICATES)));
				if(userEnrolmentList.size() > 100)
					break;
			}
			// restricting with only 100 items in the response
			if (userEnrolmentList.size() > 100) {
				userEnrolmentList = userEnrolmentList.subList(0, 100);
			}

			//get id list from userEnrollmentList, in case  request has more than one batch
			List<String> enrolledUserIdList = userEnrolmentList.stream()
					.map(obj -> (String) obj.get(Constants.USER_ID_CONSTANT)).collect(Collectors.toList());

			//inside loop iterating batch list
			List<String> userFields = Arrays.asList(Constants.USER_ID_CONSTANT, Constants.FIRSTNAME, Constants.PROFILE_DETAILS_PRIMARY_EMAIL, Constants.CHANNEL,
					Constants.PROFILE_DETAILS_DESIGNATION, Constants.PROFILE_DETAILS_DESIGNATION_OTHER);
			Map<String, Object> userMap = userUtilService.getUsersDataFromUserIds(enrolledUserIdList, userFields,
					authUserToken);


			for (Map<String, Object> responseObj : userEnrolmentList) {
				// set user details
				if (userMap.containsKey(responseObj.get(Constants.USER_ID_CONSTANT))) {
					SearchUserApiContent userObj = mapper.convertValue(
							userMap.get(responseObj.get(Constants.USER_ID_CONSTANT)), SearchUserApiContent.class);
					appendUserDetails(responseObj, userObj);
				}
				// set completion percentage & status
				setCourseCompletiondetails(responseObj, courseLeafCount);
			}
			result.put(Constants.STATUS, Constants.SUCCESSFUL);
			result.put(Constants.RESULT, userEnrolmentList);
		} catch (Exception ex) {
			result.put(Constants.STATUS, Constants.FAILED);
			logger.error(ex);
		}
		return result;
	}

	private UserProgressRequest validateGetBatchEnrolment(SunbirdApiRequest requestBody) {
		try {
			UserProgressRequest userProgressRequest = new UserProgressRequest();
			if (!ObjectUtils.isEmpty(requestBody.getRequest())) {
				userProgressRequest = mapper.convertValue(requestBody.getRequest(), UserProgressRequest.class);
			}
			if(ObjectUtils.isEmpty(userProgressRequest.getCourseId())){
				return null;
			}
			if (!userProgressRequest.getBatchList().isEmpty()) {
				for (BatchEnrolment batchEnrolment : userProgressRequest.getBatchList()) {
					if (ObjectUtils.isEmpty(batchEnrolment.getBatchId())) {
						return null;
					}
				}
				return userProgressRequest;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	/**
	 * Add user basic details with the response map
	 *
	 * @param response
	 * @param userMap
	 * @throws Exception
	 */
	private void appendUserDetails(Map<String, Object> responseObj, SearchUserApiContent userObj) throws Exception {
		if (!ObjectUtils.isEmpty(userObj)) {
			responseObj.put(Constants.FIRSTNAME, userObj.getFirstName());
			responseObj.put(Constants.DEPARTMENT, userObj.getChannel());

			if (!ObjectUtils.isEmpty(userObj.getProfileDetails())) {
				SunbirdUserProfileDetail profileDetails = userObj.getProfileDetails();
				if (!ObjectUtils.isEmpty(profileDetails.getPersonalDetails())
						&& profileDetails.getPersonalDetails().containsKey(Constants.PRIMARY_EMAIL)) {
					responseObj.put(Constants.EMAIL, profileDetails.getPersonalDetails().get(Constants.PRIMARY_EMAIL));
				}
				if (!ObjectUtils.isEmpty(profileDetails.getProfessionalDetails())
						&& !profileDetails.getProfessionalDetails().isEmpty()) {
					if (!ObjectUtils.isEmpty(profileDetails.getProfessionalDetails().get(0).getDesignation())) {
						responseObj.put(Constants.DESIGNATION,
								profileDetails.getProfessionalDetails().get(0).getDesignation());
					} else if (!ObjectUtils
							.isEmpty(profileDetails.getProfessionalDetails().get(0).getDesignationOther())) {
						responseObj.put(Constants.DESIGNATION,
								profileDetails.getProfessionalDetails().get(0).getDesignationOther());
					} else {
						responseObj.put(Constants.DESIGNATION, "");
					}
				} else {
					responseObj.put(Constants.DESIGNATION, "");
				}
			}
		}
	}

	/**
	 * To update the course completion status & percentage
	 *
	 * @param responseObj
	 * @param leafNodeCount
	 */
	private void setCourseCompletiondetails(Map<String, Object> responseObj, int leafNodeCount) {
		int progress = (int) responseObj.get(Constants.PROGRESS);
		if (progress == 0) {
			responseObj.put(Constants.COMPLETION_PERCENTAGE, 0);
			responseObj.put(Constants.STATUS, 0);
		} else if (progress >= 1 && progress < leafNodeCount) {
			responseObj.put(Constants.COMPLETION_PERCENTAGE, (progress * 100) / leafNodeCount);
			responseObj.put(Constants.STATUS, 1);
		} else {
			responseObj.put(Constants.COMPLETION_PERCENTAGE, 100);
			responseObj.put(Constants.STATUS, 2);
		}
	}

	private int getLeafCountForTheCourse(String rootOrgId, String courseId, String userChannel){
		int leafCountForTheCOurse = 0;
		Map<String, Object> contentResponse = contentService.searchLiveContent(rootOrgId, courseId, userChannel);
		if (!ObjectUtils.isEmpty(contentResponse)) {
			Map<String, Object> contentResult = (Map<String, Object>) contentResponse.get(Constants.RESULT);
			if (0 < (Integer) contentResult.get(Constants.COUNT)) {
				List<Map<String, Object>> contentList = (List<Map<String, Object>>) contentResult
						.get(Constants.CONTENT);
				Map<String, Object> content = contentList.get(0);
				leafCountForTheCOurse = (Integer)content.get(Constants.LEAF_NODES_COUNT);
			}
		}
		return leafCountForTheCOurse;
	}
}

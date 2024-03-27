package org.sunbird.assessment.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.sunbird.assessment.repo.CohortUsers;
import org.sunbird.assessment.repo.UserAssessmentTopPerformerRepository;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.*;
import org.sunbird.common.service.ContentService;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.user.service.UserUtilityService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

@Service
public class CohortsServiceImpl implements CohortsService {

	public static final String TS_CREATED = "ts_created";
	private  final CbExtLogger logger = new CbExtLogger(getClass().getName());


	UserUtilityService userUtilService;


	ContentService contentService;


	UserAssessmentTopPerformerRepository userAssessmentTopPerformerRepo;


	OutboundRequestHandlerServiceImpl outboundRequestHandlerService;


	CbExtServerProperties cbExtServerProperties;

	CassandraOperation cassandraOperation;
	@Autowired
	public CohortsServiceImpl(UserUtilityService userUtilService, ContentService contentService, UserAssessmentTopPerformerRepository userAssessmentTopPerformerRepo, OutboundRequestHandlerServiceImpl outboundRequestHandlerService, CbExtServerProperties cbExtServerProperties, CassandraOperation cassandraOperation) {
		this.userUtilService = userUtilService;
		this.contentService = contentService;
		this.userAssessmentTopPerformerRepo = userAssessmentTopPerformerRepo;
		this.outboundRequestHandlerService = outboundRequestHandlerService;
		this.cbExtServerProperties = cbExtServerProperties;
		this.cassandraOperation = cassandraOperation;
	}

	@Override
	public List<CohortUsers> getTopPerformers(String rootOrg, String contentId, String userId, int count) {
		// Check User exists
		// This contains the list of all the children for provided course(resourceId) if
		// it is a learning-path.
		// Else, it will contain the parents for provided course(resourceId)
		List<String> assessmentIdList = new ArrayList<>();
		assessmentIdList.add(contentId);
		processChildContentId(contentId, assessmentIdList);

		// fetch top learners
		List<Map<String, Object>> topLearnerRecords = new ArrayList<>();

		if (!assessmentIdList.isEmpty()) {
			topLearnerRecords = userAssessmentTopPerformerRepo
					.findByPrimaryKeyRootOrgAndPrimaryKeyParentSourceIdIn(rootOrg, assessmentIdList);
			Collections.sort(topLearnerRecords, (m1, m2) -> {
				if (m1.get(TS_CREATED) != null && m2.get(TS_CREATED) != null) {
					return ((Date) m2.get(TS_CREATED)).compareTo((Date) (m1.get(TS_CREATED)));
				} else {
					return 1;
				}
			});
		}

		int counter = 1;
		Set<String> topLearnerUUIDSet = new HashSet<>();

		for (Map<String, Object> topLearnerRow : topLearnerRecords) {
			topLearnerUUIDSet.add(topLearnerRow.get("user_id").toString());
		}
		List<String> topLearnierIdList = new ArrayList<>(topLearnerUUIDSet);
		Map<String, Object> learnerUUIDEmailMap = new HashMap<>();
		if (!topLearnerUUIDSet.isEmpty()) {
			learnerUUIDEmailMap = userUtilService.getUsersDataFromUserIds(rootOrg, topLearnierIdList,
					new ArrayList<>(Arrays.asList(Constants.FIRST_NAME, Constants.EMAIL,
							Constants.DEPARTMENT_NAME)));
			logger.info("enrichDepartmentInfo UserIds -> " + topLearnierIdList.toString() + ", fetched Information -> "
					+ learnerUUIDEmailMap.size());
		}

		List<String> userNames = new ArrayList<>();
		List<CohortUsers> topPerformers = new ArrayList<>();
		for (Map<String, Object> topLearnerRow : topLearnerRecords) {
			// Same Logic as before
			String topLearnerUUID = topLearnerRow.get("user_id").toString();

			if (learnerUUIDEmailMap != null && learnerUUIDEmailMap.containsKey(topLearnerUUID)) {
				OpenSaberApiUserProfile userProfile = (OpenSaberApiUserProfile) learnerUUIDEmailMap.get(topLearnerUUID);
				if (!userNames.contains(userProfile.getPersonalDetails().getPrimaryEmail())
						&& !topLearnerUUID.equalsIgnoreCase(userId)) {
					CohortUsers user = new CohortUsers();
					user.setDesc("Top Learner");
					user.setUserId(userProfile.getPersonalDetails().getPrimaryEmail());
					user.setEmail(userProfile.getPersonalDetails().getPrimaryEmail());
					user.setFirstName(userProfile.getPersonalDetails().getFirstname());
					userNames.add(userProfile.getPersonalDetails().getPrimaryEmail());
					topPerformers.add(user);
					if (counter == count)
						break;
					counter++;
				}
			}
		}

		return topPerformers;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<CohortUsers> getActiveUsers(String xAuthUser, String rootOrgId, String rootOrg, String contentId, String userId,
			int count, Boolean toFilter) {
		// Check User exists
// 		if (!userUtilService.validateUser(rootOrg, userId)) {
// 			throw new BadRequestException("Invalid UserId.");
// 		}
		List<String> batchIdList = null;
		try {
			List<SunbirdApiBatchResp> batches = fetchBatchDetails(rootOrgId, contentId);
			if (!CollectionUtils.isEmpty(batches)) {
				batchIdList = batches.stream().map(SunbirdApiBatchResp::getBatchId).collect(Collectors.toList());
				//List<String> batchIdList = fetchBatchIdDetails(contentId);
				if (!CollectionUtils.isEmpty(batchIdList)) {
					return fetchParticipantsList(xAuthUser, rootOrg, batchIdList, count);
				}
			}
		} catch(Exception e) {
			logger.error("Failed to get Active users for Content: " + contentId + ", Exception: ", e);
		}
		return Collections.emptyList();
	}

	@Override
	public SBApiResponse autoEnrollmentInCourse(String authUserToken, String rootOrgId, String rootOrg, String contentId, String userUUID){
		SBApiResponse finalResponse = ProjectUtil.createDefaultResponse(Constants.API_USER_ENROLMENT);
		try {
			List<SunbirdApiBatchResp> batchResp = fetchBatchDetails(rootOrgId, contentId);
			List<String> batchIdList = null;
			if (!CollectionUtils.isEmpty(batchResp))
				batchIdList = batchResp.stream().map(SunbirdApiBatchResp::getBatchId).collect(Collectors.toList());
			Map<String, String> headers = new HashMap<>();
			headers.put("x-authenticated-user-token", authUserToken);
			headers.put("authorization", cbExtServerProperties.getSbApiKey());
			headers.put(Constants.X_AUTH_USER_ORG_ID, rootOrgId);
			
			if (CollectionUtils.isEmpty(batchIdList)) {
				finalResponse = createBatchAndEnroll(contentId, userUUID, headers);
			} else {
				List<SunbirdApiUserCourse> userCourseList = fetchUserEnrolledBatches(authUserToken, userUUID, rootOrgId);
				if (!CollectionUtils.isEmpty(userCourseList)) {
					List<String> userBatchIds = userCourseList.stream().map(SunbirdApiUserCourse::getBatchId)
							.collect(Collectors.toList());
					Map<String, SunbirdApiBatchResp> batchMap = batchResp.stream()
							.collect(Collectors.toMap(SunbirdApiBatchResp::getBatchId,
									sunbirdApiBatchResp -> sunbirdApiBatchResp, (oldValue, newValue) -> oldValue,
									HashMap::new));
					boolean isUserAlreadyEnrolled = false;
					for (String userBatchId : userBatchIds) {
						if (batchIdList.contains(userBatchId)) {
							finalResponse = constructAutoEnrollResponse(batchMap.get(userBatchId));
							isUserAlreadyEnrolled = true;
							break;
						}
					}
					if (!isUserAlreadyEnrolled) {
						boolean isUserEnrolled = false;
						for (SunbirdApiBatchResp batch : batchResp) {
							if (StringUtils.isEmpty(batch.getEndDate())) {
								Map<String,Object> enrollResponse;
								enrollResponse = enrollInCourse(contentId, userUUID, headers, batch.getBatchId());
								if (!ObjectUtils.isEmpty(enrollResponse) && Constants.OK.equalsIgnoreCase((String) enrollResponse.get(Constants.RESPONSE_CODE))) {
									finalResponse = constructAutoEnrollResponse(batch);
								} else {
									finalResponse.setResult(enrollResponse);
									finalResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
								}
								isUserEnrolled = true;
								break;
							}
						}
						if (!isUserEnrolled) {
							finalResponse = createBatchAndEnroll(contentId, userUUID, headers);
						}
					}
				} else {
					boolean isUserEnrolled = false;
					for (SunbirdApiBatchResp batch : batchResp) {
						if (StringUtils.isEmpty(batch.getEndDate())) {
							Map<String,Object> enrollResponse;
							enrollResponse = enrollInCourse(contentId, userUUID, headers, batch.getBatchId());
							if (!ObjectUtils.isEmpty(enrollResponse) && Constants.OK == enrollResponse.get(Constants.RESPONSE_CODE)) {
								finalResponse = constructAutoEnrollResponse(batch);
							}else {
								finalResponse.setResult(enrollResponse);
								finalResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
							}
							isUserEnrolled = true;
							break;
						}
					}
					if (!isUserEnrolled) {
						finalResponse = createBatchAndEnroll(contentId, userUUID, headers);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Failed to auto enrol user. Exception: ", e);
			finalResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			finalResponse.getParams().setErrmsg(e.getMessage());
		}
		return finalResponse;
	}

	private SBApiResponse createBatchAndEnroll(String contentId, String userUUID, Map<String, String> headers) {
		HashMap<String, Object> batchObj = new HashMap<>();
		HashMap<String, Object> req = new HashMap<>();
		SBApiResponse response = new SBApiResponse();
		Map<String, Object> enrollResponse = new HashMap<>();
		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		batchObj.put("courseId", contentId);
		batchObj.put("name", Constants.OPEN_BATCH);
		batchObj.put("description", Constants.OPEN_BATCH);
		batchObj.put("enrollmentType", "open");
		batchObj.put("startDate", date);
		batchObj.put("createdBy", userUUID);
		req.put("request", batchObj);
		Map<String, Object> batchCreationRes = outboundRequestHandlerService.fetchResultUsingPost(
				cbExtServerProperties.getCourseServiceHost() + cbExtServerProperties.getCourseBatchCreateEndpoint(),
				req, headers);
		Map<String, Object> batchCreationResult = (Map<String, Object>) batchCreationRes.get("result");
		String batchId = (String) batchCreationResult.get("batchId");
		if (!StringUtils.isEmpty(batchId)) {
			enrollResponse = enrollInCourse(contentId, userUUID, headers, batchId);
		}
		SunbirdApiBatchResp selectedBatch = new SunbirdApiBatchResp();
		selectedBatch.setBatchId(batchId);
		selectedBatch.setEndDate(null);
		selectedBatch.setCreatedFor(new ArrayList<>());
		selectedBatch.setEnrollmentEndDate(null);
		selectedBatch.setEnrollmentType("open");
		selectedBatch.setName(Constants.OPEN_BATCH);
		selectedBatch.setStartDate(date);
		selectedBatch.setStatus(1);
		selectedBatch.setBatchId(batchId);
		if (!ObjectUtils.isEmpty(enrollResponse) && Constants.OK == enrollResponse.get(Constants.RESPONSE_CODE)) {
			 response = constructAutoEnrollResponse(selectedBatch);
		}else {
			response.setResult(enrollResponse);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	private SBApiResponse constructAutoEnrollResponse(SunbirdApiBatchResp selectedBatch) {
		SBApiResponse response = new SBApiResponse();
		List<SunbirdApiBatchResp> content = new ArrayList<>();
		HashMap<String, Object> result = new HashMap<>();
		content.add(selectedBatch);
		result.put("content", content);
		result.put("count", 1);
		response.put("response", result);
		response.setResponseCode(HttpStatus.OK);
		return response;
	}

	private Map<String, Object> enrollInCourse(String contentId, String userUUID, Map<String, String> headers, String batchId) {
		HashMap<String, Object> req;
		req = new HashMap<>();
		HashMap<String, Object> enrollObj = new HashMap<>();
		enrollObj.put("userId", userUUID);
		enrollObj.put("courseId", contentId);
		enrollObj.put("batchId", batchId);
		req.put("request", enrollObj);
		return outboundRequestHandlerService.fetchResultUsingPost(
				cbExtServerProperties.getCourseServiceHost() + cbExtServerProperties.getUserCourseEnroll(), req,
				headers);
	}

	private void processChildContentId(String givenContentId, List<String> assessmentIdList) {
		try {
			SunbirdApiResp contentHierarchy = contentService.getHeirarchyResponse(givenContentId);
			if (contentHierarchy != null) {
				if ("successful".equalsIgnoreCase(contentHierarchy.getParams().getStatus())) {
					List<SunbirdApiHierarchyResultContent> children = contentHierarchy.getResult().getContent()
							.getChildren();
					if (!CollectionUtils.isEmpty(children)) {
						// We found the parent.
						for (SunbirdApiHierarchyResultContent content : children) {
							assessmentIdList.add(content.getIdentifier());
						}
					} else {
						// There are no children. Check if parent exist
						if (!StringUtils.isEmpty(contentHierarchy.getResult().getContent().getParent())) {
							processChildContentId(contentHierarchy.getResult().getContent().getParent(),
									assessmentIdList);
						} else {
							// There are no parent.
						}
					}
				} else {
					logger.warn("Failed to fetch Content Hierarchy for Id: " + givenContentId);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private List<String> fetchBatchIdDetails(String contentId) {
		try {
			SunbirdApiResp contentHierarchy = contentService.getHeirarchyResponse(contentId);
			if (contentHierarchy != null && "successful".equalsIgnoreCase(contentHierarchy.getParams().getStatus())) {
				List<SunbirdApiBatchResp> batches = contentHierarchy.getResult().getContent().getBatches();
				if (!CollectionUtils.isEmpty(batches)) {
					return batches.stream().map(SunbirdApiBatchResp::getBatchId).collect(Collectors.toList());
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}

		return Collections.emptyList();
	}

	private List<SunbirdApiBatchResp> fetchBatchDetails(String rootOrgId, String contentId) throws Exception {
		try {
			Map<String, Object> contentResponse = contentService.searchLiveContent(rootOrgId, contentId);
			if (!ObjectUtils.isEmpty(contentResponse)) {
				Map<String, Object> contentResult = (Map<String, Object>) contentResponse.get(Constants.RESULT);
				List<Map<String, Object>> contentList = (List<Map<String, Object>>) contentResult
						.get(Constants.CONTENT);
				if (!CollectionUtils.isEmpty(contentList)) {
					ObjectMapper ob = new ObjectMapper();
					CollectionType listType = ob.getTypeFactory().constructCollectionType(ArrayList.class,
							SunbirdApiBatchResp.class);
					return ob.readValue(ob.writeValueAsString(contentList.get(0).get(Constants.BATCHES)), listType);
				}
			}
		} catch (Exception e) {
			logger.error("Failed to get batch details. Exception: ", e);
			throw e;
		}
		throw new Exception(String.format("Content Search failed for Content: %s", contentId));
	}

	private List<SunbirdApiUserCourse> fetchUserEnrolledBatches(String authToken, String userId, String rootOrgId) {
		try {
			SunbirdApiUserCourseListResp userCourseListResponse = contentService.getUserCourseListResponse(authToken,
					userId, rootOrgId);
			if (userCourseListResponse != null) {
				return userCourseListResponse.getResult().getCourses();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return Collections.emptyList();
	}

	private List<CohortUsers> fetchParticipantsList(String xAuthUser, String rootOrg, List<String> batchIdList,
			int count) {
		List<String> participantList = contentService.getParticipantsList(xAuthUser, batchIdList);
		List<CohortUsers> activeUserCollection = new ArrayList<>();
		if (participantList.size() > count) {
			participantList = participantList.stream().limit(count).collect(Collectors.toList());
		} else if (participantList.isEmpty()) {
			return activeUserCollection;
		}
		try {
			Map<String, Object> participantMap = userUtilService.getUsersDataFromUserIds(rootOrg, participantList,
					new ArrayList<>(Arrays.asList(Constants.FIRST_NAME, Constants.EMAIL,
							Constants.DEPARTMENT_NAME)));
			if (!CollectionUtils.isEmpty(participantMap)) {
				logger.info("enrichDepartmentInfo UserIds -> " + participantList.toString()
						+ ", fetched Information -> " + participantMap.size());
				int currentCount = 0;
				String desc = "Started learning this course";
				for (String userId : participantList) {
					if (participantMap.containsKey(userId)) {
						SearchUserApiContent userInfo = (SearchUserApiContent) participantMap.get(userId);
						CohortUsers user = new CohortUsers();
						// User Id is assigning instead of email
						user.setUserId(userInfo.getUserId());
						user.setEmail(userInfo.getEmail());
						user.setFirstName(userInfo.getFirstName());
						user.setDesc(desc);
						user.setDepartment(userInfo.getChannel());
						if (userInfo.getProfileDetails() != null
								&& userInfo.getProfileDetails().getProfessionalDetails() != null) {
							SunbirdUserProfessionalDetail profDetail = userInfo.getProfileDetails()
									.getProfessionalDetails().get(0);
							String designation = profDetail.getDesignation() != null ? profDetail.getDesignation()
									: profDetail.getDesignationOther();
							user.setDepartment(designation);
						}
						activeUserCollection.add(user);
						currentCount++;
						if (currentCount == count) {
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return activeUserCollection;
	}

	@Override
	public SBApiResponse autoEnrollmentInCourseV2(String authUserToken, String rootOrgId, String rootOrg, String contentId, String userUUID) {
		SBApiResponse finalResponse = ProjectUtil.createDefaultResponse(Constants.API_USER_ENROLMENT);
		try {
			Map<String, Object> contentResponse = contentService.readContent(contentId);
			if (ObjectUtils.isEmpty(contentResponse)) {
				ProjectUtil.updateErrorDetails(finalResponse, String.format(Constants.CONTENT_NOT_AVAILABLE, contentId), HttpStatus.BAD_REQUEST);
				return finalResponse;
			}
			if (!cbExtServerProperties.getContentTypeAutoEnrollAccepted().contains(contentResponse.get(Constants.PRIMARY_CATEGORY))) {
				ProjectUtil.updateErrorDetails(finalResponse, String.format(Constants.AUTO_ENROLL_PRIMARY_CATEGORY_ERROR_MSG,
						contentResponse.get(Constants.PRIMARY_CATEGORY)), HttpStatus.BAD_REQUEST);
				return finalResponse;
			}
			List<Map<String, Object>> batches = (List<Map<String, Object>>) contentResponse.get(Constants.BATCHES);
			List<SunbirdApiBatchResp> batchDetails = new ArrayList<>();
			ObjectMapper mapper = new ObjectMapper();
			batchDetails.addAll(batches.stream().filter(batch -> (Integer) batch.get(Constants.STATUS) != 2).map(batchMap -> {
				try {
					return mapper.convertValue(batchMap, SunbirdApiBatchResp.class);
				} catch (IllegalArgumentException e) {
					return null;
				}
			}).filter(sunbirdClass -> sunbirdClass != null).collect(Collectors.toList()));
			if (CollectionUtils.isEmpty(batchDetails)) {
				ProjectUtil.updateErrorDetails(finalResponse, Constants.BATCH_NOT_AVAILABLE_ERROR_MSG, HttpStatus.BAD_REQUEST);
				return finalResponse;
			}
			Map<String, String> headers = new HashMap<>();
			headers.put(Constants.X_AUTH_TOKEN, authUserToken);
			headers.put(Constants.AUTHORIZATION, cbExtServerProperties.getSbApiKey());
			headers.put(Constants.X_AUTH_USER_ORG_ID, rootOrgId);
			List<String> batchIdList = batchDetails.stream().map(batchDetail -> batchDetail.getBatchId()).collect(Collectors.toList());
			List<Map<String, Object>> userActiveEnrollmentForBatch = getActiveEnrollmentForUser(batchIdList, userUUID);
			boolean isEnrolledWithBatch = false;
			if (!userActiveEnrollmentForBatch.isEmpty()) {
				ProjectUtil.updateErrorDetails(finalResponse, Constants.BATCH_ALREADY_ENROLLED_MSG, HttpStatus.BAD_REQUEST);
				return finalResponse;
			}
			//Enroll for the 1st batch for the course, Standalone Assessment
			SunbirdApiBatchResp batchDetail = batchDetails.get(0);
			Map<String, Object> enrollResponse = enrollInCourse(contentId, userUUID, headers, batchDetail.getBatchId());
			if (!ObjectUtils.isEmpty(enrollResponse) && Constants.OK.equals(enrollResponse.get(Constants.RESPONSE_CODE))) {
				finalResponse = constructAutoEnrollResponse(batchDetail);
				isEnrolledWithBatch = true;
			}
			if (!isEnrolledWithBatch) {
				ProjectUtil.updateErrorDetails(finalResponse, Constants.BATCH_AUTO_ENROLL_ERROR_MSG, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.error("Failed to auto enrol user. Exception: ", e);
			ProjectUtil.updateErrorDetails(finalResponse, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return finalResponse;
	}

	private List<Map<String, Object>> getActiveEnrollmentForUser(List<String> batchIds, String userId) {
		Map<String, Object> propertyMap = new HashMap<>();
		propertyMap.put(Constants.BATCH_ID, batchIds);
		propertyMap.put(Constants.USER_ID, userId);
		List<Map<String, Object>> activeEnrollmentForUser = cassandraOperation.getRecordsByPropertiesWithoutFiltering(Constants.KEYSPACE_SUNBIRD_COURSES,
				Constants.TABLE_ENROLLMENT_BATCH_LOOKUP, propertyMap, Arrays.asList(Constants.BATCH_ID, Constants.USER_ID, Constants.ACTIVE));
		return activeEnrollmentForUser.stream().filter(enrollmentForUser -> (boolean) enrollmentForUser.get(Constants.ACTIVE)).collect(Collectors.toList());
	}
}

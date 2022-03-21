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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.sunbird.assessment.repo.CohortUsers;
import org.sunbird.assessment.repo.UserAssessmentTopPerformerRepository;
import org.sunbird.common.model.*;
import org.sunbird.common.service.ContentService;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.user.service.UserUtilityService;

@Service
public class CohortsServiceImpl implements CohortsService {

	public static final String TS_CREATED = "ts_created";
	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	@Autowired
	UserUtilityService userUtilService;

	@Autowired
	ContentService contentService;

	@Autowired
	UserAssessmentTopPerformerRepository userAssessmentTopPerformerRepo;

	@Autowired
	OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	@Autowired
	CbExtServerProperties cbExtServerProperties;

	@Override
	public List<CohortUsers> getTopPerformers(String rootOrg, String contentId, String userId, int count) {
		// Check User exists
// 		if (!userUtilService.validateUser(rootOrg, userId)) {
// 			throw new BadRequestException("Invalid UserId.");
// 		}

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
					new ArrayList<>(Arrays.asList(Constants.FIRST_NAME, Constants.LAST_NAME, Constants.EMAIL,
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
					user.setUser_id(userProfile.getPersonalDetails().getPrimaryEmail());
					user.setEmail(userProfile.getPersonalDetails().getPrimaryEmail());
					user.setFirst_name(userProfile.getPersonalDetails().getFirstname());
					user.setLast_name(userProfile.getPersonalDetails().getSurname());
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
	public List<CohortUsers> getActiveUsers(String xAuthUser, String rootOrg, String contentId, String userId,
			int count, Boolean toFilter) throws Exception {
		// Check User exists
// 		if (!userUtilService.validateUser(rootOrg, userId)) {
// 			throw new BadRequestException("Invalid UserId.");
// 		}

		List<String> batchIdList = fetchBatchIdDetails(contentId);
		if (CollectionUtils.isEmpty(batchIdList)) {
			return Collections.emptyList();
		}
		return fetchParticipantsList(xAuthUser, rootOrg, batchIdList, count);
	}

	@Override
	public Response autoEnrollmentInCourse(String authUserToken, String rootOrg, String contentId, String userUUID)
			throws Exception {
		List<SunbirdApiBatchResp> batchResp = fetchBatchsDetails(contentId);
		List<String> batchIdList = null;
		if (!CollectionUtils.isEmpty(batchResp))
			batchIdList = batchResp.stream().map(SunbirdApiBatchResp::getBatchId).collect(Collectors.toList());
		Map<String, String> headers = new HashMap<>();
		headers.put("x-authenticated-user-token", authUserToken);
		headers.put("authorization", cbExtServerProperties.getSbApiKey());
		Response finalResponse = null;
		if (CollectionUtils.isEmpty(batchIdList)) {
			finalResponse = createBatchAndEnroll(contentId, userUUID, headers);
		} else {
			List<SunbirdApiUserCourse> userCourseList = fetchUserEnrolledBatches(authUserToken, userUUID);
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
							enrollInCourse(contentId, userUUID, headers, batch.getBatchId());
							finalResponse = constructAutoEnrollResponse(batch);
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
						enrollInCourse(contentId, userUUID, headers, batch.getBatchId());
						finalResponse = constructAutoEnrollResponse(batch);
						isUserEnrolled = true;
						break;
					}
				}
				if (!isUserEnrolled) {
					finalResponse = createBatchAndEnroll(contentId, userUUID, headers);
				}
			}
		}
		return finalResponse;
	}

	private Response createBatchAndEnroll(String contentId, String userUUID, Map<String, String> headers) {
		HashMap<String, Object> batchObj = new HashMap<>();
		HashMap<String, Object> req = new HashMap<>();
		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		batchObj.put("courseId", contentId);
		batchObj.put("name", "Open Batch");
		batchObj.put("description", "Open Batch");
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
			enrollInCourse(contentId, userUUID, headers, batchId);
		}
		SunbirdApiBatchResp selectedBatch = new SunbirdApiBatchResp();
		selectedBatch.setBatchId(batchId);
		selectedBatch.setEndDate(null);
		selectedBatch.setCreatedFor(new ArrayList<>());
		selectedBatch.setEnrollmentEndDate(null);
		selectedBatch.setEnrollmentType("open");
		selectedBatch.setName("Open Batch");
		selectedBatch.setStartDate(date);
		selectedBatch.setStatus(1);
		selectedBatch.setBatchId(batchId);
		Response response = constructAutoEnrollResponse(selectedBatch);
		return response;
	}

	private Response constructAutoEnrollResponse(SunbirdApiBatchResp selectedBatch) {
		Response response = new Response();
		List<SunbirdApiBatchResp> content = new ArrayList<>();
		HashMap<String, Object> result = new HashMap<>();
		content.add(selectedBatch);
		result.put("content", content);
		result.put("count", 1);
		response.put("response", result);
		return response;
	}

	private void enrollInCourse(String contentId, String userUUID, Map<String, String> headers, String batchId) {
		Response response = null;
		HashMap<String, Object> req;
		req = new HashMap<>();
		HashMap<String, Object> enrollObj = new HashMap<>();
		enrollObj.put("userId", userUUID);
		enrollObj.put("courseId", contentId);
		enrollObj.put("batchId", batchId);
		req.put("request", enrollObj);
		Map<String, Object> enrollMentResponse = outboundRequestHandlerService.fetchResultUsingPost(
				cbExtServerProperties.getCourseServiceHost() + cbExtServerProperties.getUserCourseEnroll(), req,
				headers);
		Map<String, Object> enrollmentresul = (Map<String, Object>) enrollMentResponse.get("result");
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

	private List<SunbirdApiBatchResp> fetchBatchsDetails(String contentId) {
		try {
			SunbirdApiResp contentHierarchy = contentService.getHeirarchyResponse(contentId);
			if (contentHierarchy != null && "successful".equalsIgnoreCase(contentHierarchy.getParams().getStatus())) {
				return contentHierarchy.getResult().getContent().getBatches();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return Collections.emptyList();
	}

	private List<SunbirdApiUserCourse> fetchUserEnrolledBatches(String authToken, String userId) {
		try {
			SunbirdApiUserCourseListResp userCourseListResponse = contentService.getUserCourseListResponse(authToken,
					userId);
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
		} else if (participantList.size() == 0) {
			return activeUserCollection;
		}
		try {
			Map<String, Object> participantMap = userUtilService.getUsersDataFromUserIds(rootOrg, participantList,
					new ArrayList<>(Arrays.asList(Constants.FIRST_NAME, Constants.LAST_NAME, Constants.EMAIL,
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
						user.setUser_id(userInfo.getUserId());
						user.setEmail(userInfo.getEmail());
						user.setFirst_name(userInfo.getFirstName());
						user.setLast_name(userInfo.getLastName());
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
}

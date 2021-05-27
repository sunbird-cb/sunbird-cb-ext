package org.sunbird.assessment.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import clojure.lang.Obj;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.sunbird.assessment.repo.CohortUsers;
import org.sunbird.assessment.repo.UserAssessmentTopPerformerRepository;
import org.sunbird.common.model.*;
import org.sunbird.common.service.ContentService;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.service.UserUtilityService;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.core.logger.CbExtLogger;

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
	public List<CohortUsers> getTopPerformers(String rootOrg, String contentId, String userId, int count){
		// Check User exists
		if (!userUtilService.validateUser(rootOrg, userId)) {
			throw new BadRequestException("Invalid UserId.");
		}

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
	public List<CohortUsers> getActiveUsers(String xAuthUser, String rootOrg, String contentId, String userId, int count,
			Boolean toFilter) throws Exception {
		// Check User exists
		if (!userUtilService.validateUser(rootOrg, userId)) {
			throw new BadRequestException("Invalid UserId.");
		}

		List<String> batchIdList = fetchBatchIdDetails(contentId);
		if (CollectionUtils.isEmpty(batchIdList)) {
			return Collections.emptyList();
		}
		return fetchParticipantsList(xAuthUser, rootOrg, batchIdList, count);
	}

	@Override
	public Response autoEnrollmentInCourse(String authUserToken, String rootOrg, String contentId, String userUUID) throws Exception {
		List<SunbirdApiBatchResp> batchResp = fetchBatchsDetails(contentId);
		List<String> batchIdList = null;
		if (!CollectionUtils.isEmpty(batchResp))
			batchIdList = batchResp.stream().map(SunbirdApiBatchResp::getBatchId).collect(Collectors.toList());
		Map<String, String> headers = new HashMap<>();
		headers.put("x-authenticated-user-token", authUserToken);
		headers.put("authorization", cbExtServerProperties.getSbApiKey());
		Response finalResponse = null;
		if (CollectionUtils.isEmpty(batchIdList)) {
			Map<String, Object> batchCreationRes = createBatchForCourse(contentId, userUUID, headers);
			Map<String, Object> batchCreationResult = (Map<String, Object>) batchCreationRes.get("result");
			String batchId = (String) batchCreationResult.get("batchId");
			if (!StringUtils.isEmpty(batchId)) {
				finalResponse = enrollInCourse(contentId, userUUID, headers, batchId);
			}
		} else {
			boolean isUserEnrolledInBatch = false;
			String enrolledBatchId = null;
			for (String batchId : batchIdList) {
				List<String> batchParticipants = contentService.getParticipantsForBatch(authUserToken, batchId);
				if (!CollectionUtils.isEmpty(batchParticipants) && batchParticipants.contains(userUUID)) {
					isUserEnrolledInBatch = true;
					enrolledBatchId = batchId;
					break;
				}
			}
			if (isUserEnrolledInBatch) {
				finalResponse = getAutoEnrollResponse(contentId, enrolledBatchId, batchResp);
				finalResponse.put(Constants.STATUS, HttpStatus.OK);
			} else {
				boolean isUserEnrolled = false;
				for (SunbirdApiBatchResp batch : batchResp) {
					if (StringUtils.isEmpty(batch.getEndDate())) {
						finalResponse = enrollInCourse(contentId, userUUID, headers, batch.getBatchId());
						isUserEnrolled = true;
						break;
					}
				}
				if (!isUserEnrolled) {
					Map<String, Object> batchCreationRes = createBatchForCourse(contentId, userUUID, headers);
					Map<String, Object> batchCreationResult = (Map<String, Object>) batchCreationRes.get("result");
					String batchId = (String) batchCreationResult.get("batchId");
					if (!StringUtils.isEmpty(batchId)) {
						finalResponse = enrollInCourse(contentId, userUUID, headers, batchId);
					}
				}
			}
		}
		return finalResponse;
	}

	private Map<String, Object> createBatchForCourse(String contentId, String userUUID, Map<String, String> headers) {
		HashMap<String, Object> batchObj = new HashMap<>();
		HashMap<String, Object> req = new HashMap<>();
		String date =new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		batchObj.put("courseId", contentId);
		batchObj.put("name", "Open Batch");
		batchObj.put("description", "Open Batch");
		batchObj.put("enrollmentType", "open");
		batchObj.put("startDate", date);
		batchObj.put("createdBy", userUUID);
		req.put("request", batchObj);
		Map<String, Object> batchCreationRes = outboundRequestHandlerService.fetchResultUsingPost(cbExtServerProperties.getCourseServiceHost() + cbExtServerProperties.getCourseBatchCreateEndpoint(), req, headers);
		return batchCreationRes;
	}

	private Response enrollInCourse(String contentId, String userUUID, Map<String, String> headers, String batchId) {
		Response response = null;
		HashMap<String, Object> req;
		req = new HashMap<>();
		HashMap<String, Object> enrollObj = new HashMap<>();
		enrollObj.put("userId", userUUID);
		enrollObj.put("courseId", contentId);
		enrollObj.put("batchId", batchId);
		req.put("request", enrollObj);
		Map<String, Object> enrollMentResponse = outboundRequestHandlerService.fetchResultUsingPost(cbExtServerProperties.getCourseServiceHost() + cbExtServerProperties.getUserCourseEnroll(), req, headers);
		Map<String, Object> enrollmentresul = (Map<String, Object>) enrollMentResponse.get("result");
		if(enrollmentresul.get("response").equals("SUCCESS")){
			response = getAutoEnrollResponse(contentId, batchId, null);
		}else{
			response.put(Constants.MESSAGE, "FAILED TO ENROLL IN COURSE!");
		}
		response.put(Constants.STATUS, HttpStatus.OK);
		return response;
	}

	private Response getAutoEnrollResponse(String contentId, String batchId, List<SunbirdApiBatchResp> batchResponse) {
		Response response = new Response();
		List<SunbirdApiBatchResp> batchResps = null;
		if(CollectionUtils.isEmpty(batchResponse))
			 batchResps = fetchBatchsDetails(contentId);
		SunbirdApiBatchResp selectedBatch = batchResps.stream().filter(batch -> batch.getBatchId().equals(batchId)).findAny().get();
		HashMap<String, Object> result = new HashMap<>();
		result.put("batchId", selectedBatch.getBatchId());
		result.put("endDate", selectedBatch.getEndDate());
		result.put("enrollmentEndDate", selectedBatch.getEnrollmentEndDate());
		result.put("enrollmentType", selectedBatch.getEnrollmentType());
		result.put("name", selectedBatch.getName());
		result.put("startDate", selectedBatch.getStartDate());
		result.put("status", selectedBatch.getStatus());
		response.put("result", result);
		return response;
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

	private List<CohortUsers> fetchParticipantsList(String xAuthUser, String rootOrg, List<String> batchIdList, int count) {
		List<String> participantList = contentService.getParticipantsList(xAuthUser, batchIdList);
		List<CohortUsers> activeUserCollection = new ArrayList<>();
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
						OpenSaberApiUserProfile userProfile = (OpenSaberApiUserProfile) participantMap.get(userId);
						CohortUsers user = new CohortUsers();
						user.setUser_id(userProfile.getPersonalDetails().getPrimaryEmail());
						user.setEmail(userProfile.getPersonalDetails().getPrimaryEmail());
						user.setFirst_name(userProfile.getPersonalDetails().getFirstname());
						user.setLast_name(userProfile.getPersonalDetails().getSurname());
						user.setDesc(desc);
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

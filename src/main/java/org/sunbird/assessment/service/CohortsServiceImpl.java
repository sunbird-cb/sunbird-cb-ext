package org.sunbird.assessment.service;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.sunbird.assessment.repo.CohortUsers;
import org.sunbird.assessment.repo.UserAssessmentTopPerformerRepository;
import org.sunbird.common.model.OpenSaberApiUserProfile;
import org.sunbird.common.model.SunbirdApiBatchResp;
import org.sunbird.common.model.SunbirdApiHierarchyResultContent;
import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.service.ContentService;
import org.sunbird.common.service.UserUtilityService;
import org.sunbird.common.util.Constants;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.core.logger.CbExtLogger;

@Service
public class CohortsServiceImpl implements CohortsService {

	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	@Autowired
	UserUtilityService userUtilService;

	@Autowired
	ContentService contentService;

	@Autowired
	UserAssessmentTopPerformerRepository userAssessmentTopPerformerRepo;

	@Override
	public List<CohortUsers> getTopPerformers(String rootOrg, String contentId, String userId, int count)
			throws Exception {
		// Check User exists
		if (!userUtilService.validateUser(rootOrg, userId)) {
			throw new BadRequestException("Invalid UserId.");
		}

		// This contains the list of all the children for provided course(resourceId) if
		// it is a learning-path.
		// Else, it will contain the parents for provided course(resourceId)
		List<String> assessmentIdList = new ArrayList<String>();
		assessmentIdList.add(contentId);
		processChildContentId(contentId, assessmentIdList);

		// fetch top learners
		List<Map<String, Object>> topLearnerRecords = new ArrayList<>();

		if (!assessmentIdList.isEmpty()) {
			topLearnerRecords = userAssessmentTopPerformerRepo
					.findByPrimaryKeyRootOrgAndPrimaryKeyParentSourceIdIn(rootOrg, assessmentIdList);
			Collections.sort(topLearnerRecords, new Comparator<Map<String, Object>>() {
				@Override
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					if (m1.get("ts_created") != null && m2.get("ts_created") != null) {
						return ((Date) m2.get("ts_created")).compareTo((Date) (m1.get("ts_created")));
					} else {
						return 1;
					}
				}
			});
		}

		int counter = 1;
		Set<String> topLearnerUUIDSet = new HashSet<String>();

		for (Map<String, Object> topLearnerRow : topLearnerRecords) {
			topLearnerUUIDSet.add(topLearnerRow.get("user_id").toString());
		}
		List<String> topLearnierIdList = new ArrayList<String>(topLearnerUUIDSet);
		Map<String, Object> learnerUUIDEmailMap = new HashMap<>();
		if (!topLearnerUUIDSet.isEmpty()) {
			learnerUUIDEmailMap = userUtilService.getUsersDataFromUserIds(rootOrg, topLearnierIdList,
					new ArrayList<>(Arrays.asList(Constants.FIRST_NAME, Constants.LAST_NAME, Constants.EMAIL,
							Constants.DEPARTMENT_NAME)));
			logger.info("enrichDepartmentInfo UserIds -> " + topLearnierIdList.toString() + ", fetched Information -> "
					+ learnerUUIDEmailMap.size());
		}

		List<String> userNames = new ArrayList<String>();
		List<CohortUsers> topPerformers = new ArrayList<CohortUsers>();
		for (Map<String, Object> topLearnerRow : topLearnerRecords) {
			// Same Logic as before
			String topLearnerUUID = topLearnerRow.get("user_id").toString();

			if (learnerUUIDEmailMap != null && learnerUUIDEmailMap.containsKey(topLearnerUUID)) {
				OpenSaberApiUserProfile userProfile = (OpenSaberApiUserProfile) learnerUUIDEmailMap.get(topLearnerUUID);
				if (!userNames.contains(userProfile.getPersonalDetails().getPrimaryEmail())
						&& !topLearnerUUID.toLowerCase().equals(userId)) {
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
			return Collections.EMPTY_LIST;
		}
		List<CohortUsers> activeUserCollection = fetchParticipantsList(xAuthUser, rootOrg, batchIdList, count);

		return activeUserCollection;
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
			if (contentHierarchy != null) {
				if ("successful".equalsIgnoreCase(contentHierarchy.getParams().getStatus())) {
					List<SunbirdApiBatchResp> batches = contentHierarchy.getResult().getContent().getBatches();
					if (!CollectionUtils.isEmpty(batches)) {
						return batches.stream().map(SunbirdApiBatchResp::getBatchId).collect(Collectors.toList());
					}
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}

		return Collections.emptyList();
	}

	private List<CohortUsers> fetchParticipantsList(String xAuthUser, String rootOrg, List<String> batchIdList, int count) {
		List<String> participantList = contentService.getParticipantsList(xAuthUser, batchIdList);

		try {
			if (!CollectionUtils.isEmpty(participantList)) {
				Map<String, Object> participantMap = userUtilService.getUsersDataFromUserIds(rootOrg, participantList,
						new ArrayList<>(Arrays.asList(Constants.FIRST_NAME, Constants.LAST_NAME, Constants.EMAIL,
								Constants.DEPARTMENT_NAME)));
				logger.info("enrichDepartmentInfo UserIds -> " + participantList.toString()
						+ ", fetched Information -> " + participantMap.size());

				if (participantMap != null) {
					List<CohortUsers> activeUserCollection = new ArrayList<CohortUsers>();
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
					return activeUserCollection;
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}

		return Collections.emptyList();
	}
}

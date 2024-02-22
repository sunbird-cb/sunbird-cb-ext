package org.sunbird.assessment.service;

import java.util.List;

import org.sunbird.assessment.repo.CohortUsers;
import org.sunbird.common.model.SBApiResponse;

public interface CohortsService {
	List<CohortUsers> getTopPerformers(String rootOrg, String resourceId, String userUUID, int count);

	List<CohortUsers> getActiveUsers(String xAuthUser, String rootOrgId, String rootOrg, String contentId, String userUUID, int count, Boolean toFilter);
	SBApiResponse autoEnrollmentInCourse(String authUserToken, String rootOrgId, String rootOrg, String contentId, String userUUID);

	SBApiResponse autoEnrollmentInCourseV2(String authUserToken, String rootOrgId, String rootOrg, String contentId, String userUUID);
}

package org.sunbird.assessment.service;

import java.util.List;

import org.sunbird.assessment.repo.CohortUsers;
import org.sunbird.common.model.Response;
import org.sunbird.common.model.SBApiResponse;

public interface CohortsService {
	List<CohortUsers> getTopPerformers(String rootOrg, String resourceId, String userUUID, int count) throws Exception;

	List<CohortUsers> getActiveUsers(String xAuthUser, String rootOrgId, String rootOrg, String contentId, String userUUID, int count, Boolean toFilter)
			throws Exception;
	SBApiResponse autoEnrollmentInCourse(String authUserToken, String rootOrgId, String rootOrg, String contentId, String userUUID)throws Exception;

	SBApiResponse autoEnrollmentInCourseV2(String authUserToken, String rootOrgId, String rootOrg, String contentId, String userUUID)throws Exception;
}

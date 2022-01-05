package org.sunbird.assessment.service;

import java.util.List;

import org.sunbird.assessment.repo.CohortUsers;
import org.sunbird.common.model.Response;

public interface CohortsService {
	List<CohortUsers> getTopPerformers(String rootOrg, String resourceId, String userUUID, int count);

	List<CohortUsers> getActiveUsers(String xAuthUser, String rootOrg, String contentId, String userUUID, int count,
			Boolean toFilter);

	Response autoEnrollmentInCourse(String authUserToken, String rootOrg, String contentId, String userUUID);
}

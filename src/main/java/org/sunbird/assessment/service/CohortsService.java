package org.sunbird.assessment.service;

import java.util.List;

import org.sunbird.assessment.repo.CohortUsers;

public interface CohortsService {
	List<CohortUsers> getTopPerformers(String rootOrg, String resourceId, String userUUID, int count) throws Exception;

	List<CohortUsers> getActiveUsers(String rootOrg, String contentId, String userUUID, int count, Boolean toFilter)
			throws Exception;
}

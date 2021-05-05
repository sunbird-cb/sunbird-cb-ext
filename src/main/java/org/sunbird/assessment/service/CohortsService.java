package org.sunbird.assessment.service;

import java.util.List;

import org.sunbird.assessment.repo.CohortUsers;

public interface CohortsService {
	List<CohortUsers> getTopPerformers(String rootOrg,String resourceId, String userUUID, int count) throws Exception;

}

package org.sunbird.halloffame.service;

import org.sunbird.common.model.SBApiResponse;

import java.util.Map;

/**
 * @author mahesh.vakkund
 */
public interface HallOfFameService {
    public Map<String, Object> fetchHallOfFameData();

    public SBApiResponse learnerLeaderBoard(String rootOrgId, String authToken);

    public SBApiResponse fetchingTop10Learners(String ministryOrgId, String authToken);
}

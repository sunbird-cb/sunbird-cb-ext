package org.sunbird.searchby.service;

import org.sunbird.common.model.FracApiResponse;
import org.sunbird.common.model.SBApiResponse;

import java.util.Map;

public interface MasterDataService {
	FracApiResponse getListPositions();

	Map<String, Object> getMasterDataByType(String type);

	SBApiResponse upsertMasterData(Map<String, Object> request);

	Map<String, Object> getProfilePageMetaData();

	SBApiResponse getDeptPositions(String userOrgId);

	SBApiResponse retrieveDeptPositionByAdmin(Map<String, Object> request);
}

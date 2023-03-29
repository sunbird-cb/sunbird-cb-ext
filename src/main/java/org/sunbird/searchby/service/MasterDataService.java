package org.sunbird.searchby.service;

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.searchby.model.PositionListResponse;

import java.util.Map;

public interface MasterDataService {
     PositionListResponse getListPositions(String userToken);
     Map<String,Object> getMasterDataByType(String type);
     SBApiResponse upsertMasterData(Map<String,Object> request);
     Map<String,Object> getProfilePageMetaData();
}

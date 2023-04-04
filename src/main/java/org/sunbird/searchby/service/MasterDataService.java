package org.sunbird.searchby.service;

import org.sunbird.common.model.FracApiResponse;
import org.sunbird.common.model.SBApiResponse;

import java.util.Map;

public interface MasterDataService {
     FracApiResponse getListPositions(String userToken);
     Map<String,Object> getMasterDataByType(String type);
     SBApiResponse upsertMasterData(Map<String,Object> request);
     Map<String,Object> getProfilePageMetaData();
}

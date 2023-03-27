package org.sunbird.searchby.service;

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.searchby.model.FracApiResponseV2;

import java.util.Map;

public interface MasterDataService {
     FracApiResponseV2 getListPositions(String userToken);
     SBApiResponse getMasterDataByType(String type);
     SBApiResponse create(Map<String,Object> request);
     SBApiResponse update(Map<String,Object> request, String id, String type);
}

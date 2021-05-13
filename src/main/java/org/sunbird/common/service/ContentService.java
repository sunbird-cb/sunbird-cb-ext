package org.sunbird.common.service;

import java.util.List;

import org.sunbird.common.model.SunbirdApiResp;

public interface ContentService {

	public SunbirdApiResp getHeirarchyResponse(String contentId);

	public List<String> getParticipantsList(String xAuthUser, List<String> batchIdList);
}

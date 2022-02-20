package org.sunbird.common.service;

import java.util.List;

import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.model.SunbirdApiUserCourseListResp;

public interface ContentService {

	public SunbirdApiResp getHeirarchyResponse(String contentId);

	public List<String> getParticipantsForBatch(String xAuthUser, String batchId);

	public List<String> getParticipantsList(String xAuthUser, List<String> batchIdList);

	public SunbirdApiUserCourseListResp getUserCourseListResponse(String authToken, String userId);
}


package org.sunbird.common.service;

import java.util.List;
import java.util.Map;

import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.model.SunbirdApiUserCourseListResp;

public interface ContentService {

	public SunbirdApiResp getHeirarchyResponse(String contentId);

	public SunbirdApiResp getAssessmentHierachyResponse(String assessmentId);

	public List<String> getParticipantsList(String xAuthUser, List<String> batchIdList);

	public List<String> getParticipantsForBatch(String xAuthUser, String batchId);

	public SunbirdApiUserCourseListResp getUserCourseListResponse(String authToken, String userId, String rootOrgId);

	public SunbirdApiResp getQuestionListDetails(List<String> questionIdList);

	Map<String, Object> searchLiveContentByContentIds(List<String> contentIds);

	public Map<String, Object> searchLiveContent(String rootOrgId, String contentId, String userChannel);

	public Map<String, Object> searchLiveContent(String rootOrgId, String contentId);

	public Map<String, Object> searchLiveContent(String contentId);

	public Map<String, Object> getHierarchyResponseMap(String contentId);

	public String getParentIdentifier(String resourceId);

	public String getContentType(String resourceId);

	public void getLiveContentDetails(List<String> contentIdList, List<String> fields,
			Map<String, Map<String, String>> contentInfoMap);

	public Map<String, Object> readContent(String contentId);

	public Map<String, Object> readContent(String contentId, List<String> fields);

	public List<Map<String, Object>> searchContent(String tag);
}

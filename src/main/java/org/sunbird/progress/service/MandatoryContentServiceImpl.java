package org.sunbird.progress.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.progress.model.MandatoryContentInfo;
import org.sunbird.progress.model.MandatoryContentResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MandatoryContentServiceImpl implements MandatoryContentService {

	@Autowired
	private OutboundRequestHandlerServiceImpl outboundReqService;

	@Autowired
	private CbExtServerProperties cbExtServerProperties;

	@Autowired
	private CassandraOperation cassandraOperation;

	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	ObjectMapper mapper = new ObjectMapper();

	public void enrichProgressDetails(String authUserToken, MandatoryContentResponse mandatoryContentInfo,
			String userId) {
		HashMap<String, Object> req;
		HashMap<String, Object> reqObj;
		List<String> fields = Arrays.asList("progressdetails");
		HashMap<String, String> headersValues = new HashMap<>();
		headersValues.put("X-Authenticated-User-Token", authUserToken);
		headersValues.put("Authorization", cbExtServerProperties.getSbApiKey());
		for (Map.Entry<String, MandatoryContentInfo> infoMap : mandatoryContentInfo.getContentDetails().entrySet()) {
			try {
				req = new HashMap<>();
				reqObj = new HashMap<>();
				reqObj.put("userId", userId);
				reqObj.put("courseId", infoMap.getKey());
				reqObj.put("batchId", infoMap.getValue().getBatchId());
				reqObj.put("fields", fields);
				req.put("request", reqObj);
				Map<String, Object> response = outboundReqService.fetchResultUsingPost(
						cbExtServerProperties.getCourseServiceHost() + cbExtServerProperties.getProgressReadEndPoint(),
						req, headersValues);
				if ("OK".equals(response.get("responseCode"))) {
					List<Object> result = (List<Object>) ((HashMap<String, Object>) response.get("result"))
							.get("contentList");
					if (!CollectionUtils.isEmpty(result)) {
						Optional<Object> optionResult = result.stream().findFirst();
						if (optionResult.isPresent()) {
							Map<String, Object> content = (Map<String, Object>) optionResult.get();
							BigDecimal progress = new BigDecimal(content.get("completionPercentage").toString());
							mandatoryContentInfo.getContentDetails().get(infoMap.getKey())
									.setUserProgress(progress.floatValue());
						}
					}
				}
			} catch (Exception ex) {
				logger.error(ex);
			}
		}
	}

	@Override
	public MandatoryContentResponse getMandatoryContentStatusForUser(String authUserToken, String rootOrg, String org,
			String userId) {
		MandatoryContentResponse response = new MandatoryContentResponse();

		Map<String, Object> propertyMap = new HashMap<>();
		propertyMap.put(Constants.ROOT_ORG, rootOrg);
		propertyMap.put(Constants.ORG, org);
		List<Map<String, Object>> contentList = cassandraOperation.getRecordsByProperties(Constants.DATABASE,
				Constants.MANDATORY_CONTENT, propertyMap, new ArrayList<>());

		if (CollectionUtils.isEmpty(contentList)) {
			logger.info("getMandatoryContentStatusForUser: There are no mandatory Content set in DB.");
			return response;
		}

		for (Map<String, Object> content : contentList) {
			String contentId = (String) content.get(Constants.CONTENT_ID);
			content.remove(Constants.CONTENT_ID);
			MandatoryContentInfo info = mapper.convertValue(content, MandatoryContentInfo.class);
			response.addContentInfo(contentId, info);
		}

		try {
			logger.info("getMandatoryContentStatusForUser: MandatoryCourse Details : "
					+ new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(response));
		} catch (JsonProcessingException e) {
			logger.error(e);
		}
		enrichProgressDetails(authUserToken, response, userId);
		try {
			logger.info("getMandatoryContentStatusForUser: Ret Value is: "
					+ new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(response));
		} catch (JsonProcessingException e) {
			logger.error(e);
		}
		Iterator<MandatoryContentInfo> entries = response.getContentDetails().values().iterator();
		boolean isCompleted = false;
		while (entries.hasNext()) {
			MandatoryContentInfo entry = entries.next();
			if (entry.getUserProgress() < entry.getMinProgressForCompletion()) {
				response.setMandatoryCourseCompleted(false);
				isCompleted = false;
				break;
			} else {
				isCompleted = true;
			}
		}
		if (isCompleted) {
			response.setMandatoryCourseCompleted(true);
		}
		return response;
	}
}

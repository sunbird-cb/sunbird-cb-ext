package org.sunbird.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.util.CbExtServerProperties;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	@Autowired
	CbExtServerProperties serverConfig;

	@Autowired
	private ObjectMapper mapper;

	public SunbirdApiResp getHeirarchyResponse(String contentId) {
		StringBuilder url = new StringBuilder();
		url.append(serverConfig.getContentHost()).append(serverConfig.getHierarchyEndPoint()).append("/" + contentId)
				.append("?hierarchyType=detail");
		SunbirdApiResp response = mapper.convertValue(outboundRequestHandlerService.fetchResult(url.toString()),
				SunbirdApiResp.class);
		if (response.getResponseCode().equalsIgnoreCase("Ok")) {
			return response;
		}

		return null;
	}
}

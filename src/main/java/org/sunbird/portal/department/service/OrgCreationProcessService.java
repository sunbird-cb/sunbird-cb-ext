package org.sunbird.portal.department.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.core.logger.CbExtLogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OrgCreationProcessService {

	private CbExtLogger log = new CbExtLogger(getClass().getName());

	@Autowired
	private CbExtServerProperties extServerProperties;

	@Autowired
	RestTemplate restTemplate;

	public void createOrg(Map<String, Object> orgObj) {
		log.info("Creating org to sb started ....");
		ObjectMapper mapper = new ObjectMapper();
		String orgName = (String) orgObj.get("orgName");
		Map<String, Object> request = new HashMap<>();
		HashMap<String, Object> innerReq = new HashMap<>();
		innerReq.put("channel", orgName);
		innerReq.put("orgName", orgName);
		innerReq.put("isRootOrg", true);
		request.put("request", innerReq);
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Authenticated-User-Token", (String) orgObj.get("userToken"));
		headers.set("Authorization", extServerProperties.getSbApiKey());
		HttpEntity<Object> entity = new HttpEntity<>(request, headers);
		try {
			log.info(mapper.writeValueAsString(request));
		} catch (JsonProcessingException e) {
			log.error(e);
		}
		Map<String, Object> response = restTemplate.postForObject(
				extServerProperties.getSbUrl() + extServerProperties.getOrgCreateEndPoint(), entity, Map.class);
		if (!CollectionUtils.isEmpty(response) && !ObjectUtils.isEmpty(response.get("result"))) {
			String orgId = (String) ((Map<String, Object>) response.get("result")).get("organisationId");
			log.info("Created org Id : " + orgId);
			log.info("Creating org to sb finished ....");
		} else {
			log.info("Some exception occurred while creating the org ....");
		}

	}
}

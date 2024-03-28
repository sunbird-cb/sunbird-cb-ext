package org.sunbird.catalog.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.catalog.model.Catalog;
import org.sunbird.catalog.model.Category;
import org.sunbird.catalog.model.Framework;
import org.sunbird.catalog.model.FrameworkResponse;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CatalogServiceImpl {

	private Logger log = LoggerFactory.getLogger(CatalogServiceImpl.class);

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private CbExtServerProperties extServerProperties;

	@Autowired
	private OutboundRequestHandlerServiceImpl outboundRequestHandlerServiceImpl;

	public Catalog getCatalog(String authUserToken, boolean isEnrichConsumption) {
		return fetchCatalog(authUserToken, isEnrichConsumption);
	}

	private Catalog fetchCatalog(String authUserToken, boolean isEnrichConsumption) {
		log.info("Fetching Framework details...");
		ObjectMapper mapper = new ObjectMapper();
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Authenticated-User-Token", authUserToken);
		headers.set("Authorization", extServerProperties.getSbApiKey());
		HttpEntity<Object> entity = new HttpEntity<>(headers);

		ResponseEntity<String> responseStr = restTemplate.exchange(extServerProperties.getKmBaseHost()
				+ extServerProperties.getKmFrameWorkPath() + extServerProperties.getTaxonomyFrameWorkName(),
				HttpMethod.GET, entity, String.class);
		FrameworkResponse response;
		try {
			response = mapper.readValue(responseStr.getBody(), FrameworkResponse.class);
			if (response != null && "successful".equalsIgnoreCase(response.getParams().getStatus())) {
				return processResponse(response.getResult().getFramework(), isEnrichConsumption);
			} else {
				log.info("Some exception occurred while creating the org ....");
			}
		} catch (Exception e) {
			log.error("Failed to read response data. Exception: ", e);
		}
		return new Catalog();
	}

	private Catalog processResponse(Framework framework, boolean isEnrichConsumption) {
		Catalog catalog = new Catalog();
		for (Category c : framework.getCategories()) {
			if (c.getName() != null && c.getName().equalsIgnoreCase(extServerProperties.getTaxonomyCategoryName())) {
				catalog.setTerms(c.getTerms());
			}
		}

		// TODO - Enrich Consumption details for the given term name.
		return catalog;
	}

	public SBApiResponse getSectors() {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_SECTOR_LIST);
		try {
			Map<String, Object> requestBody = new HashMap<String, Object>();
			Map<String, Object> request = new HashMap<String, Object>();
			Map<String, Object> search = new HashMap<String, Object>();
			search.put(Constants.STATUS, Constants.LIVE);
			request.put(Constants.SEARCH, search);
			requestBody.put(Constants.REQUEST, request);

			StringBuilder strUrl = new StringBuilder(extServerProperties.getKmBaseHost());
			strUrl.append(extServerProperties.getKmFrameworkTermSearchPath()).append("?framework=")
					.append(extServerProperties.getTaxonomyFrameWorkName()).append("&category=")
					.append(extServerProperties.getSectorCategoryName());

			Map<String, Object> termResponse = outboundRequestHandlerServiceImpl.fetchResultUsingPost(strUrl.toString(),
					requestBody, null);
			List<Map<String, Object>> sectors = new ArrayList();
			if (termResponse != null
					&& "OK".equalsIgnoreCase((String) termResponse.get(Constants.RESPONSE_CODE))) {
				Map<String, Object> result = (Map<String, Object>) termResponse.get(Constants.RESULT);
				List<Map<String, Object>> terms = (List<Map<String, Object>>) result.get(Constants.TERMS);
				if (CollectionUtils.isNotEmpty(terms)) {
					for (Map<String, Object> sector : terms) {
						processSector(sector, sectors);
					}
				}
			}
			response.getResult().put(Constants.COUNT, sectors.size());
			response.getResult().put(Constants.SECTORS, sectors);
		} catch (Exception e) {
			String errMsg = "Failed to read sector details. Exception: " + e.getMessage();
			log.error(errMsg, e);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			response.getParams().setErrmsg(errMsg);
			response.getParams().setStatus(Constants.FAILED);
		}
		return response;
	}

	private void processSector(Map<String, Object> sector, List<Map<String, Object>> sectors) {
		Map<String, Object> newSector = new HashMap<String, Object>();
		for (String field : extServerProperties.getSectorFields()) {
			if (sector.containsKey(field)) {
				newSector.put(field, sector.get(field));
			}
		}
		if (sector.containsKey(Constants.CHILDREN)) {
			newSector.put(Constants.CHILDREN, new ArrayList());
			processSubSector(sector, newSector);
		}
		sectors.add(newSector);
	}

	private void processSubSector(Map<String, Object> sector, Map<String, Object> newSector) {
		List<Map<String, Object>> subSectorList = (List<Map<String, Object>>) sector.get(Constants.CHILDREN);
		for (Map<String, Object> subSector : subSectorList) {
			Map<String, Object> newSubSector = new HashMap<String, Object>();
			for (String field : extServerProperties.getSubSectorFields()) {
				if (subSector.containsKey(field)) {
					newSubSector.put(field, sector.get(field));
				}
			}
			((List) newSector.get(Constants.CHILDREN)).add(newSubSector);
		}
	}
}

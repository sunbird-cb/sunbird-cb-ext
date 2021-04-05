package org.sunbird.catalog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.catalog.model.Catalog;
import org.sunbird.catalog.model.Category;
import org.sunbird.catalog.model.Framework;
import org.sunbird.catalog.model.FrameworkResponse;
import org.sunbird.common.util.CbExtServerProperties;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CatalogServiceImpl {

	private Logger log = LoggerFactory.getLogger(CatalogServiceImpl.class);

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private CbExtServerProperties extServerProperties;

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
}

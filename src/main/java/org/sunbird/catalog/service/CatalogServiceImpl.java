package org.sunbird.catalog.service;

import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.bcel.Const;
import org.jclouds.packet.domain.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
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
					.append(extServerProperties.getSectorFrameworkName()).append("&category=")
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
		Set<String> uniqueSubSector = new HashSet<String>();
		for (Map<String, Object> subSector : subSectorList) {
			if (uniqueSubSector.contains((String) subSector.get(Constants.IDENTIFIER))) {
				continue;
			} else {
				uniqueSubSector.add((String) subSector.get(Constants.IDENTIFIER));
			}
			Map<String, Object> newSubSector = new HashMap<String, Object>();
			for (String field : extServerProperties.getSubSectorFields()) {
				if (subSector.containsKey(field)) {
					newSubSector.put(field, subSector.get(field));
				}
			}
			((List) newSector.get(Constants.CHILDREN)).add(newSubSector);
		}
	}

	public SBApiResponse readSector(String sectorId) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_SECTOR_READ);
		try {
			StringBuilder strUrl = new StringBuilder(extServerProperties.getKmBaseHost());
			strUrl.append(extServerProperties.getKmFrameworkTermReadPath()).append("/").append(sectorId).append("?framework=")
					.append(extServerProperties.getSectorFrameworkName()).append("&category=")
					.append(extServerProperties.getSectorCategoryName());

			Map<String, Object> sectorMap = new HashMap<String, Object>();
			Map<String, Object> sectorResponse = (Map<String, Object>) outboundRequestHandlerServiceImpl.fetchResult(strUrl.toString());
			if (null != sectorResponse) {
				if (Constants.OK.equalsIgnoreCase((String) sectorResponse.get(Constants.RESPONSE_CODE))) {
					Map<String, Object> resultMap = (Map<String, Object>) sectorResponse.get(Constants.RESULT);
					Map<String, Object> sectorInput = (Map<String, Object>) resultMap.get(Constants.TERM);
					processSector(sectorInput, sectorMap);
					response.getResult().put(Constants.SECTOR, sectorMap);
				} else {
					response.setResponseCode(HttpStatus.NOT_FOUND);
					response.getParams().setErrmsg("Data not found with id : " + sectorId);
				}
			} else {
				response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
				response.getParams().setErrmsg("Failed to read the sector details for Id : " + sectorId);
			}
		} catch (Exception e) {
			log.error("Failed to read sector with Id: " + sectorId, e);
			response.getParams().setErrmsg("Failed to read sector: " + e.getMessage());
			response.getParams().setStatus(Constants.FAILED);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	public SBApiResponse createSector(Map<String, Object> request) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_SECTOR_CREATE);
		String errMsg = validateSectorCreateReq(request);
		if (StringUtils.isNotBlank(errMsg)) {
			response.getParams().setErrmsg(errMsg);
			response.getParams().setStatus(Constants.FAILED);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			return response;
		}

		// Check sector exists
		Map<String, Object> reqBody = (Map<String, Object>) request.get(Constants.REQUEST);
		String name = (String) reqBody.get(Constants.NAME);
		SBApiResponse readResponse = readSector(getCodeValue(name));
		if (readResponse == null) {
			// We have failed to read the sector details before creating... through error
			response.getParams().setErrmsg("Failed to validate sector exists or not.");
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			response.getParams().setStatus(Constants.FAILED);
		} else if (HttpStatus.NOT_FOUND.equals(readResponse.getResponseCode())) {
			// Resource is not found... we can create sector.
			reqBody.put(Constants.CODE, name);
			Map<String, Object> parentObj = new HashMap<>();
			parentObj.put(Constants.IDENTIFIER,
					extServerProperties.getSectorFrameworkName() + "_" + extServerProperties.getSectorCategoryName());
			reqBody.put(Constants.PARENTS, Arrays.asList(parentObj));
			Map<String, Object> termReq = new HashMap<String, Object>();
			termReq.put(Constants.TERM, reqBody);
			Map<String, Object> createReq = new HashMap<String, Object>();
			createReq.put(Constants.REQUEST, termReq);

			StringBuilder strUrl = new StringBuilder(extServerProperties.getKmBaseHost());
			strUrl.append(extServerProperties.getKmFrameworkTermCreatePath()).append("?framework=")
					.append(extServerProperties.getSectorFrameworkName()).append("&category=")
					.append(extServerProperties.getSectorCategoryName());
			Map<String, Object> termResponse = outboundRequestHandlerServiceImpl.fetchResultUsingPost(strUrl.toString(),
					createReq, null);
			List<Map<String, Object>> sectors = new ArrayList<>();
			if (termResponse != null
					&& Constants.OK.equalsIgnoreCase((String) termResponse.get(Constants.RESPONSE_CODE))) {
				log.info("Created sector successfully with name: " + name);
			} else {
				log.error("Failed to create the sector object with name: " + name);
				response.getParams().setErrmsg("Failed to create sector.");
				response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
				response.getParams().setStatus(Constants.FAILED);
			}
		} else if (HttpStatus.OK.equals(readResponse.getResponseCode())) {
			errMsg = "Sector already exists with name: " + name;
			log.error(errMsg);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			response.getParams().setStatus(Constants.FAILED);
		} else {
			log.error("Failed to validate sector exists while creating with name: " + name);
			response.getParams().setErrmsg("Failed to create sector.");
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			response.getParams().setStatus(Constants.FAILED);
		}
		return response;
	}

	public SBApiResponse createSubSector(Map<String, Object> request) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_SUB_SECTOR_CREATE);

		String errMsg = validateSubSectorCreateReq(request);
		if (StringUtils.isNotBlank(errMsg)) {
			response.getParams().setErrmsg(errMsg);
			response.getParams().setStatus(Constants.FAILED);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			return response;
		}

		Map<String, Object> reqBody = (Map<String, Object>) request.get(Constants.REQUEST);
		String id = (String) reqBody.get(Constants.IDENTIFIER);
		id = splitCodeValue(id);
		SBApiResponse readResponse = readSector(id);
		if (readResponse == null) {
			//We have failed to read the sector details before creating... through error
			response.getParams().setErrmsg("Failed to validate sector exists or not.");
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			response.getParams().setStatus(Constants.FAILED);
		} else if (HttpStatus.NOT_FOUND.equals(readResponse.getResponseCode())) {
			//We have failed to read the sector details before creating... through error
			response.getParams().setErrmsg("Failed to find existing sector for Id : " + id);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			response.getParams().setStatus(Constants.FAILED);
		} else if (HttpStatus.OK.equals(readResponse.getResponseCode())) {
			List<Map<String, Object>> subSectorList = (List<Map<String, Object>>) reqBody.get(Constants.SUB_SECTORS);
			List<String> requiredSubSector = new ArrayList<String>();
			Map<String, Object> readTermResponse = (Map<String, Object>) readResponse.getResult();
			Map<String, Object> existingTerm = (Map<String, Object>) readTermResponse.get(Constants.SECTOR);
			List<Map<String, Object>> existingChildren = (List<Map<String, Object>>) existingTerm
					.get(Constants.CHILDREN);

			for (Map<String, Object> subSector : subSectorList) {
				String name = (String) subSector.get(Constants.NAME);
				if (subSector.containsKey(Constants.IDENTIFIER)) {
					continue;
				} else {
					if (CollectionUtils.isEmpty(existingChildren)) {
						requiredSubSector.add(name);
					} else {
						for (Map<String, Object> existingSubSector : existingChildren) {
							if (name.equalsIgnoreCase((String) existingSubSector.get(Constants.NAME))) {
								errMsg = "Failed to create SubSector. Name '" + name + "' already exists in Sector.";
								break;
							} else {
								requiredSubSector.add(name);
							}
						}
					}
				}
				if (StringUtils.isNotBlank(errMsg)) {
					break;
				}
			}
			if (StringUtils.isNotBlank(errMsg)) {
				response.getParams().setErrmsg(errMsg);
				response.setResponseCode(HttpStatus.BAD_REQUEST);
				response.getParams().setStatus(Constants.FAILED);
				return response;
			}
			for (String name : requiredSubSector) {
				Map<String, Object> requestBody = new HashMap<>();
				Map<String, Object> termReq = new HashMap<>();
				termReq.put(Constants.CODE, name);
				termReq.put(Constants.NAME, name);

				Map<String, Object> parentObj = new HashMap<>();
				parentObj.put(Constants.IDENTIFIER, extServerProperties.getSectorFrameworkName() + "_"
						+ extServerProperties.getSectorCategoryName() + "_" + id);
				termReq.put(Constants.PARENTS, Arrays.asList(parentObj));
				requestBody.put(Constants.TERM, termReq);
				Map<String, Object> createReq = new HashMap<String, Object>();
				createReq.put(Constants.REQUEST, requestBody);

				StringBuilder strUrl = new StringBuilder(extServerProperties.getKmBaseHost());
				strUrl.append(extServerProperties.getKmFrameworkTermCreatePath()).append("?framework=")
						.append(extServerProperties.getSectorFrameworkName()).append("&category=")
						.append(extServerProperties.getSectorCategoryName());
				Map<String, Object> termResponse = outboundRequestHandlerServiceImpl.fetchResultUsingPost(
						strUrl.toString(),
						createReq, null);
				List<Map<String, Object>> sectors = new ArrayList<>();
				if (termResponse != null
						&& Constants.OK.equalsIgnoreCase((String) termResponse.get(Constants.RESPONSE_CODE))) {
					log.info("Created sub sector successfully with name : " + name);
				} else {
					log.error("Failed to create the sector object with name: " + name);
					response.getParams().setErrmsg("Failed to create sector.");
					response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
					response.getParams().setStatus(Constants.FAILED);
					break;
				}
			}
		} else {
			//We have failed to read the sector details before creating... through error
			response.getParams().setErrmsg("Failed to validate sector exists or not for Id : " + id);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			response.getParams().setStatus(Constants.FAILED);
		}
		return response;
	}

	private void processSector(Map<String, Object> sectorInput, Map<String, Object> sectorMap) {
		for (String field : extServerProperties.getSectorFields()) {
			if (sectorInput.containsKey(field)) {
				sectorMap.put(field, sectorInput.get(field));
			}
		}
		if (sectorInput.containsKey(Constants.CHILDREN)) {
			sectorMap.put(Constants.CHILDREN, new ArrayList<Map<String, Object>>());
			processSubSector(sectorInput, sectorMap);
		}
	}

	private String validateSectorCreateReq(Map<String, Object> sectorRequest) {
		String errMsg = "";
		Map<String, Object> sector = (Map<String, Object>) sectorRequest.get(Constants.REQUEST);
		if (MapUtils.isEmpty(sector)) {
			errMsg = "Invalid Request Object";
			return errMsg;
		}
		List<String> missingParams = new ArrayList<>();
		String name = (String) sector.get(Constants.NAME);
		if (StringUtils.isBlank(name)) {
			missingParams.add(Constants.NAME);
		}
		String imgUrl = (String) sector.get(Constants.IMG_URL_KEY);
		if (StringUtils.isBlank(imgUrl)) {
			missingParams.add(Constants.IMG_URL_KEY);
		}
		if (CollectionUtils.isNotEmpty(missingParams)) {
			errMsg = "Request is missing one or more parameters. Missing params : " + missingParams.toString();
		}
		return errMsg;
	}

	private String getCodeValue(String name) {
		if (StringUtils.isNotBlank(name)) {
			name = name.replace(" ", "-");
		}
		return name;
	}

	private String splitCodeValue(String name) {
		String[] strArray = name.split("_");
		return strArray[strArray.length - 1];
	}

	private String validateSubSectorCreateReq(Map<String, Object> sectorRequest) {
		String errMsg = "";
		Map<String, Object> sector = (Map<String, Object>) sectorRequest.get(Constants.REQUEST);
		if (MapUtils.isEmpty(sector)) {
			errMsg = "Invalid Request Object";
			return errMsg;
		}
		List<String> missingParams = new ArrayList<>();
		String identifier = (String) sector.get(Constants.IDENTIFIER);
		if (StringUtils.isBlank(identifier)) {
			missingParams.add(Constants.IDENTIFIER);
		}
		Object subSectors = sector.get(Constants.SUB_SECTORS);
		if (ObjectUtils.isEmpty(subSectors)) {
			missingParams.add(Constants.SUB_SECTORS);
		} else {
			List<Map<String, Object>> subSectorMap = (List<Map<String, Object>>) subSectors;
			for (Map<String, Object> subSector : subSectorMap) {
				String name = (String) subSector.get(Constants.NAME);
				if (StringUtils.isBlank(name)) {
					missingParams.add(Constants.NAME);
					break;
				}
			}
		}
		if (CollectionUtils.isNotEmpty(missingParams)) {
			errMsg = "Request is missing one or more parameters. Missing params : " + missingParams.toString();
		}
		return errMsg;
	}
}

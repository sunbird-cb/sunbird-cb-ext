package org.sunbird.common.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class OutboundRequestHandlerServiceImpl {
	private CbExtLogger log = new CbExtLogger(getClass().getName());

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * @param uri
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public Object fetchResultUsingPost(String uri, Object request) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		Object response = null;
		StringBuilder str = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Object> entity = new HttpEntity<>(request, headers);
			if (log.isDebugEnabled()) {
				str = new StringBuilder(this.getClass().getCanonicalName())
						.append(Constants.FETCH_RESULT_CONSTANT).append(System.lineSeparator());
				str.append(Constants.URI_CONSTANT).append(uri).append(System.lineSeparator());
				str.append(Constants.REQUEST_CONSTANT).append(mapper.writeValueAsString(request))
						.append(System.lineSeparator());
				log.debug(str.toString());
			}
			response = restTemplate.postForObject(uri, entity, Map.class);
			if (log.isDebugEnabled()) {
				str = new StringBuilder(this.getClass().getCanonicalName())
						.append(Constants.FETCH_RESULT_CONSTANT).append(System.lineSeparator());
				str.append(Constants.RESPONSE_CONSTANT).append(mapper.writeValueAsString(response))
						.append(System.lineSeparator());
				log.debug(str.toString());
			}
		} catch (HttpClientErrorException e) {
			try {
				response = (new ObjectMapper()).readValue(e.getResponseBodyAsString(),
						new TypeReference<HashMap<String, Object>>() {
						});
			} catch (Exception e1) {
			}
			log.error("Failed to get details. ", e);
		} catch (Exception e) {
			log.error(e);
		}
		return response;
	}

	/**
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	public Object fetchResult(String uri) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		Object response = null;
		try {
			if (log.isDebugEnabled()) {
				StringBuilder str = new StringBuilder(this.getClass().getCanonicalName())
						.append(Constants.FETCH_RESULT_CONSTANT).append(System.lineSeparator());
				str.append(Constants.URI_CONSTANT).append(uri).append(System.lineSeparator());
				log.debug(str.toString());
			}
			response = restTemplate.getForObject(uri, Map.class);
		} catch (HttpClientErrorException e) {
			try {
				response = (new ObjectMapper()).readValue(e.getResponseBodyAsString(),
						new TypeReference<HashMap<String, Object>>() {
						});
			} catch (Exception e1) {
			}
			log.error("Error received: " + e.getResponseBodyAsString(), e);
		} catch (Exception e) {
			log.error(e);
			try {
				log.warn("Error Response: " + mapper.writeValueAsString(response));
			} catch (Exception e1) {
			}
		}
		return response;
	}

	/**
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	public Object fetchUsingGetWithHeaders(String uri, Map<String, String> headersValues) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		ResponseEntity<Map> response = null;
		try {
			if (log.isDebugEnabled()) {
				StringBuilder str = new StringBuilder(this.getClass().getCanonicalName())
						.append(Constants.FETCH_RESULT_CONSTANT).append(System.lineSeparator());
				str.append(Constants.URI_CONSTANT).append(uri).append(System.lineSeparator());
				log.debug(str.toString());
			}
			HttpHeaders headers = new HttpHeaders();
			if (!CollectionUtils.isEmpty(headersValues)) {
				headersValues.forEach((k, v) -> headers.set(k, v));
			}
			HttpEntity entity = new HttpEntity(headers);
			response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
			return response.getBody();
		}catch (Exception e) {
			log.error(e);
		}
		return null;
	}

	public Object fetchUsingGetWithHeadersProfile(String uri, Map<String, String> headersValues) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		Map<String, Object> response = null;
		try {
			if (log.isDebugEnabled()) {
				StringBuilder str = new StringBuilder(this.getClass().getCanonicalName())
						.append(Constants.FETCH_RESULT_CONSTANT).append(System.lineSeparator());
				str.append(Constants.URI_CONSTANT).append(uri).append(System.lineSeparator());
				log.debug(str.toString());
			}
			HttpHeaders headers = new HttpHeaders();
			if (!CollectionUtils.isEmpty(headersValues)) {
				headersValues.forEach((k, v) -> headers.set(k, v));
			}
			HttpEntity<Object> entity = new HttpEntity<>(headers);
			response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class).getBody();
		} catch (HttpClientErrorException e) {
			try {
				response = (new ObjectMapper()).readValue(e.getResponseBodyAsString(),
						new TypeReference<HashMap<String, Object>>() {
						});
			} catch (Exception e1) {
			}
			log.error("Error received: " + e.getResponseBodyAsString(), e);
		} catch (Exception e) {
			log.error(e);
			try {
				log.warn("Error Response: " + mapper.writeValueAsString(response));
			} catch (Exception e1) {
			}
		}
		return response;
	}

	public Map<String, Object> fetchResultUsingPost(String uri, Object request, Map<String, String> headersValues) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		Map<String, Object> response = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			if (!CollectionUtils.isEmpty(headersValues)) {
				headersValues.forEach((k, v) -> headers.set(k, v));
			}
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Object> entity = new HttpEntity<>(request, headers);
			if (log.isDebugEnabled()) {
				StringBuilder str = new StringBuilder(this.getClass().getCanonicalName()).append(".fetchResult")
						.append(System.lineSeparator());
				str.append("URI: ").append(uri).append(System.lineSeparator());
				str.append("Request: ").append(mapper.writeValueAsString(request)).append(System.lineSeparator());
				log.debug(str.toString());
			}
			response = restTemplate.postForObject(uri, entity, Map.class);
			if (log.isDebugEnabled()) {
				StringBuilder str = new StringBuilder("Response: ");
				str.append(mapper.writeValueAsString(response)).append(System.lineSeparator());
				log.debug(str.toString());
			}
		} catch (HttpClientErrorException hce) {
			try {
				response = (new ObjectMapper()).readValue(hce.getResponseBodyAsString(),
						new TypeReference<HashMap<String, Object>>() {
						});
			} catch (Exception e1) {
			}
			log.error("Error received: " + hce.getResponseBodyAsString(), hce);
		} catch(JsonProcessingException e) {
			log.error(e);
			try {
				log.warn("Error Response: " + mapper.writeValueAsString(response));
			} catch (Exception e1) {
			}
		}
		return response;
	}

	public Map<String, Object> fetchResultUsingPatch(String uri, Object request, Map<String, String> headersValues) {
		Map<String, Object> response = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			if (!CollectionUtils.isEmpty(headersValues)) {
				headersValues.forEach((k, v) -> headers.set(k, v));
			}
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Object> entity = new HttpEntity<>(request, headers);
			if (log.isDebugEnabled()) {
				logDetails(uri, request);
			}
			response = restTemplate.patchForObject(uri, entity, Map.class);
			if (log.isDebugEnabled()) {
				logDetails(uri, response);
			}
		} catch (HttpClientErrorException e) {
			try {
				response = (new ObjectMapper()).readValue(e.getResponseBodyAsString(),
						new TypeReference<HashMap<String, Object>>() {
						});
			} catch (Exception e1) {
			}
			log.error("Error received: " + e.getResponseBodyAsString(), e);
		}
		if (response == null) {
			return MapUtils.EMPTY_MAP;
		}
		return response;
	}

	private void logDetails(String uri, Object objectDetails) {
		try {
			StringBuilder str = new StringBuilder(this.getClass().getCanonicalName()).append(".fetchResult")
					.append(System.lineSeparator());
			str.append("URI: ").append(uri).append(System.lineSeparator());
			str.append("Request/Response: ").append((new ObjectMapper()).writeValueAsString(objectDetails))
					.append(System.lineSeparator());
			log.debug(str.toString());
		} catch (JsonProcessingException je) {
		}
	}
}
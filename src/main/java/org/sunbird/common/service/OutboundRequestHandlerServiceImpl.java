package org.sunbird.common.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;

import com.fasterxml.jackson.core.JsonProcessingException;
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
		try {
			HttpHeaders headers = new HttpHeaders();
			HttpEntity<Object> entity = new HttpEntity<>(request, headers);
			response = restTemplate.postForObject(uri, entity, Map.class);
			if (log.isDebugEnabled()) {
				StringBuilder str = new StringBuilder(this.getClass().getCanonicalName())
						.append(Constants.FETCH_RESULT_CONSTANT).append(System.lineSeparator());
				str.append(Constants.URI_CONSTANT).append(uri).append(System.lineSeparator());
				str.append(Constants.REQUEST_CONSTANT).append(mapper.writeValueAsString(request))
						.append(System.lineSeparator());
				str.append(Constants.RESPONSE_CONSTANT).append(mapper.writeValueAsString(response))
						.append(System.lineSeparator());
				log.debug(str.toString());
			}
		} catch (HttpClientErrorException e) {
			log.error(e);
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
			log.error(e);
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
		} catch (HttpClientErrorException e) {
			log.error(e);
		} catch (Exception e) {
			log.error(e);
		}
		return response.getBody();
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
			log.error(e);
		} catch (Exception e) {
			log.error(e);
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
		} catch (HttpClientErrorException | JsonProcessingException e) {
			log.error(e);
			try {
				log.warn("Error Response: " + mapper.writeValueAsString(response));
			} catch (Exception e1) {
			}
		}
		return response;
	}

	public Map<String, Object> fetchResultUsingPatch(String uri, Object request, Map<String, String> headersValues) {
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
			response = restTemplate.patchForObject(uri, entity, Map.class);
			if (log.isDebugEnabled()) {
				StringBuilder str = new StringBuilder("Response: ");
				str.append(mapper.writeValueAsString(response)).append(System.lineSeparator());
				log.debug(str.toString());
			}
		} catch (HttpClientErrorException | JsonProcessingException e) {
			log.error(e);
		}
		return response;
	}
}
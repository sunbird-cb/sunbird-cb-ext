package org.sunbird.common.model;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

public class SBApiResponse {

	private String id;
	private String ver;
	private String ts;
	private SunbirdApiRespParam params;
	private HttpStatus responseCode;

	private transient Map<String, Object> response = new HashMap<>();

	public SBApiResponse() {
		this.ver = "v1";
		this.ts = new Timestamp(System.currentTimeMillis()).toString();
		this.params = new SunbirdApiRespParam();
	}

	public SBApiResponse(String id) {
		this();
		this.id = id;
	}

	public boolean containsKey(String key) {
		return response.containsKey(key);
	}

	public Object get(String key) {
		return response.get(key);
	}

	public String getId() {
		return id;
	}

	public SunbirdApiRespParam getParams() {
		return params;
	}

	public HttpStatus getResponseCode() {
		return responseCode;
	}

	public Map<String, Object> getResult() {
		return response;
	}

	public String getTs() {
		return ts;
	}

	public String getVer() {
		return ver;
	}

	public void put(String key, Object vo) {
		response.put(key, vo);
	}

	public void putAll(Map<String, Object> map) {
		response.putAll(map);
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setParams(SunbirdApiRespParam params) {
		this.params = params;
	}

	public void setResponseCode(HttpStatus responseCode) {
		this.responseCode = responseCode;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public void setVer(String ver) {
		this.ver = ver;
	}
}

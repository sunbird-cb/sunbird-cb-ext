package org.sunbird.catalog.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FrameworkResponse {
	private String id;
	private String ver;
	private FrameworkResponseParams params;
	private String responseCode;
	private FrameworkResponseResult result;

	public String getId() {
		return id;
	}

	public FrameworkResponseParams getParams() {
		return params;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public FrameworkResponseResult getResult() {
		return result;
	}

	public String getVer() {
		return ver;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setParams(FrameworkResponseParams params) {
		this.params = params;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public void setResult(FrameworkResponseResult result) {
		this.result = result;
	}

	public void setVer(String ver) {
		this.ver = ver;
	}

}

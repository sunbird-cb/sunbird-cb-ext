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

	public void setId(String id) {
		this.id = id;
	}

	public String getVer() {
		return ver;
	}

	public void setVer(String ver) {
		this.ver = ver;
	}

	public FrameworkResponseParams getParams() {
		return params;
	}

	public void setParams(FrameworkResponseParams params) {
		this.params = params;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public FrameworkResponseResult getResult() {
		return result;
	}

	public void setResult(FrameworkResponseResult result) {
		this.result = result;
	}

}

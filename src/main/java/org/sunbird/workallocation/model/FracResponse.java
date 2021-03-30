package org.sunbird.workallocation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FracResponse {
	private FracStatusInfo statusInfo;
	private ResponseData responseData;

	public FracStatusInfo getStatusInfo() {
		return statusInfo;
	}

	public void setStatusInfo(FracStatusInfo statusInfo) {
		this.statusInfo = statusInfo;
	}

	public ResponseData getResponseData() {
		return responseData;
	}

	public void setResponseData(ResponseData responseData) {
		this.responseData = responseData;
	}
}

package org.sunbird.common.model;

import java.util.List;
import java.util.Map;

import org.sunbird.workallocation.model.FracStatusInfo;

public class FracApiResponse {
	private FracStatusInfo statusInfo;
	private List<Map<String, Object>> responseData;

	public FracStatusInfo getStatusInfo() {
		return statusInfo;
	}

	public void setStatusInfo(FracStatusInfo statusInfo) {
		this.statusInfo = statusInfo;
	}

	public List<Map<String, Object>> getResponseData() {
		return responseData;
	}

	public void setResponseData(List<Map<String, Object>> responseData) {
		this.responseData = responseData;
	}
}

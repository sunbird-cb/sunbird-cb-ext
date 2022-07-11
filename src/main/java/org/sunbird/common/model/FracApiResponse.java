package org.sunbird.common.model;

import java.util.List;

import org.sunbird.searchby.model.FracCommonInfo;
import org.sunbird.workallocation.model.FracStatusInfo;

public class FracApiResponse {
	private FracStatusInfo statusInfo;
	private List<FracCommonInfo> responseData;

	public FracStatusInfo getStatusInfo() {
		return statusInfo;
	}

	public void setStatusInfo(FracStatusInfo statusInfo) {
		this.statusInfo = statusInfo;
	}

	public List<FracCommonInfo> getResponseData() {
		return responseData;
	}

	public void setResponseData(List<FracCommonInfo> responseData) {
		this.responseData = responseData;
	}
}

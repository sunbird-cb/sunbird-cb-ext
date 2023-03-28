package org.sunbird.searchby.model;

import org.sunbird.workallocation.model.FracStatusInfo;

import java.util.List;

public class PositionListResponse {
	private FracStatusInfo statusInfo;
	private List<MasterData> responseData;

	public FracStatusInfo getStatusInfo() {
		return statusInfo;
	}

	public void setStatusInfo(FracStatusInfo statusInfo) {
		this.statusInfo = statusInfo;
	}

	public List<MasterData> getResponseData() {
		return responseData;
	}

	public void setResponseData(List<MasterData> responseData) {
		this.responseData = responseData;
	}
}

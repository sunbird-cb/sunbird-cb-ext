package org.sunbird.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchUserApiResp {
	private String id;
	private String ver;
	private String ets;
	private SunbirdApiRespParam params;
	private String responseCode;
	private SearchUserApiRespResult result;

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

	public String getEts() {
		return ets;
	}

	public void setEts(String ets) {
		this.ets = ets;
	}

	public SunbirdApiRespParam getParams() {
		return params;
	}

	public void setParams(SunbirdApiRespParam params) {
		this.params = params;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public SearchUserApiRespResult getResult() {
		return result;
	}

	public void setResult(SearchUserApiRespResult result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "SearchUserApiResp{" +
				"id='" + id + '\'' +
				", ver='" + ver + '\'' +
				", ets='" + ets + '\'' +
				", params=" + params +
				", responseCode='" + responseCode + '\'' +
				", result=" + result +
				'}';
	}
}

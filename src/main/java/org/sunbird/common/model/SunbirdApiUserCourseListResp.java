package org.sunbird.common.model;

public class SunbirdApiUserCourseListResp {
	private String id;
	private String ver;
	private String ts;
	private SunbirdApiRespParam params;
	private String responseCode;
	private SunbirdApiUserCourseResult result;

	public String getId() {
		return id;
	}

	public SunbirdApiRespParam getParams() {
		return params;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public SunbirdApiUserCourseResult getResult() {
		return result;
	}

	public String getTs() {
		return ts;
	}

	public String getVer() {
		return ver;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setParams(SunbirdApiRespParam params) {
		this.params = params;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public void setResult(SunbirdApiUserCourseResult result) {
		this.result = result;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public void setVer(String ver) {
		this.ver = ver;
	}
}

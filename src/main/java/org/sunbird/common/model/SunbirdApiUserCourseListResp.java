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

    public void setId(String id) {
        this.id = id;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
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

    public SunbirdApiUserCourseResult getResult() {
        return result;
    }

    public void setResult(SunbirdApiUserCourseResult result) {
        this.result = result;
    }
}

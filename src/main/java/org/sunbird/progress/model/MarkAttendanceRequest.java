package org.sunbird.progress.model;

import org.sunbird.common.model.SunbirdApiRequest;

import java.util.HashMap;

public class MarkAttendanceRequest {

    private SunbirdApiRequest requestBody;

    private HashMap<String, String> headersValues;

    public SunbirdApiRequest getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(SunbirdApiRequest requestBody) {
        this.requestBody = requestBody;
    }

    public HashMap<String, String> getHeadersValues() {
        return headersValues;
    }

    public void setHeadersValues(HashMap<String, String> headerValues) {
        this.headersValues = headerValues;
    }
}



package org.sunbird.progress.model;

import org.sunbird.common.model.SunbirdApiRequest;

import java.util.HashMap;
import java.util.Map;

public class UpdateContentProgressRequest {

    private SunbirdApiRequest requestBody;

    private HashMap<String, String> headersValues;

    public SunbirdApiRequest getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(SunbirdApiRequest requestBody) {
        this.requestBody = requestBody;
    }

    public Map<String, String> getHeadersValues() {
        return headersValues;
    }

    public void setHeadersValues(Map<String, String> headerValues) {
        this.headersValues = (HashMap<String, String>) headerValues;
    }
}



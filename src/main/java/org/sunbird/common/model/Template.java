package org.sunbird.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Template {

    private String data;
    private String id;
    private Map<String, Object> params;



    @Override
    public String toString() {
        return "Template{" +
                "data='" + data + '\'' +
                ", id='" + id + '\'' +
                ", params=" + params +
                '}';
    }

    public Template(String data, String id, Map<String, Object> params) {
        this.data = data;
        this.id = id;
        this.params = params;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

}

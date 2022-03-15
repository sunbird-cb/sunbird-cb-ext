
package org.sunbird.assessment.model;

import java.io.Serializable;

import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "ver",
    "ts",
    "params",
    "responseCode",
    "result"
})

public class Response extends JdkSerializationRedisSerializer implements Serializable{
	
	private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id;
    @JsonProperty("ver")
    private String ver;
    @JsonProperty("ts")
    private String ts;
    @JsonProperty("params")
    private Params params;
    @JsonProperty("responseCode")
    private String responseCode;
    @JsonProperty("result")
    private Result result;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Response() {
    }

    /**
     * 
     * @param result
     * @param ver
     * @param id
     * @param params
     * @param ts
     * @param responseCode
     */
    public Response(String id, String ver, String ts, Params params, String responseCode, Result result) {
        super();
        this.id = id;
        this.ver = ver;
        this.ts = ts;
        this.params = params;
        this.responseCode = responseCode;
        this.result = result;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public Response withId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("ver")
    public String getVer() {
        return ver;
    }

    @JsonProperty("ver")
    public void setVer(String ver) {
        this.ver = ver;
    }

    public Response withVer(String ver) {
        this.ver = ver;
        return this;
    }

    @JsonProperty("ts")
    public String getTs() {
        return ts;
    }

    @JsonProperty("ts")
    public void setTs(String ts) {
        this.ts = ts;
    }

    public Response withTs(String ts) {
        this.ts = ts;
        return this;
    }

    @JsonProperty("params")
    public Params getParams() {
        return params;
    }

    @JsonProperty("params")
    public void setParams(Params params) {
        this.params = params;
    }

    public Response withParams(Params params) {
        this.params = params;
        return this;
    }

    @JsonProperty("responseCode")
    public String getResponseCode() {
        return responseCode;
    }

    @JsonProperty("responseCode")
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public Response withResponseCode(String responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    @JsonProperty("result")
    public Result getResult() {
        return result;
    }

    @JsonProperty("result")
    public void setResult(Result result) {
        this.result = result;
    }

    public Response withResult(Result result) {
        this.result = result;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Response.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("ver");
        sb.append('=');
        sb.append(((this.ver == null)?"<null>":this.ver));
        sb.append(',');
        sb.append("ts");
        sb.append('=');
        sb.append(((this.ts == null)?"<null>":this.ts));
        sb.append(',');
        sb.append("params");
        sb.append('=');
        sb.append(((this.params == null)?"<null>":this.params));
        sb.append(',');
        sb.append("responseCode");
        sb.append('=');
        sb.append(((this.responseCode == null)?"<null>":this.responseCode));
        sb.append(',');
        sb.append("result");
        sb.append('=');
        sb.append(((this.result == null)?"<null>":this.result));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}

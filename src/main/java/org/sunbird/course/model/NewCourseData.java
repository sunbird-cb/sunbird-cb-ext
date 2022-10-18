package org.sunbird.course.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "ver", "ts", "params", "responseCode", "result" })
public class NewCourseData {

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

	@JsonProperty("id")
	public String getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty("ver")
	public String getVer() {
		return ver;
	}

	@JsonProperty("ver")
	public void setVer(String ver) {
		this.ver = ver;
	}

	@JsonProperty("ts")
	public String getTs() {
		return ts;
	}

	@JsonProperty("ts")
	public void setTs(String ts) {
		this.ts = ts;
	}

	@JsonProperty("params")
	public Params getParams() {
		return params;
	}

	@JsonProperty("params")
	public void setParams(Params params) {
		this.params = params;
	}

	@JsonProperty("responseCode")
	public String getResponseCode() {
		return responseCode;
	}

	@JsonProperty("responseCode")
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	@JsonProperty("result")
	public Result getResult() {
		return result;
	}

	@JsonProperty("result")
	public void setResult(Result result) {
		this.result = result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(NewCourseData.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this)))
				.append('[');
		sb.append("id");
		sb.append('=');
		sb.append(((this.id == null) ? "<null>" : this.id));
		sb.append(',');
		sb.append("ver");
		sb.append('=');
		sb.append(((this.ver == null) ? "<null>" : this.ver));
		sb.append(',');
		sb.append("ts");
		sb.append('=');
		sb.append(((this.ts == null) ? "<null>" : this.ts));
		sb.append(',');
		sb.append("params");
		sb.append('=');
		sb.append(((this.params == null) ? "<null>" : this.params));
		sb.append(',');
		sb.append("responseCode");
		sb.append('=');
		sb.append(((this.responseCode == null) ? "<null>" : this.responseCode));
		sb.append(',');
		sb.append("result");
		sb.append('=');
		sb.append(((this.result == null) ? "<null>" : this.result));
		sb.append(',');
		if (sb.charAt((sb.length() - 1)) == ',') {
			sb.setCharAt((sb.length() - 1), ']');
		} else {
			sb.append(']');
		}
		return sb.toString();
	}

}
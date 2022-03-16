package org.sunbird.telemetry.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "channel", "pdata", "env", "cdata", "rollup" })

public class Context {

	@JsonProperty("channel")
	private Long channel;
	@JsonProperty("pdata")
	private Pdata pdata;
	@JsonProperty("env")
	private String env;
	@JsonProperty("cdata")
	private List<java.lang.Object> cdata = null;
	@JsonProperty("rollup")
	private Rollup rollup;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public Context() {
	}

	/**
	 *
	 * @param pdata
	 * @param channel
	 * @param env
	 * @param cdata
	 * @param rollup
	 */
	public Context(Long channel, Pdata pdata, String env, List<java.lang.Object> cdata, Rollup rollup) {
		super();
		this.channel = channel;
		this.pdata = pdata;
		this.env = env;
		this.cdata = cdata;
		this.rollup = rollup;
	}

	@JsonProperty("channel")
	public Long getChannel() {
		return channel;
	}

	@JsonProperty("channel")
	public void setChannel(Long channel) {
		this.channel = channel;
	}

	@JsonProperty("pdata")
	public Pdata getPdata() {
		return pdata;
	}

	@JsonProperty("pdata")
	public void setPdata(Pdata pdata) {
		this.pdata = pdata;
	}

	@JsonProperty("env")
	public String getEnv() {
		return env;
	}

	@JsonProperty("env")
	public void setEnv(String env) {
		this.env = env;
	}

	@JsonProperty("cdata")
	public List<java.lang.Object> getCdata() {
		return cdata;
	}

	@JsonProperty("cdata")
	public void setCdata(List<java.lang.Object> cdata) {
		this.cdata = cdata;
	}

	@JsonProperty("rollup")
	public Rollup getRollup() {
		return rollup;
	}

	@JsonProperty("rollup")
	public void setRollup(Rollup rollup) {
		this.rollup = rollup;
	}

}
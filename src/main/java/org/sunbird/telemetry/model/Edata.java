package org.sunbird.telemetry.model;

import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "state", "props", "login_time" })

public class Edata {

	@JsonProperty("state")
	private String state;
	@JsonProperty("props")
	private List<String> props = null;
	@JsonProperty("login_time")
	private Long loginTime;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public Edata() {
	}

	/**
	 *
	 * @param loginTime
	 * @param state
	 * @param props
	 */
	public Edata(String state, List<String> props, Long loginTime) {
		super();
		this.state = state;
		this.props = props;
		this.loginTime = loginTime;
	}

	@JsonProperty("state")
	public String getState() {
		return state;
	}

	@JsonProperty("state")
	public void setState(String state) {
		this.state = state;
	}

	@JsonProperty("props")
	public List<String> getProps() {
		return props;
	}

	@JsonProperty("props")
	public void setProps(List<String> props) {
		this.props = props;
	}

	@JsonProperty("login_time")
	public Long getLoginTime() {
		return loginTime;
	}

	@JsonProperty("login_time")
	public void setLoginTime(Long loginTime) {
		this.loginTime = loginTime;
	}

}
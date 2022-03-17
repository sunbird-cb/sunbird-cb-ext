package org.sunbird.telemetry.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "l1" })

public class Rollup {

	@JsonProperty("l1")
	private Long l1;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public Rollup() {
	}

	/**
	 *
	 * @param l1
	 */
	public Rollup(Long l1) {
		super();
		this.l1 = l1;
	}

	@JsonProperty("l1")
	public Long getL1() {
		return l1;
	}

	@JsonProperty("l1")
	public void setL1(Long l1) {
		this.l1 = l1;
	}

}
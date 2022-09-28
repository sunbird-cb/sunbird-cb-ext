package org.sunbird.course.model;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "min", "max" })
@Generated("jsonschema2pojo")
public class LastUpdatedOn {

	@JsonProperty("min")
	private String min;
	@JsonProperty("max")
	private String max;

	@JsonProperty("min")
	public String getMin() {
		return min;
	}

	@JsonProperty("min")
	public void setMin(String min) {
		this.min = min;
	}

	@JsonProperty("max")
	public String getMax() {
		return max;
	}

	@JsonProperty("max")
	public void setMax(String max) {
		this.max = max;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(LastUpdatedOn.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this)))
				.append('[');
		sb.append("min");
		sb.append('=');
		sb.append(((this.min == null) ? "<null>" : this.min));
		sb.append(',');
		sb.append("max");
		sb.append('=');
		sb.append(((this.max == null) ? "<null>" : this.max));
		sb.append(',');
		if (sb.charAt((sb.length() - 1)) == ',') {
			sb.setCharAt((sb.length() - 1), ']');
		} else {
			sb.append(']');
		}
		return sb.toString();
	}

}
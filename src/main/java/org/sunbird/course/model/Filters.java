package org.sunbird.course.model;

import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "primaryCategory", "contentType", "lastUpdatedOn" })
@Generated("jsonschema2pojo")
public class Filters {

	@JsonProperty("primaryCategory")
	private List<String> primaryCategory = null;
	@JsonProperty("contentType")
	private List<String> contentType = null;
	@JsonProperty("lastUpdatedOn")
	private LastUpdatedOn lastUpdatedOn;

	@JsonProperty("primaryCategory")
	public List<String> getPrimaryCategory() {
		return primaryCategory;
	}

	@JsonProperty("primaryCategory")
	public void setPrimaryCategory(List<String> primaryCategory) {
		this.primaryCategory = primaryCategory;
	}

	@JsonProperty("contentType")
	public List<String> getContentType() {
		return contentType;
	}

	@JsonProperty("contentType")
	public void setContentType(List<String> contentType) {
		this.contentType = contentType;
	}

	@JsonProperty("lastUpdatedOn")
	public LastUpdatedOn getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	@JsonProperty("lastUpdatedOn")
	public void setLastUpdatedOn(LastUpdatedOn lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Filters.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this)))
				.append('[');
		sb.append("primaryCategory");
		sb.append('=');
		sb.append(((this.primaryCategory == null) ? "<null>" : this.primaryCategory));
		sb.append(',');
		sb.append("contentType");
		sb.append('=');
		sb.append(((this.contentType == null) ? "<null>" : this.contentType));
		sb.append(',');
		sb.append("lastUpdatedOn");
		sb.append('=');
		sb.append(((this.lastUpdatedOn == null) ? "<null>" : this.lastUpdatedOn));
		sb.append(',');
		if (sb.charAt((sb.length() - 1)) == ',') {
			sb.setCharAt((sb.length() - 1), ']');
		} else {
			sb.append(']');
		}
		return sb.toString();
	}

}
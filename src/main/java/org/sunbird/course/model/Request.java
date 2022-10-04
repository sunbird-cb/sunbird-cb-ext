package org.sunbird.course.model;

import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "filters", "offset", "limit", "query", "sort_by", "fields" })
@Generated("jsonschema2pojo")
public class Request {

	@JsonProperty("filters")
	private Filters filters;
	@JsonProperty("offset")
	private Integer offset;
	@JsonProperty("limit")
	private Integer limit;
	@JsonProperty("query")
	private String query;
	@JsonProperty("sort_by")
	private SortBy sortBy;
	@JsonProperty("fields")
	private List<String> fields = null;

	@JsonProperty("filters")
	public Filters getFilters() {
		return filters;
	}

	@JsonProperty("filters")
	public void setFilters(Filters filters) {
		this.filters = filters;
	}

	@JsonProperty("offset")
	public Integer getOffset() {
		return offset;
	}

	@JsonProperty("offset")
	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	@JsonProperty("limit")
	public Integer getLimit() {
		return limit;
	}

	@JsonProperty("limit")
	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	@JsonProperty("query")
	public String getQuery() {
		return query;
	}

	@JsonProperty("query")
	public void setQuery(String query) {
		this.query = query;
	}

	@JsonProperty("sort_by")
	public SortBy getSortBy() {
		return sortBy;
	}

	@JsonProperty("sort_by")
	public void setSortBy(SortBy sortBy) {
		this.sortBy = sortBy;
	}

	@JsonProperty("fields")
	public List<String> getFields() {
		return fields;
	}

	@JsonProperty("fields")
	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Request.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this)))
				.append('[');
		sb.append("filters");
		sb.append('=');
		sb.append(((this.filters == null) ? "<null>" : this.filters));
		sb.append(',');
		sb.append("offset");
		sb.append('=');
		sb.append(((this.offset == null) ? "<null>" : this.offset));
		sb.append(',');
		sb.append("limit");
		sb.append('=');
		sb.append(((this.limit == null) ? "<null>" : this.limit));
		sb.append(',');
		sb.append("query");
		sb.append('=');
		sb.append(((this.query == null) ? "<null>" : this.query));
		sb.append(',');
		sb.append("sortBy");
		sb.append('=');
		sb.append(((this.sortBy == null) ? "<null>" : this.sortBy));
		sb.append(',');
		sb.append("fields");
		sb.append('=');
		sb.append(((this.fields == null) ? "<null>" : this.fields));
		sb.append(',');
		if (sb.charAt((sb.length() - 1)) == ',') {
			sb.setCharAt((sb.length() - 1), ']');
		} else {
			sb.append(']');
		}
		return sb.toString();
	}

}
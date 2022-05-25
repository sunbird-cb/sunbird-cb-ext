/** */
package org.sunbird.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class will take input for elastic search query
 *
 * @author Manzarul
 */
public class SearchDTO {

  @SuppressWarnings("rawtypes")
  private List<Map> properties;

  private List<Map<String, String>> facets = new ArrayList<>();
  private List<String> fields;
  private List<String> excludedFields;
  private Map<String, Object> sortBy = new HashMap<>();
  private String operation;
  private String query;
  private List<String> queryFields;

  private Integer limit = 250;
  private Integer offset = 0;
  private boolean fuzzySearch = false;
  // additional properties will hold , filters, exist , not exist
  private Map<String, Object> additionalProperties = new HashMap<>();
  private Map<String, Integer> softConstraints = new HashMap<>();
  private List<Map<String, Object>> groupQuery = new ArrayList<>();
  private List<String> mode = new ArrayList<>();
  //added for multiMatch search option
  private Map<String,List<String>> multiSearchFields = new HashMap<>();

  public List<Map<String, Object>> getGroupQuery() {
    return groupQuery;
  }

  public void setGroupQuery(List<Map<String, Object>> groupQuery) {
    this.groupQuery = groupQuery;
  }

  public SearchDTO() {
    super();
  }

  @SuppressWarnings("rawtypes")
  public SearchDTO(List<Map> properties, String operation, int limit) {
    super();
    this.properties = properties;
    this.operation = operation;
    this.limit = limit;
  }

  @SuppressWarnings("rawtypes")
  public List<Map> getProperties() {
    return properties;
  }

  @SuppressWarnings("rawtypes")
  public void setProperties(List<Map> properties) {
    this.properties = properties;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public List<Map<String, String>> getFacets() {
    return facets;
  }

  public void setFacets(List<Map<String, String>> facets) {
    this.facets = facets;
  }

  public Map<String, Object> getSortBy() {
    return sortBy;
  }

  public void setSortBy(Map<String, Object> sortBy) {
    this.sortBy = sortBy;
  }

  public boolean isFuzzySearch() {
    return fuzzySearch;
  }

  public void setFuzzySearch(boolean fuzzySearch) {
    this.fuzzySearch = fuzzySearch;
  }

  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  public void setAdditionalProperties(Map<String, Object> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  public Object getAdditionalProperty(String key) {
    return additionalProperties.get(key);
  }

  public void addAdditionalProperty(String key, Object value) {
    this.additionalProperties.put(key, value);
  }

  public List<String> getFields() {
    return fields;
  }

  public void setFields(List<String> fields) {
    this.fields = fields;
  }

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public Map<String, Integer> getSoftConstraints() {
    return softConstraints;
  }

  public void setSoftConstraints(Map<String, Integer> softConstraints) {
    this.softConstraints = softConstraints;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public List<String> getMode() {
    return mode;
  }

  public void setMode(List<String> mode) {
    this.mode = mode;
  }

  public List<String> getExcludedFields() {
    return excludedFields;
  }

  public void setExcludedFields(List<String> excludedFields) {
    this.excludedFields = excludedFields;
  }

  public List<String> getQueryFields() {
    return queryFields;
  }

  public void setQueryFields(List<String> queryFields) {
    this.queryFields = queryFields;
  }

  public Map<String, List<String>> getMultiSearchFields() { return multiSearchFields; }

  public void setMultiSearchFields(Map<String, List<String>> multiSearchFields) { this.multiSearchFields = multiSearchFields; }
}
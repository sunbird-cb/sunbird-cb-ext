package org.sunbird.common.util;

import java.util.Arrays;
import java.util.List;

/**
 * This class will contains all the key related to request and response.
 *
 * @author Manzarul
 */
public final class JsonKey {
  public static final String CONTENT = "content";
  public static final String COUNT = "count";
  public static final String DATE_HISTOGRAM = "DATE_HISTOGRAM";
  public static final String EXISTS = "exists";
  public static final String FACETS = "facets";
  public static final String FIELDS = "fields";
  public static final String FILTERS = "filters";
  public static final String GROUP_QUERY = "groupQuery";
  public static final String LIMIT = "limit";
  public static final String NAME = "name";
  public static final String NOT_EXISTS = "not_exists";
  public static final String OFFSET = "offset";
  public static final String QUERY = "query";
  public static final String QUERY_FIELDS = "queryFields";
  public static final String RESPONSE = "response";
  public static final String SORT_BY = "sort_by";
  public static final String SOFT_CONSTRAINTS = "softConstraints";
  public static final String ES_OR_OPERATION = "$or";
  public static final String NESTED_KEY_FILTER = "nestedFilters";
  public static final String NESTED_EXISTS = "nested_exists";
  public static final String NESTED_NOT_EXISTS = "nested_not_exists";
  public static final String MULTI_QUERY_SEARCH_FIELDS = "multiQuerySearchFields";

  private JsonKey() {}
}
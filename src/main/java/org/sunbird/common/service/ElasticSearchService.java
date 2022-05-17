package org.sunbird.common.service;

import org.elasticsearch.client.RestHighLevelClient;
import org.sunbird.common.model.SearchDTO;

import java.io.IOException;
import java.util.Map;

public interface ElasticSearchService {
  public static final String _DOC = "_doc";

  /**
   * Method to perform the elastic search on the basis of SearchDTO . SearchDTO contains the search
   * criteria like fields, facets, sort by , filters etc. here user can pass single type to search
   * or multiple type or null
   *
   * @return search result as Map.
   */
  public Map<String, Object> search(
          SearchDTO searchDTO, String index, RestHighLevelClient esClient) throws IOException;

}
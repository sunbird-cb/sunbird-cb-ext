package org.sunbird.common.util;

import org.sunbird.common.service.ElasticSearchService;
import org.sunbird.common.service.impl.ElasticSearchRestHighImpl;

public class EsClientFactory {

  private static ElasticSearchService restClient = null;

  /**
   * This method return REST/TCP client for elastic search
   *
   * @param type can be "tcp" or "rest"
   * @return ElasticSearchService with the respected type impl
   */
  public static ElasticSearchService getInstance(String type) {
    if (Constants.REST.equals(type)) {
      return getRestClient();
    }
    return null;
  }

  private static ElasticSearchService getRestClient() {
    if (restClient == null) {
      synchronized (EsClientFactory.class) {
        if (restClient == null) {
          restClient = new ElasticSearchRestHighImpl();
        }
      }
    }
    return restClient;
  }
}
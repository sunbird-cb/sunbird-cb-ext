package org.sunbird.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class RequestInterceptor {

  private static Logger logger = LoggerFactory.getLogger(RequestInterceptor.class.getName());
  private static ConcurrentHashMap<String, Short> apiHeaderIgnoreMap = new ConcurrentHashMap<>();

  private RequestInterceptor() {
  }

  public static String fetchUserIdFromAccessToken(String accessToken) {
    String clientAccessTokenId = null;
    if (accessToken != null) {
      try {
        clientAccessTokenId = AccessTokenValidator.verifyUserToken(accessToken);
        if (Constants._UNAUTHORIZED.equalsIgnoreCase(clientAccessTokenId)) {
          clientAccessTokenId = null;
        }
      } catch (Exception ex) {
        String errMsg = "Exception occurred while fetching the userid from the access token. Exception: " + ex.getMessage();
        logger.error(errMsg, ex);
        clientAccessTokenId = null;
      }
    }
    return clientAccessTokenId;
  }
}
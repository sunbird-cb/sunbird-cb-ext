package org.sunbird.common.util;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Manzarul This class will connect to key cloak server and provide the connection to do
 *     other operations.
 */
public class KeyCloakConnectionProvider {

  private static Logger logger = LoggerFactory.getLogger(KeyCloakConnectionProvider.class.getName());

  private static Keycloak keycloak;
  private static PropertiesCache cache = PropertiesCache.getInstance();
  public static String SSO_URL = null;
  public static String SSO_REALM = null;
  public static String CLIENT_ID = null;

  static {
    try {
      initialiseConnection();
    } catch (Exception e) {
      logger.error(
          "Exception occurred while initializing keycloak connection: " + e.getMessage(), e);
    }
    registerShutDownHook();
  }

  /**
   * Method to initializate the Keycloak connection
   *
   * @return Keycloak connection
   */
  public static Keycloak initialiseConnection() {
    keycloak = initialiseEnvConnection();
    if (keycloak != null) {
      return keycloak;
    }
    KeycloakBuilder keycloakBuilder =
        KeycloakBuilder.builder()
            .serverUrl(cache.getProperty(Constants.SSO_URL))
            .realm(cache.getProperty(Constants.SSO_REALM))
            .username(cache.getProperty(Constants.SSO_USERNAME))
            .password(cache.getProperty(Constants.SSO_PASSWORD))
            .clientId(cache.getProperty(Constants.SSO_CLIENT_ID))
            .resteasyClient(
                new ResteasyClientBuilder()
                    .connectionPoolSize(Integer.parseInt(cache.getProperty(Constants.SSO_POOL_SIZE)))
                    .build());
    if (cache.getProperty(Constants.SSO_CLIENT_SECRET) != null
        && !(cache.getProperty(Constants.SSO_CLIENT_SECRET).equals(Constants.SSO_CLIENT_SECRET))) {
      keycloakBuilder.clientSecret(cache.getProperty(Constants.SSO_CLIENT_SECRET));
    }
    SSO_URL = cache.getProperty(Constants.SSO_URL);
    SSO_REALM = cache.getProperty(Constants.SSO_REALM);
    CLIENT_ID = cache.getProperty(Constants.SSO_CLIENT_ID);
    keycloak = keycloakBuilder.build();

    logger.info("key cloak instance is created successfully.");
    return keycloak;
  }

  /**
   * This method will provide the keycloak connection from environment variable. if environment
   * variable is not set then it will return null.
   *
   * @return Keycloak
   */
  private static Keycloak initialiseEnvConnection() {
    String url = System.getenv(Constants.SUNBIRD_SSO_URL);
    String username = System.getenv(Constants.SUNBIRD_SSO_USERNAME);
    String password = System.getenv(Constants.SUNBIRD_SSO_PASSWORD);
    String cleintId = System.getenv(Constants.SUNBIRD_SSO_CLIENT_ID);
    String clientSecret = System.getenv(Constants.SUNBIRD_SSO_CLIENT_SECRET);
    String relam = System.getenv(Constants.SUNBIRD_SSO_RELAM);
    if (StringUtils.isBlank(url)
        || StringUtils.isBlank(username)
        || StringUtils.isBlank(password)
        || StringUtils.isBlank(cleintId)
        || StringUtils.isBlank(relam)) {
      logger.info("key cloak connection is not provided by Environment variable.");
      return null;
    }
    SSO_URL = url;
    SSO_REALM = relam;
    CLIENT_ID = cleintId;
    KeycloakBuilder keycloakBuilder =
        KeycloakBuilder.builder()
            .serverUrl(url)
            .realm(relam)
            .username(username)
            .password(password)
            .clientId(cleintId)
            .resteasyClient(
                new ResteasyClientBuilder()
                    .connectionPoolSize(Integer.parseInt(cache.getProperty(Constants.SSO_POOL_SIZE)))
                    .build());

    if (StringUtils.isNotBlank(clientSecret)) {
      keycloakBuilder.clientSecret(clientSecret);
      logger.info("KeyCloakConnectionProvider:initialiseEnvConnection client sceret is provided.");
    }
    keycloakBuilder.grantType("client_credentials");
    keycloak = keycloakBuilder.build();
    logger.info("key cloak instance is created from Environment variable settings .");
    return keycloak;
  }

  /**
   * This method will provide key cloak connection instance.
   *
   * @return Keycloak
   */
  public static Keycloak getConnection() {
    if (keycloak != null) {
      return keycloak;
    } else {
      try {
        return initialiseConnection();
      } catch (Exception e) {
        logger.error("getConnection : " + e.getMessage(), e);
      }
    }
    return null;
  }

  /**
   * This class will be called by registerShutDownHook to register the call inside jvm , when jvm
   * terminate it will call the run method to clean up the resource.
   *
   * @author Manzarul
   */
  static class ResourceCleanUp extends Thread {
    @Override
    public void run() {
      if (null != keycloak) {
        keycloak.close();
      }
    }
  }

  /** Register the hook for resource clean up. this will be called when jvm shut down. */
  public static void registerShutDownHook() {
    Runtime runtime = Runtime.getRuntime();
    runtime.addShutdownHook(new ResourceCleanUp());
  }
}
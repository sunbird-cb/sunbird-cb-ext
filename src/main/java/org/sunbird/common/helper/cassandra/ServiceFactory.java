package org.sunbird.common.helper.cassandra;

import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.cassandra.utils.CassandraOperationImpl;

/**
 * This class will provide cassandraOperationImpl instance.
 *
 * @author Manzarul
 */
public class ServiceFactory {
  private static CassandraOperation operation = null;

  private ServiceFactory() {}

  /**
   * On call of this method , it will provide a new CassandraOperationImpl instance on each call.
   *
   * @return
   */
  public static CassandraOperation getInstance() {
    if (null == operation) {
      synchronized (ServiceFactory.class) {
        if (null == operation) {
          operation = new CassandraOperationImpl();
        }
      }
    }
    return operation;
  }
}
package org.sunbird.common.helper.cassandra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class CassandraConnectionMngrFactory {

  @Autowired
  @Qualifier("CassandraConnectionManagerImplHelper")
  private static CassandraConnectionManager instance;

  public static CassandraConnectionManager getInstance() {
    if (instance == null) {
      synchronized (CassandraConnectionMngrFactory.class) {
        if (instance == null) {
          instance = new CassandraConnectionManagerImpl();
        }
      }
    }
    return instance;
  }
}
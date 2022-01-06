package org.sunbird.cassandra.utils;

import com.datastax.driver.core.Session;

/**
 * Interface for cassandra connection manager , implementation would be Standalone and Embedde
 * cassandra connection manager 
 * @author fathima
 */
public interface CassandraConnectionManager {

	/**
	   * Method to get the cassandra session oject on basis of keyspace name provided .
	   * @param keyspaceName
	   * @return Session
	   */
	  Session getSession(String keyspaceName);
}

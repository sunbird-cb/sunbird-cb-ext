package org.sunbird.cassandra.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * This class will be used to read cassandratablecolumn properties file.
 * @author fathima
 *
 */

public class CassandraPropertyReader {

	private final Properties properties = new Properties();
	  private static final String file = "cassandratablecolumn.properties";
	  private static CassandraPropertyReader cassandraPropertyReader = null;

	  /** private default constructor 
	 * @throws IOException */
	  private CassandraPropertyReader() throws IOException {
	    InputStream in = this.getClass().getClassLoader().getResourceAsStream(file);
	    try {
	      properties.load(in);
	    } catch (IOException e) {
	    	throw e;
	    }
	  }

	  public static CassandraPropertyReader getInstance() {
	    if (null == cassandraPropertyReader) {
	      synchronized (CassandraPropertyReader.class) {
	        if (null == cassandraPropertyReader) {
	          try {
				cassandraPropertyReader = new CassandraPropertyReader();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        }
	      }
	    }
	    return cassandraPropertyReader;
	  }

	  /**
	   * Method to read value from resource file .
	   *
	   * @param key property value to read
	   * @return value corresponding to given key if found else will return key itself.
	   */
	  public String readProperty(String key) {
	    return properties.getProperty(key) != null ? properties.getProperty(key) : key;
	  }
}

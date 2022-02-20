package org.sunbird.cassandra.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class will be used to read cassandratablecolumn properties file.
 *
 * @author fathima
 *
 */

public class CassandraPropertyReader {
	private static final String FILE = "cassandratablecolumn.properties";
	private static CassandraPropertyReader cassandraPropertyReader = null;

	public static synchronized CassandraPropertyReader getInstance() {
		if (cassandraPropertyReader == null) {
			try {
				cassandraPropertyReader = new CassandraPropertyReader();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return cassandraPropertyReader;
	}

	private final Properties properties = new Properties();

	/**
	 * private default constructor
	 *
	 * @throws IOException
	 */
	private CassandraPropertyReader() throws IOException {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(FILE);
		try {
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Method to read value from resource file .
	 *
	 * @param key property value to read
	 * @return value corresponding to given key if found else will return key
	 *         itself.
	 */
	public String readProperty(String key) {
		return properties.getProperty(key) != null ? properties.getProperty(key) : key;
	}
}

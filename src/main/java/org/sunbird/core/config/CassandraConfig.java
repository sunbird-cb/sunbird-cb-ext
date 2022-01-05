package org.sunbird.core.config;

import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;

public abstract class CassandraConfig extends AbstractCassandraConfiguration {

	protected String contactPoints;
	protected int port;
	protected String keyspaceName;

	@Override
	public String getContactPoints() {
		return contactPoints;
	}

	@Override
	protected String getKeyspaceName() {
		return keyspaceName;
	}

	@Override
	protected boolean getMetricsEnabled() {
		return false;
	}

	@Override
	public int getPort() {
		return port;
	}

	public void setContactPoints(String contactPoints) {
		this.contactPoints = contactPoints;
	}

	public void setKeyspaceName(String keyspaceName) {
		this.keyspaceName = keyspaceName;
	}

	public void setPort(Integer port) {
		this.port = port;
	}
}
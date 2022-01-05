package org.sunbird.workallocation.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("work_allocation")
public class WorkAllocationCassandraModel {

	@PrimaryKey
	private WorkAllocationPrimaryKeyModel primaryKey;

	@Column("data")
	private String data;

	public WorkAllocationCassandraModel() {
	}

	public WorkAllocationCassandraModel(String id, String data) {
		this.primaryKey = new WorkAllocationPrimaryKeyModel();
		this.primaryKey.setId(id);
		this.data = data;
	}

	public String getData() {
		return data;
	}

	public WorkAllocationPrimaryKeyModel getPrimaryKey() {
		return primaryKey;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setPrimaryKey(WorkAllocationPrimaryKeyModel primaryKey) {
		this.primaryKey = primaryKey;
	}
}
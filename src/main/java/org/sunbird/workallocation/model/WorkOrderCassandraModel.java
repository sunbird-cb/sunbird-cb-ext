package org.sunbird.workallocation.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("work_order")
public class WorkOrderCassandraModel {

	@PrimaryKey
	private WorkOrderPrimaryKeyModel primaryKey;

	@Column("data")
	private String data;

	public WorkOrderCassandraModel() {
	}

	public WorkOrderCassandraModel(String id, String data) {
		this.primaryKey = new WorkOrderPrimaryKeyModel();
		this.primaryKey.setId(id);
		this.data = data;
	}

	public String getData() {
		return data;
	}

	public WorkOrderPrimaryKeyModel getPrimaryKey() {
		return primaryKey;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setPrimaryKey(WorkOrderPrimaryKeyModel primaryKey) {
		this.primaryKey = primaryKey;
	}
}
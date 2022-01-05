package org.sunbird.staff.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class StaffInfoPrimaryKeyModel {

	@PrimaryKeyColumn(name = "orgId", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String orgId;

	@PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
	private String id;

	public StaffInfoPrimaryKeyModel() {
	}

	public StaffInfoPrimaryKeyModel(String orgId, String id) {
		this.orgId = orgId;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

}

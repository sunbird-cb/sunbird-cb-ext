package org.sunbird.workallocation.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class UserWorkAllocationPrimaryKeyModel {

    private static final long serialVersionUID = 1L;
    @PrimaryKeyColumn(name = "userid", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String userId;


    @PrimaryKeyColumn(name = "workallocationid", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String workAllocationId;

    public String getWorkAllocationId() {
        return workAllocationId;
    }

    public void setWorkAllocationId(String workAllocationId) {
        this.workAllocationId = workAllocationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
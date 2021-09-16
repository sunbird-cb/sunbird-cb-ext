package org.sunbird.workallocation.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("user_work_allocation_mapping")
public class UserWorkAllocationMapping {

    public UserWorkAllocationMapping() {
        super();
    }

    public UserWorkAllocationMapping(String userId, String workAllocationId, String workOrderId, String status) {
        this.primaryKey = new UserWorkAllocationPrimaryKeyModel();
        this.primaryKey.setUserId(userId);
        this.primaryKey.setWorkAllocationId(workAllocationId);
        this.workOrderId = workOrderId;
        this.status = status;
    }

    @PrimaryKey
    private UserWorkAllocationPrimaryKeyModel primaryKey;

    @Column("workorderid")
    private String workOrderId;

    @Column("status")
    private String status;

    public UserWorkAllocationPrimaryKeyModel getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(UserWorkAllocationPrimaryKeyModel primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getStatus() {
        return status;
    }

    public String getWorkOrderId() {
        return workOrderId;
    }
    public void setWorkOrderId(String workOrderId) {
        this.workOrderId = workOrderId;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

package org.sunbird.workallocation.repo;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;
import org.sunbird.workallocation.model.WorkOrderCassandraModel;
import org.sunbird.workallocation.model.WorkOrderPrimaryKeyModel;

@Repository
public interface WorkOrderRepo extends CassandraRepository<WorkOrderCassandraModel, WorkOrderPrimaryKeyModel> {

}

package org.sunbird.workallocation.repo;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;
import org.sunbird.workallocation.model.WorkAllocationCassandraModel;
import org.sunbird.workallocation.model.WorkAllocationPrimaryKeyModel;

@Repository
public interface WorkAllocationRepo extends CassandraRepository<WorkAllocationCassandraModel, WorkAllocationPrimaryKeyModel> {

}
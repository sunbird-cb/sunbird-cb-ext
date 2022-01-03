package org.sunbird.workallocation.repo;

import java.util.List;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;
import org.sunbird.workallocation.model.WorkAllocationCassandraModel;
import org.sunbird.workallocation.model.WorkAllocationPrimaryKeyModel;

@Repository
public interface WorkAllocationRepo extends CassandraRepository<WorkAllocationCassandraModel, WorkAllocationPrimaryKeyModel> {

    @Query("SELECT * FROM work_allocation where id IN ?0")
    List<WorkAllocationCassandraModel> findByIdIn(List<String> userId);

}
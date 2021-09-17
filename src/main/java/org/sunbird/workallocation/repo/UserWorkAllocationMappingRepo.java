package org.sunbird.workallocation.repo;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;
import org.sunbird.workallocation.model.UserWorkAllocationMapping;
import org.sunbird.workallocation.model.UserWorkAllocationPrimaryKeyModel;

import java.util.List;

@Repository
public interface UserWorkAllocationMappingRepo extends CassandraRepository<UserWorkAllocationMapping, UserWorkAllocationPrimaryKeyModel> {

    @Query("SELECT * FROM user_work_allocation_mapping where userid=?0;")
    List<UserWorkAllocationMapping> findByUserId(String userId);
}

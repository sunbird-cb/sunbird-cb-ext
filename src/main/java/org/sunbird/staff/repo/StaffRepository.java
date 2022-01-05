package org.sunbird.staff.repo;

import java.util.List;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;
import org.sunbird.staff.model.StaffInfoModel;
import org.sunbird.staff.model.StaffInfoPrimaryKeyModel;

@Repository
public interface StaffRepository extends CassandraRepository<StaffInfoModel, StaffInfoPrimaryKeyModel> {

	@Query(" delete from org_staff_position where orgId=?0 and id=?1")
	public List<StaffInfoModel> deleteStaffDetails(String rootOrg, String id);

	@Query("select * from org_staff_position where orgId=?0 and position=?1")
	List<StaffInfoModel> getAllByOrgIdAndPosition(String orgId, String position);

	@Query(" select * from org_staff_position where orgId=?0")
	public List<StaffInfoModel> getStaffDetails(String rootOrg);

	@Query("update org_staff_position set totalPositionsFilled=?0, totalPositionsVacant=?1 where orgId=?2 and id=?3")
	public List<StaffInfoModel> updateStaffDetails(int totalPositionFilled, int totalPositionVacant, String rootOrg,
			String id);
}

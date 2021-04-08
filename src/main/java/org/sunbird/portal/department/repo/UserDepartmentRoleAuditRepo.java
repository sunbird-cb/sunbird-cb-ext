package org.sunbird.portal.department.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sunbird.portal.department.dto.UserDepartmentRoleAudit;

@Repository
public interface UserDepartmentRoleAuditRepo extends  JpaRepository<UserDepartmentRoleAudit, Integer> {

}

package org.sunbird.org.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.sunbird.org.model.OrgHierarchy;

public interface OrgHierarchyRepository extends JpaRepository<OrgHierarchy, Integer> {
    @Query(value = "SELECT * FROM org_hierarchy_v2 org WHERE LOWER(org.orgname) LIKE LOWER(CONCAT('%',?1, '%'))", nativeQuery = true)
    List<OrgHierarchy> searchOrgWithHierarchy(String orgName);
}

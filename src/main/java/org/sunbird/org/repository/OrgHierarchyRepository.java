package org.sunbird.org.repository;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.sunbird.org.model.OrgHierarchy;

public interface OrgHierarchyRepository extends JpaRepository<OrgHierarchy, Integer> {
    @Query(value = "SELECT * FROM org_hierarchy_v2 org WHERE org.orgname ~* ?1", nativeQuery = true)
    List<OrgHierarchy> searchOrgWithHierarchy(String orgName);

    List<OrgHierarchy> findAllByOrgName(String orgName);

    List<OrgHierarchy> findAllByMapId(String mapId);

    OrgHierarchy findByMapId(String mapId);

    @Query(value = "SELECT sborgid from org_hierarchy_v2 where mapid=?1", nativeQuery = true)
    String getSbOrgIdFromMapId(String mapId);

    @Query(value = "SELECT * FROM org_hierarchy_v2 where LOWER(parentmapid)=LOWER(?1)", nativeQuery = true)
    List<OrgHierarchy> findAllByParentMapId(String parentMapId);

    List<OrgHierarchy> findAllBySbOrgType(String orgType);

    OrgHierarchy findByChannel(String channel);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE org_hierarchy_v2 set sborgid=?2 where channel=?1", nativeQuery = true)
    void updateOrgIdForChannel(String channel, String sbOrgId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE org_hierarchy_v2 set sborgid=?2, sbrootorgid=?3 where channel=?1", nativeQuery = true)
    void updateSbOrgIdAndSbOrgRootIdForChannel(String channel, String sbOrgId, String sbRootOrgId);

    @Query(value = "SELECT sborgid from org_hierarchy_v2 where sbrootorgid=?1", nativeQuery = true)
    List<String> findAllBySbRootOrgId(String sbRootOrgId);

    @Query(value = "SELECT sborgid from org_hierarchy_v2 where sbrootorgid in (?1)", nativeQuery = true)
    List<String> fetchL2LevelOrgList(List<String> orgIdList);

    @Query(value = "SELECT * from org_hierarchy_v2 where mapid in (?1)", nativeQuery = true)
    List<OrgHierarchy> searchOrgForL1MapId(Set<String> l1MapIdSet);

    OrgHierarchy findByOrgNameAndParentMapId(String orgName, String parentMapId);
}

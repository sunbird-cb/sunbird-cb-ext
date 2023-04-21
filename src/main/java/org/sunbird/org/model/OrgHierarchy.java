package org.sunbird.org.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "org_hierarchy_v2")
public class OrgHierarchy {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "orgname")
    @NotNull
    private String orgName;

    @Column(name = "channel")
    @NotNull
    private String channel;

    @Column(name = "mapid")
    @NotNull
    private String mapId;

    @Column(name = "orgcode")
    @NotNull
    private String orgCode;

    @Column(name = "parentmapid")
    @NotNull
    private String parentMapId;

    @Column(name = "sborgid")
    @NotNull
    private String sbOrgId;

    @Column(name = "sbrootorgid")
    @NotNull
    private String sbRootOrgId;

    @Column(name = "sborgtype")
    @NotNull
    private String sbOrgType;

    @Column(name = "sborgsubtype")
    @NotNull
    private String sbOrgSubType;

    @Column(name = "l1mapid")
    @NotNull
    private String l1MapId;

    @Column(name = "l2mapid")
    @NotNull
    private String l2MapId;

    @Column(name = "l1orgname")
    @NotNull
    private String l1OrgName;

    @Column(name = "l2orgname")
    @NotNull
    private String l2OrgName;

    public OrgHierarchyInfo toOrgInfo() {
        OrgHierarchyInfo orgInfo = new OrgHierarchyInfo();
        orgInfo.setId(id);
        orgInfo.setChannel(channel);
        orgInfo.setOrgName(orgName);
        orgInfo.setMapId(mapId);
        orgInfo.setSbOrgId(sbOrgId);
        orgInfo.setSbOrgType(sbOrgType);
        orgInfo.setSbOrgSubType(sbOrgSubType);
        orgInfo.setSbRootOrgId(sbRootOrgId);
        orgInfo.setL1MapId(l1MapId);
        orgInfo.setL2MapId(l2MapId);
        orgInfo.setL1OrgName(l1OrgName);
        orgInfo.setL2OrgName(l2OrgName);
        return orgInfo;
    }
}

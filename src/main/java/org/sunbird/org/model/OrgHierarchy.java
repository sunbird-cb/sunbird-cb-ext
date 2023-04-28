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
    private String orgCode;

    @Column(name = "parentmapid")
    @NotNull
    private String parentMapId;

    @Column(name = "sborgid")
    private String sbOrgId;

    @Column(name = "sbrootorgid")
    private String sbRootOrgId;

    @Column(name = "sborgtype")
    private String sbOrgType;

    @Column(name = "sborgsubtype")
    private String sbOrgSubType;

    @Column(name = "l1mapid")
    private String l1MapId;

    @Column(name = "l2mapid")
    private String l2MapId;

    @Column(name = "l1orgname")
    private String l1OrgName;

    @Column(name = "l2orgname")
    private String l2OrgName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getParentMapId() {
        return parentMapId;
    }

    public void setParentMapId(String parentMapId) {
        this.parentMapId = parentMapId;
    }

    public String getSbOrgId() {
        return sbOrgId;
    }

    public void setSbOrgId(String sbOrgId) {
        this.sbOrgId = sbOrgId;
    }

    public String getSbRootOrgId() {
        return sbRootOrgId;
    }

    public void setSbRootOrgId(String sbRootOrgId) {
        this.sbRootOrgId = sbRootOrgId;
    }

    public String getSbOrgType() {
        return sbOrgType;
    }

    public void setSbOrgType(String sbOrgType) {
        this.sbOrgType = sbOrgType;
    }

    public String getSbOrgSubType() {
        return sbOrgSubType;
    }

    public void setSbOrgSubType(String sbOrgSubType) {
        this.sbOrgSubType = sbOrgSubType;
    }

    public String getL1MapId() {
        return l1MapId;
    }

    public void setL1MapId(String l1MapId) {
        this.l1MapId = l1MapId;
    }

    public String getL2MapId() {
        return l2MapId;
    }

    public void setL2MapId(String l2MapId) {
        this.l2MapId = l2MapId;
    }

    public String getL1OrgName() {
        return l1OrgName;
    }

    public void setL1OrgName(String l1OrgName) {
        this.l1OrgName = l1OrgName;
    }

    public String getL2OrgName() {
        return l2OrgName;
    }

    public void setL2OrgName(String l2OrgName) {
        this.l2OrgName = l2OrgName;
    }

    public OrgHierarchy(@NotNull String orgName, @NotNull String channel,
            @NotNull String mapId, @NotNull String parentMapId) {
        this.orgName = orgName;
        this.channel = channel;
        this.mapId = mapId;
        this.parentMapId = parentMapId;
    }

    public OrgHierarchy() {
    }

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

package org.sunbird.portal.department.dto;


import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Arrays;

@Entity
@Table(name = "user_department_role_audit", schema = "wingspan")
public class UserDepartmentRoleAudit {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "user_id")
    @NotNull
    String userId;

    @Column(name = "dept_id")
    @NotNull
    Integer deptId;

    @Column(name = "role_ids")
    @NotNull
    @Type(type = "org.sunbird.portal.department.dto.GenericArrayUserType")
    Integer[] roleIds;

    @Column(name = "isactive")
    @NotNull
    Boolean isActive;

    @Column(name = "isblocked")
    @NotNull
    Boolean isBlocked;

    @Column(name = "created_by")
    @NotNull
    String createdBy;


    @Column(name = "created_time")
    long createdTime;

    public UserDepartmentRoleAudit() {
    }

    public UserDepartmentRoleAudit(@NotNull String userId, @NotNull Integer deptId, @NotNull Integer[] roleIds, @NotNull Boolean isActive, @NotNull Boolean isBlocked, @NotNull String createdBy) {
        this.userId = userId;
        this.deptId = deptId;
        this.roleIds = roleIds;
        this.isActive = isActive;
        this.isBlocked = isBlocked;
        this.createdBy = createdBy;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public Integer[] getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Integer[] roleIds) {
        this.roleIds = roleIds;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsBlocked() {
        return isBlocked;
    }

    public void setIsBlocked(Boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }


    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return "UserDepartmentRoleAudit{" +
                "userId='" + userId + '\'' +
                ", deptId=" + deptId +
                ", roleIds=" + Arrays.toString(roleIds) +
                ", isActive=" + isActive +
                ", isBlocked=" + isBlocked +
                ", createdBy='" + createdBy + '\'' +
                ", createdTime=" + createdTime +
                '}';
    }
}

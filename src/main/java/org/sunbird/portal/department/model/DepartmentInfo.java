package org.sunbird.portal.department.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentInfo implements Comparable<DepartmentInfo> {
	private Integer id;
	private String rootOrg;
	private String deptName;
	private Integer[] deptTypeIds;
	private List<DeptTypeInfo> deptTypeInfos;
	private String description;
	private long noOfUsers;
	private String headquarters;
	private String logo;
	private long creationDate;
	private String createdBy;
	@JsonProperty("active_users")
	private List<PortalUserInfo> activeUsers;
	@JsonProperty("inActive_users")
	private List<PortalUserInfo> inActiveUsers;
	@JsonProperty("blocked_users")
	private List<PortalUserInfo> blockedUsers;
	private List<String> currentUserRoles;
	private Integer sourceId;

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getRootOrg() {
		return rootOrg;
	}

	public void setRootOrg(String rootOrg) {
		this.rootOrg = rootOrg;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public Integer[] getDeptTypeIds() {
		return deptTypeIds;
	}

	public void setDeptTypeIds(Integer[] deptTypeIds) {
		this.deptTypeIds = deptTypeIds;
	}

	public List<DeptTypeInfo> getDeptTypeInfos() {
		return deptTypeInfos;
	}

	public void setDeptTypeInfos(List<DeptTypeInfo> deptTypeInfo) {
		this.deptTypeInfos = deptTypeInfo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getNoOfUsers() {
		return noOfUsers;
	}

	public void setNoOfUsers(long noOfUsers) {
		this.noOfUsers = noOfUsers;
	}

	public String getHeadquarters() {
		return headquarters;
	}

	public void setHeadquarters(String headquarters) {
		this.headquarters = headquarters;
	}

	public void addActiveUser(PortalUserInfo pUserInfo) {
		if (this.activeUsers == null) {
			this.activeUsers = new ArrayList<>();
		}
		this.activeUsers.add(pUserInfo);
	}

	public void addInActiveUser(PortalUserInfo pUserInfo) {
		if (this.inActiveUsers == null) {
			this.inActiveUsers = new ArrayList<>();
		}
		this.inActiveUsers.add(pUserInfo);
	}

	public void addBlockedUser(PortalUserInfo pUserInfo) {
		if (this.blockedUsers == null) {
			this.blockedUsers = new ArrayList<>();
		}
		this.blockedUsers.add(pUserInfo);
	}

	public List<String> getCurrentUserRoles() {
		return currentUserRoles;
	}

	public void setCurrentUserRoles(List<String> currentUserRoles) {
		this.currentUserRoles = currentUserRoles;
	}

	public long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(long created) {
		this.creationDate = created;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Integer getSourceId() {
		return sourceId;
	}

	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}

	public List<PortalUserInfo> getActiveUsers() {
		return activeUsers;
	}

	public void setActiveUsers(List<PortalUserInfo> activeUsers) {
		this.activeUsers = activeUsers;
	}

	public List<PortalUserInfo> getInActiveUsers() {
		return inActiveUsers;
	}

	public void setInActiveUsers(List<PortalUserInfo> inActiveUsers) {
		this.inActiveUsers = inActiveUsers;
	}

	public List<PortalUserInfo> getBlockedUsers() {
		return blockedUsers;
	}

	public void setBlockedUsers(List<PortalUserInfo> blockedUsers) {
		this.blockedUsers = blockedUsers;
	}

	public String toString() {
		StringBuilder str = new StringBuilder("DepartmentInfo:");
		str.append(" Id:").append(id);
		str.append(", RootOrg:").append(rootOrg);
		str.append(", DepartmentName: ").append(deptName);
		str.append(", DeptTypeId: ").append(deptTypeIds);
		str.append(", DeptTypeInfo: ").append(deptTypeInfos);
		str.append(", Description: ").append(description);
		str.append(", headquarters: ").append(headquarters);
		str.append(", noOfUsers: ").append(noOfUsers);
		str.append(", creationDate: ").append(creationDate);
		str.append(", createdBy: ").append(createdBy);
		if (sourceId != null) {
			str.append(", sourceId: ").append(sourceId);
		}
		str.append("]");

		return str.toString();
	}

	public int compareTo(DepartmentInfo o) {
		return o.getDeptName().compareTo(this.getDeptName());
	}
}

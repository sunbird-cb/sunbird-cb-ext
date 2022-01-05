package org.sunbird.portal.department.model;

public class DeptTypeInfo {
	private Integer id;
	private String deptType;
	private String deptSubType;
	private String description;

	public String getDeptSubType() {
		return deptSubType;
	}

	public String getDeptType() {
		return deptType;
	}

	public String getDescription() {
		return description;
	}

	public Integer getId() {
		return id;
	}

	public void setDeptSubType(String deptSubType) {
		this.deptSubType = deptSubType;
	}

	public void setDeptType(String deptType) {
		this.deptType = deptType;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("DeptTypeInfo: ");
		str.append(", Id: ").append(id);
		str.append(", DeptType: ").append(deptType);
		str.append(", deptSubType: ").append(deptSubType);
		str.append(", description: ").append(description);
		str.append("]");
		return str.toString();
	}
}

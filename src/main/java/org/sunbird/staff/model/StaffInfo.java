package org.sunbird.staff.model;

public class StaffInfo {

	private String position;
	private String orgId;
	private String id;
	private Integer totalPositionsFilled;
	private Integer totalPositionsVacant;

	public String getId() {
		return id;
	}

	public String getOrgId() {
		return orgId;
	}

	public String getPosition() {
		return position;
	}

	public Integer getTotalPositionsFilled() {
		return totalPositionsFilled;
	}

	public Integer getTotalPositionsVacant() {
		return totalPositionsVacant;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public void setTotalPositionsFilled(Integer totalPositionsFilled) {
		this.totalPositionsFilled = totalPositionsFilled;
	}

	public void setTotalPositionsVacant(Integer totalPositionsVacant) {
		this.totalPositionsVacant = totalPositionsVacant;
	}

}

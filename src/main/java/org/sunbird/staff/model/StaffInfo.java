package org.sunbird.staff.model;

public class StaffInfo {

	private String position;
	private String orgId;
	private String id;
	private Integer totalPositionsFilled;
	private Integer totalPositionsVacant;

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public Integer getTotalPositionsFilled() {
		return totalPositionsFilled;
	}

	public void setTotalPositionsFilled(Integer totalPositionsFilled) {
		this.totalPositionsFilled = totalPositionsFilled;
	}

	public Integer getTotalPositionsVacant() {
		return totalPositionsVacant;
	}

	public void setTotalPositionsVacant(Integer totalPositionsVacant) {
		this.totalPositionsVacant = totalPositionsVacant;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}

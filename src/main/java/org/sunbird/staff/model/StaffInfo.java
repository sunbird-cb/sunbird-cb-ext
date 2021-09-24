package org.sunbird.staff.model;

public class StaffInfo {
	
	private String position;
	private int totalPositionsFilled;
	private int totalPositionsVacant;
	private String orgId;
	private String id;
	
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public int getTotalPositionsFilled() {
		return totalPositionsFilled;
	}
	public void setTotalPositionsFilled(int totalPositionsFilled) {
		this.totalPositionsFilled = totalPositionsFilled;
	}
	public int getTotalPositionsVacant() {
		return totalPositionsVacant;
	}
	public void setTotalPositionsVacant(int totalPositionsVacant) {
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

package org.sunbird.staff.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.sunbird.common.util.Constants;

@Table("org_staff_position")
public class StaffInfoModel {

	@PrimaryKey
	private StaffInfoPrimaryKeyModel primaryKey;

	@Column(Constants.POSITION)
	private String position;

	@Column(Constants.TOTAL_POSITION_FILLED)
	private int totalPositionsFilled;

	@Column(Constants.TOTAL_POSITION_VACANT)
	private int totalPositionsVacant;

	public StaffInfoModel() {
		super();
	}

	public StaffInfoModel(StaffInfoPrimaryKeyModel primaryKey) {
		this.primaryKey = primaryKey;
	}

	public StaffInfoModel(StaffInfoPrimaryKeyModel primaryKey, String position, int totalPositionsFilled,
			int totalPositionsVacant) {
		this.primaryKey = primaryKey;
		this.position = position;
		this.totalPositionsFilled = totalPositionsFilled;
		this.totalPositionsVacant = totalPositionsVacant;
	}

	public StaffInfoPrimaryKeyModel getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(StaffInfoPrimaryKeyModel primaryKey) {
		this.primaryKey = primaryKey;
	}

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

	public StaffInfo getStaffInfo() {
		StaffInfo info = new StaffInfo();
		info.setId(primaryKey.getId());
		info.setOrgId(primaryKey.getOrgId());
		info.setPosition(position);
		info.setTotalPositionsFilled(totalPositionsFilled);
		info.setTotalPositionsVacant(totalPositionsVacant);
		return info;
	}

}

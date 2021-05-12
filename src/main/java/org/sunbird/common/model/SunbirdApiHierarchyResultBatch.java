package org.sunbird.common.model;

import java.util.List;

public class SunbirdApiHierarchyResultBatch {

	private int count;
	private List<String> participants;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<String> getParticipants() {
		return participants;
	}

	public void setParticipants(List<String> participants) {
		this.participants = participants;
	}
}

package org.sunbird.workallocation.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkAllocation {
	private String id;
	private String userId;
	private String userName;
	private String userEmail;
	private WAObject draftWAObject;
	private WAObject activeWAObject;
	private List<WAObject> archivedWAList;

	public void addArchivedWAList(WAObject archivedWAList) {
		if (CollectionUtils.isEmpty(this.archivedWAList)) {
			this.archivedWAList = new ArrayList<>();
		}
		this.archivedWAList.add(archivedWAList);
	}

	public WAObject getActiveWAObject() {
		return activeWAObject;
	}

	public List<WAObject> getArchivedWAList() {
		return archivedWAList;
	}

	public WAObject getDraftWAObject() {
		return draftWAObject;
	}

	public String getId() {
		return id;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public String getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setActiveWAObject(WAObject activeWAObject) {
		this.activeWAObject = activeWAObject;
	}

	public void setArchivedWAList(List<WAObject> archivedWAList) {
		this.archivedWAList = archivedWAList;
	}

	public void setDraftWAObject(WAObject draftWAObject) {
		this.draftWAObject = draftWAObject;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}

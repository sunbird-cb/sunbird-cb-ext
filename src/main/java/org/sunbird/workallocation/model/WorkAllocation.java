package org.sunbird.workallocation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkAllocation {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private WAObject draftWAObject;
    private WAObject activeWAObject;
    private List<WAObject> archivedWAList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public WAObject getDraftWAObject() {
        return draftWAObject;
    }

    public void setDraftWAObject(WAObject draftWAObject) {
        this.draftWAObject = draftWAObject;
    }

    public WAObject getActiveWAObject() {
        return activeWAObject;
    }

    public void setActiveWAObject(WAObject activeWAObject) {
        this.activeWAObject = activeWAObject;
    }

    public List<WAObject> getArchivedWAList() {
        return archivedWAList;
    }

    public void setArchivedWAList(List<WAObject> archivedWAList) {
        this.archivedWAList = archivedWAList;
    }
    public void addArchivedWAList(WAObject archivedWAList) {
        if(CollectionUtils.isEmpty(this.archivedWAList))
            this.archivedWAList = new ArrayList<>();
        this.archivedWAList.add(archivedWAList);
    }
}

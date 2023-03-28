package org.sunbird.searchby.model;

public class MasterData {
    private String id;
    private String contextType;
    private String contextName;
    private String contextData;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContextType() {
        return contextType;
    }

    public void setContextType(String contextType) {
        this.contextType = contextType;
    }

    public String getContextName() {
        return contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public String getContextData() {
        return contextData;
    }

    public void setContextData(String contextData) {
        this.contextData = contextData;
    }

    public MasterData() {
    }

    public MasterData(String id, String contextType, String contextName, String contextData) {
        this.id = id;
        this.contextType = contextType;
        this.contextName = contextName;
        this.contextData = contextData;
    }
}

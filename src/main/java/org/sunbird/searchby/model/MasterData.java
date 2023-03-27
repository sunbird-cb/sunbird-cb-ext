package org.sunbird.searchby.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MasterData {
    private String Id;
    private String contextType;
    @JsonProperty("contextname")
    private String contextName;
    @JsonProperty("contextdata")
    private String contextData;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
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
        Id = id;
        this.contextType = contextType;
        this.contextName = contextName;
        this.contextData = contextData;
    }
}

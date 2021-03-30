package org.sunbird.workallocation.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildNode {
    private String type;
    private String id;
    private String name;
    private String description;
    private String status;
    private String source;
    private String parentRole;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getParentRole() {
        return parentRole;
    }

    public void setParentRole(String parentRole) {
        this.parentRole = parentRole;
    }
    
    public Map<String, Object> getFracRequest(String source) {
		Map<String, Object> frac = new HashMap<String, Object>();
		frac.put("name", name);
		frac.put("type", "ROLE");
		frac.put("source", source);
		return frac;
	}
}

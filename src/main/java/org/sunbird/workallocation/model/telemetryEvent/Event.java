package org.sunbird.workallocation.model.telemetryEvent;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Event {
    private Actor actor;
    private String eid;
    private Map<String, Object> edata;
    private String ver;
    @JsonProperty("@timestamp")
    private String timestamp;
    private long ets;
    private Context context;
    private Flags flags;
    private String mid;
    private String type;
    private ObjectData object;

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }


    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public long getEts() {
        return ets;
    }

    public void setEts(long ets) {
        this.ets = ets;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Flags getFlags() {
        return flags;
    }

    public void setFlags(Flags flags) {
        this.flags = flags;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ObjectData getObject() {
        return object;
    }

    public void setObject(ObjectData object) {
        this.object = object;
    }

    public Map<String, Object> getEdata() {
        return edata;
    }

    public void setEdata(Map<String, Object> edata) {
        this.edata = edata;
    }
}

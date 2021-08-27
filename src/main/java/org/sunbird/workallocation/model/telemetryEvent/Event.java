package org.sunbird.workallocation.model.telemetryEvent;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Event {
    private Actor actor;
    private String eid;
    private Map<String, Object> edata;
    private String ver;
    private long ets;
    private Context context;
    private String mid;
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


    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
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

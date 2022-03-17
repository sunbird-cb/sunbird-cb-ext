package org.sunbird.telemetry.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"eid",
"ets",
"ver",
"mid",
"actor",
"context",
"object",
"edata"
})

public class AuditEvents {

@JsonProperty("eid")
private String eid;
@JsonProperty("ets")
private long ets;
@JsonProperty("ver")
private String ver;
@JsonProperty("mid")
private String mid;
@JsonProperty("actor")
private Actor actor;
@JsonProperty("context")
private Context context;
@JsonProperty("object")
private Object object;
@JsonProperty("edata")
private Edata edata;

/**
* No args constructor for use in serialization
*
*/
public AuditEvents() {
}

/**
*
* @param actor
* @param eid
* @param edata
* @param ver
* @param ets
* @param context
* @param mid
* @param object
*/
public AuditEvents(String eid, long ets, String ver, String mid, Actor actor, Context context, Object object, Edata edata) {
super();
this.eid = eid;
this.ets = ets;
this.ver = ver;
this.mid = mid;
this.actor = actor;
this.context = context;
this.object = object;
this.edata = edata;
}

@JsonProperty("eid")
public String getEid() {
return eid;
}

@JsonProperty("eid")
public void setEid(String eid) {
this.eid = eid;
}

@JsonProperty("ets")
public long getEts() {
return ets;
}

@JsonProperty("ets")
public void setEts(long ets) {
this.ets = ets;
}

@JsonProperty("ver")
public String getVer() {
return ver;
}

@JsonProperty("ver")
public void setVer(String ver) {
this.ver = ver;
}

@JsonProperty("mid")
public String getMid() {
return mid;
}

@JsonProperty("mid")
public void setMid(String mid) {
this.mid = mid;
}

@JsonProperty("actor")
public Actor getActor() {
return actor;
}

@JsonProperty("actor")
public void setActor(Actor actor) {
this.actor = actor;
}

@JsonProperty("context")
public Context getContext() {
return context;
}

@JsonProperty("context")
public void setContext(Context context) {
this.context = context;
}

@JsonProperty("object")
public Object getObject() {
return object;
}

@JsonProperty("object")
public void setObject(Object object) {
this.object = object;
}

@JsonProperty("edata")
public Edata getEdata() {
return edata;
}

@JsonProperty("edata")
public void setEdata(Edata edata) {
this.edata = edata;
}

}
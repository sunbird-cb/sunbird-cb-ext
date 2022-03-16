package org.sunbird.telemetry.model;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"id",
"pid",
"ver"
})

public class Pdata {

@JsonProperty("id")
private String id;
@JsonProperty("pid")
private String pid;
@JsonProperty("ver")
private String ver;

/**
* No args constructor for use in serialization
*
*/
public Pdata() {
}

/**
*
* @param ver
* @param pid
* @param id
*/
public Pdata(String id, String pid, String ver) {
super();
this.id = id;
this.pid = pid;
this.ver = ver;
}

@JsonProperty("id")
public String getId() {
return id;
}

@JsonProperty("id")
public void setId(String id) {
this.id = id;
}

@JsonProperty("pid")
public String getPid() {
return pid;
}

@JsonProperty("pid")
public void setPid(String pid) {
this.pid = pid;
}

@JsonProperty("ver")
public String getVer() {
return ver;
}

@JsonProperty("ver")
public void setVer(String ver) {
this.ver = ver;
}

}
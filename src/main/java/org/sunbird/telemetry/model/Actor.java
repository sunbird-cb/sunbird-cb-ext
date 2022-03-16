package org.sunbird.telemetry.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"id",
"type"
})

public class Actor {

@JsonProperty("id")
private String id;
@JsonProperty("type")
private String type;

/**
* No args constructor for use in serialization
*
*/
public Actor() {
}

/**
*
* @param id
* @param type
*/
public Actor(String id, String type) {
super();
this.id = id;
this.type = type;
}

@JsonProperty("id")
public String getId() {
return id;
}

@JsonProperty("id")
public void setId(String id) {
this.id = id;
}

@JsonProperty("type")
public String getType() {
return type;
}

@JsonProperty("type")
public void setType(String type) {
this.type = type;
}

} 
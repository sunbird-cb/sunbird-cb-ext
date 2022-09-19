
package org.sunbird.scheduler.model;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"lastUpdatedOn"
})
@Generated("jsonschema2pojo")
public class SortBy {

@JsonProperty("lastUpdatedOn")
private String lastUpdatedOn;

@JsonProperty("lastUpdatedOn")
public String getLastUpdatedOn() {
return lastUpdatedOn;
}

@JsonProperty("lastUpdatedOn")
public void setLastUpdatedOn(String lastUpdatedOn) {
this.lastUpdatedOn = lastUpdatedOn;
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append(SortBy.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("lastUpdatedOn");
sb.append('=');
sb.append(((this.lastUpdatedOn == null)?"<null>":this.lastUpdatedOn));
sb.append(',');
if (sb.charAt((sb.length()- 1)) == ',') {
sb.setCharAt((sb.length()- 1), ']');
} else {
sb.append(']');
}
return sb.toString();
}

}
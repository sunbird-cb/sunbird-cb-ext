
package org.sunbird.scheduler.model;

import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"count",
"content"
})
@Generated("jsonschema2pojo")
public class Result {

@JsonProperty("count")
private Integer count;
@JsonProperty("content")
private List<Content> content = null;

@JsonProperty("count")
public Integer getCount() {
return count;
}

@JsonProperty("count")
public void setCount(Integer count) {
this.count = count;
}

@JsonProperty("content")
public List<Content> getContent() {
return content;
}

@JsonProperty("content")
public void setContent(List<Content> content) {
this.content = content;
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append(Result.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("count");
sb.append('=');
sb.append(((this.count == null)?"<null>":this.count));
sb.append(',');
sb.append("content");
sb.append('=');
sb.append(((this.content == null)?"<null>":this.content));
sb.append(',');
if (sb.charAt((sb.length()- 1)) == ',') {
sb.setCharAt((sb.length()- 1), ']');
} else {
sb.append(']');
}
return sb.toString();
}

}
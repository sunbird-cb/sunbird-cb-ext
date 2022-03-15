
package org.sunbird.assessment.model;

import java.io.Serializable;

import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "enabled",
    "autoBatch"
})

public class Trackable extends JdkSerializationRedisSerializer implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("enabled")
    private String enabled;
    @JsonProperty("autoBatch")
    private String autoBatch;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Trackable() {
    }

    /**
     * 
     * @param enabled
     * @param autoBatch
     */
    public Trackable(String enabled, String autoBatch) {
        super();
        this.enabled = enabled;
        this.autoBatch = autoBatch;
    }

    @JsonProperty("enabled")
    public String getEnabled() {
        return enabled;
    }

    @JsonProperty("enabled")
    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public Trackable withEnabled(String enabled) {
        this.enabled = enabled;
        return this;
    }

    @JsonProperty("autoBatch")
    public String getAutoBatch() {
        return autoBatch;
    }

    @JsonProperty("autoBatch")
    public void setAutoBatch(String autoBatch) {
        this.autoBatch = autoBatch;
    }

    public Trackable withAutoBatch(String autoBatch) {
        this.autoBatch = autoBatch;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Trackable.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null)?"<null>":this.enabled));
        sb.append(',');
        sb.append("autoBatch");
        sb.append('=');
        sb.append(((this.autoBatch == null)?"<null>":this.autoBatch));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}

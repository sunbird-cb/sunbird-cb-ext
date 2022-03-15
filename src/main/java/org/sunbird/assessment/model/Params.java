
package org.sunbird.assessment.model;

import java.io.Serializable;

import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "resmsgid",
    "msgid",
    "err",
    "status",
    "errmsg"
})

public class Params extends JdkSerializationRedisSerializer implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("resmsgid")
    private String resmsgid;
    @JsonProperty("msgid")
    private Object msgid;
    @JsonProperty("err")
    private Object err;
    @JsonProperty("status")
    private String status;
    @JsonProperty("errmsg")
    private Object errmsg;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Params() {
    }

    /**
     * 
     * @param resmsgid
     * @param err
     * @param msgid
     * @param errmsg
     * @param status
     */
    public Params(String resmsgid, Object msgid, Object err, String status, Object errmsg) {
        super();
        this.resmsgid = resmsgid;
        this.msgid = msgid;
        this.err = err;
        this.status = status;
        this.errmsg = errmsg;
    }

    @JsonProperty("resmsgid")
    public String getResmsgid() {
        return resmsgid;
    }

    @JsonProperty("resmsgid")
    public void setResmsgid(String resmsgid) {
        this.resmsgid = resmsgid;
    }

    public Params withResmsgid(String resmsgid) {
        this.resmsgid = resmsgid;
        return this;
    }

    @JsonProperty("msgid")
    public Object getMsgid() {
        return msgid;
    }

    @JsonProperty("msgid")
    public void setMsgid(Object msgid) {
        this.msgid = msgid;
    }

    public Params withMsgid(Object msgid) {
        this.msgid = msgid;
        return this;
    }

    @JsonProperty("err")
    public Object getErr() {
        return err;
    }

    @JsonProperty("err")
    public void setErr(Object err) {
        this.err = err;
    }

    public Params withErr(Object err) {
        this.err = err;
        return this;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    public Params withStatus(String status) {
        this.status = status;
        return this;
    }

    @JsonProperty("errmsg")
    public Object getErrmsg() {
        return errmsg;
    }

    @JsonProperty("errmsg")
    public void setErrmsg(Object errmsg) {
        this.errmsg = errmsg;
    }

    public Params withErrmsg(Object errmsg) {
        this.errmsg = errmsg;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Params.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("resmsgid");
        sb.append('=');
        sb.append(((this.resmsgid == null)?"<null>":this.resmsgid));
        sb.append(',');
        sb.append("msgid");
        sb.append('=');
        sb.append(((this.msgid == null)?"<null>":this.msgid));
        sb.append(',');
        sb.append("err");
        sb.append('=');
        sb.append(((this.err == null)?"<null>":this.err));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null)?"<null>":this.status));
        sb.append(',');
        sb.append("errmsg");
        sb.append('=');
        sb.append(((this.errmsg == null)?"<null>":this.errmsg));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}

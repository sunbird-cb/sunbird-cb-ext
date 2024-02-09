package org.sunbird.course.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.sunbird.common.util.Constants;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "resmsgid", "msgid", "status", "err", "errmsg" })
public class Params {

	@JsonProperty("resmsgid")
	private String resmsgid;
	@JsonProperty("msgid")
	private String msgid;
	@JsonProperty("status")
	private String status;
	@JsonProperty("err")
	private Object err;
	@JsonProperty("errmsg")
	private Object errmsg;

	@JsonProperty("resmsgid")
	public String getResmsgid() {
		return resmsgid;
	}

	@JsonProperty("resmsgid")
	public void setResmsgid(String resmsgid) {
		this.resmsgid = resmsgid;
	}

	@JsonProperty("msgid")
	public String getMsgid() {
		return msgid;
	}

	@JsonProperty("msgid")
	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}

	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}

	@JsonProperty("err")
	public Object getErr() {
		return err;
	}

	@JsonProperty("err")
	public void setErr(Object err) {
		this.err = err;
	}

	@JsonProperty("errmsg")
	public Object getErrmsg() {
		return errmsg;
	}

	@JsonProperty("errmsg")
	public void setErrmsg(Object errmsg) {
		this.errmsg = errmsg;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Params.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this)))
				.append('[');
		sb.append("resmsgid");
		sb.append('=');
		sb.append(((this.resmsgid == null) ? Constants.NULL_CONSTANT : this.resmsgid));
		sb.append(',');
		sb.append("msgid");
		sb.append('=');
		sb.append(((this.msgid == null) ? Constants.NULL_CONSTANT : this.msgid));
		sb.append(',');
		sb.append("status");
		sb.append('=');
		sb.append(((this.status == null) ? Constants.NULL_CONSTANT : this.status));
		sb.append(',');
		sb.append("err");
		sb.append('=');
		sb.append(((this.err == null) ? Constants.NULL_CONSTANT : this.err));
		sb.append(',');
		sb.append("errmsg");
		sb.append('=');
		sb.append(((this.errmsg == null) ? Constants.NULL_CONSTANT : this.errmsg));
		sb.append(',');
		if (sb.charAt((sb.length() - 1)) == ',') {
			sb.setCharAt((sb.length() - 1), ']');
		} else {
			sb.append(']');
		}
		return sb.toString();
	}

}
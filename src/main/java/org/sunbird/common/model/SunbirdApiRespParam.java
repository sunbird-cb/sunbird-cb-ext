package org.sunbird.common.model;

public class SunbirdApiRespParam {

	private String resmsgid;
	private String msgid;
	private String err;
	private String status;
	private String errmsg;

	public String getErr() {
		return err;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public String getMsgid() {
		return msgid;
	}

	public String getResmsgid() {
		return resmsgid;
	}

	public String getStatus() {
		return status;
	}

	public void setErr(String err) {
		this.err = err;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}

	public void setResmsgid(String resmsgid) {
		this.resmsgid = resmsgid;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}

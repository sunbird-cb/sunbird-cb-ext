package org.sunbird.catalog.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FrameworkResponseParams {
	private String status;
	private String err;
	private String errmsg;

	public String getErr() {
		return err;
	}

	public String getErrmsg() {
		return errmsg;
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

	public void setStatus(String status) {
		this.status = status;
	}

}

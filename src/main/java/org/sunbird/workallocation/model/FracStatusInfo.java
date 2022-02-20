package org.sunbird.workallocation.model;

public class FracStatusInfo {
	private int statusCode;
	private String statusMessage;
	private String errorMessage;

	public String getErrorMessage() {
		return errorMessage;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

}

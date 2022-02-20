package org.sunbird.exception;

public class MyOwnRuntimeException extends Exception {

	private static final long serialVersionUID = 1L;
	private String message = "";

	/**
	 * @param code the code to set
	 */
	@Override
	public String getMessage() {
		return message;
	}

	/**
	 *
	 * @param message
	 */
	public MyOwnRuntimeException(String message) {
		this.message = message;
	}

}

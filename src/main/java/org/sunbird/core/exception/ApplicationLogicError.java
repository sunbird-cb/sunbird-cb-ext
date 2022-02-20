package org.sunbird.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
@ResponseBody
public class ApplicationLogicError extends RuntimeException {

	/**
		 *
		 */
	private static final long serialVersionUID = 1L;
	private final String message;

	public ApplicationLogicError(String message) {
		this.message = message;
	}

	public ApplicationLogicError(String message, Throwable e) {
		super(message, e);
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

}

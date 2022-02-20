package org.sunbird.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@ResponseBody
public class InvalidDataInputException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private String code;
	private transient Object[] params;

	public InvalidDataInputException(String code) {
		super(code);
		this.code = code;
	}

	public InvalidDataInputException(String code, Object[] params) {
		this.code = code;
		this.params = params;
	}

	public InvalidDataInputException(String code, Throwable e) {
		super(code, e);
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public Object[] getParams() {
		return params;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

}
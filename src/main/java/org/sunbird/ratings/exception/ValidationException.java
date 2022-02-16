package org.sunbird.ratings.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@ResponseBody
public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String codeMessage = "Validation Exception occurred with these details :";
    /**
     * message String ResponseCode.
     */
    private List<String> message;
    /**
     * responseCode int ResponseCode.
     */
    private int responseCode;

    public void setMessage(List<String> message) {
        this.message = message;
    }

    public String getCode() {
        return codeMessage;
    }

    public void setCode(String code) {
        this.codeMessage = code;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message.toString();
    }


    public ValidationException(List<String> message) {
        this.message = message;
    }

    public ValidationException(List<String> message, int responseCode) {
        super();
        this.message = message;
        this.responseCode = responseCode;
    }

    @Override
    public String toString() {
        return "ValidationException{" +
                "code='" + codeMessage + '\'' +
                ", message='" + message + '\'' +
                ", responseCode=" + responseCode +
                '}';
    }
}

package org.sunbird.ratings.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@ResponseBody
public class ValidateRatingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    @Override
    public String getMessage() {
        return message;
    }

    private final String message;

    public ValidateRatingException(String message) {
        super(message);
        this.message = message;
    }
}

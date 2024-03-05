package org.sunbird.profile.exception;

/**
 * @author mahesh.vakkund
 */
public class ProfileRequestException extends Exception {
    private static final long serialVersionUID = 1L;

    public ProfileRequestException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    private final String message;


}

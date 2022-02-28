package org.sunbird.ratings.responsecode;


public interface ResponseMessage {

    interface Message {

        String INVALID_RATING  = "Rating must be between 1 and 5.";
        String INVALID_REVIEW  = "Review must contain only alphanumeric string.";
        String INVALID_INPUT   = "Field must not be empty.";
        String SUCCESS_MESSAGE = "Success.";
        String INVALID_LIMIT   = "Limit must be greater than 1";
        String INVALID_USER    = "user is invalid.";

    }

    interface Key {
        String INVALID_RATING  = "INVALID_RATING_RANGE";
        String INVALID_REVIEW  = "INVALID_REVIEW";
        String INVALID_INPUT   = "INVALID_INPUT";
        String SUCCESS_MESSAGE = "SUCCESS_MESSAGE";
        String INVALID_LIMIT   = "INVALID_LIMIT";
        String INVALID_USER    = "INVALID_USER";
    }
}
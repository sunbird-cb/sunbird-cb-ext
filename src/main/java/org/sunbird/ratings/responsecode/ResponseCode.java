package org.sunbird.ratings.responsecode;

import org.apache.commons.lang3.StringUtils;

public enum ResponseCode {
    invalidRating(ResponseMessage.Key.INVALID_RATING, ResponseMessage.Message.INVALID_RATING),
    invalidReview(
            ResponseMessage.Key.INVALID_REVIEW, ResponseMessage.Message.INVALID_REVIEW),
    invalidInput(
            ResponseMessage.Key.INVALID_INPUT, ResponseMessage.Message.INVALID_INPUT),
    invalidUser(
            ResponseMessage.Key.INVALID_INPUT, ResponseMessage.Message.INVALID_INPUT),

    success(ResponseMessage.Key.SUCCESS_MESSAGE, ResponseMessage.Message.SUCCESS_MESSAGE),

    OK(200),
    UPDATED(201),
    BAD_REQUEST(400),
    SERVER_ERROR(500),
    RESOURCE_NOT_FOUND(404),
    FORBIDDEN(403),
    SERVICE_UNAVAILABLE(503),
    PARTIAL_SUCCESS_RESPONSE(206);

    private int responseCode;
    /**
     * error code contains String value
     */
    private String errorCode;
    /**
     * errorMessage contains proper error message.
     */
    private String errorMessage;

    private ResponseCode(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    private ResponseCode(String errorCode, String errorMessage, int responseCode) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.responseCode = responseCode;
    }

    /**
     * @param errorCode
     * @return
     */
    public String getMessage(int errorCode) {
        return "";
    }

    /**
     * @return
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @return
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * This method will provide status message based on code
     *
     * @param code
     * @return String
     */
    public static String getResponseMessage(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        ResponseCode responseCodes[] = ResponseCode.values();
        for (ResponseCode actionState : responseCodes) {
            if (actionState.getErrorCode().equals(code)) {
                return actionState.getErrorMessage();
            }
        }
        return "";
    }

    private ResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }


    /**
     * This method will provide ResponseCode enum based on error code
     *
     * @param errorCode
     * @return String
     */
    public static ResponseCode getResponse(String errorCode) {
        if (StringUtils.isBlank(errorCode)) {
            return null;
        } else {
           // ResponseCode value = null;
            ResponseCode responseCodes[] = ResponseCode.values();
            for (ResponseCode response : responseCodes) {
                if (response.getErrorCode().equals(errorCode)) {
                    return response;
                }
            }
            return null;
        }
    }
}

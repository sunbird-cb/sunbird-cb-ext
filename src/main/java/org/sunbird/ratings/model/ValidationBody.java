package org.sunbird.ratings.model;

public class ValidationBody {
    private RequestRating requestRating;
    private LookupRequest lookupRequest;
    private String activityId = "";
    private String activityType = "";
    private String userId = "";

    public RequestRating getRequestRating() {
        return requestRating;
    }

    public void setRequestRating(RequestRating requestRating) {
        this.requestRating = requestRating;
    }

    public LookupRequest getLookupRequest() {
        return lookupRequest;
    }

    public void setLookupRequest(LookupRequest lookupRequest) {
        this.lookupRequest = lookupRequest;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

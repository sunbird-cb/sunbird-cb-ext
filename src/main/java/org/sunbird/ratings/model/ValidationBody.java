package org.sunbird.ratings.model;

public class ValidationBody {
    private RequestRating requestRating;
    private LookupRequest lookupRequest;
    private String activity_Id = "";
    private String activity_Type = "";
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

    public String getActivity_Id() {
        return activity_Id;
    }

    public void setActivity_Id(String activity_Id) {
        this.activity_Id = activity_Id;
    }

    public String getActivity_Type() {
        return activity_Type;
    }

    public void setActivity_Type(String activity_Type) {
        this.activity_Type = activity_Type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

package org.sunbird.ratings.model;

public class ValidationBody {
    private RequestRating requestRating;
    private LookupRequest lookupRequest;
    private String activity_id = "";
    private String activity_type = "";
    private String user_id = "";

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

    public String getActivity_id() {
        return activity_id;
    }

    public void setActivity_id(String activity_id) {
        this.activity_id = activity_id;
    }

    public String getActivity_type() {
        return activity_type;
    }

    public void setActivity_type(String activity_type) {
        this.activity_type = activity_type;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}

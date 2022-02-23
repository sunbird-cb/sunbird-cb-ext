package org.sunbird.ratings.model;

public class LookupRequest {

    private String activity_Id;
    private String activity_Type;
    private Float rating;
    private int limit;
    private String updateOn;

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

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getUpdateOn() {
        return updateOn;
    }

    public void setUpdateOn(String updateOn) {
        this.updateOn = updateOn;
    }
}

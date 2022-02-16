package org.sunbird.ratings.model;

public class RequestRating {
    String activity_Id;
    String userId;
    String activity_type;
    Float rating;
    String review ;

    public String getActivity_Id() {
        return activity_Id;
    }

    public void setActivity_Id(String activity_Id) {
        this.activity_Id = activity_Id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActivity_type() {
        return activity_type;
    }

    public void setActivity_type(String activity_type) {
        this.activity_type = activity_type;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }



}

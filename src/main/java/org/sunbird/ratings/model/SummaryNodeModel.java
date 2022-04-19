package org.sunbird.ratings.model;

import java.util.UUID;

public class SummaryNodeModel {
    private String objectType;
    private String user_id;
    private String date;
    private Double rating;
    private String review;

    public SummaryNodeModel() {

    }
    public SummaryNodeModel(String objectType, String user_id, String date, Double rating, String review) {
        this.objectType = objectType;
        this.user_id = user_id;
        this.date = date;
        this.rating = rating;
        this.review = review;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}

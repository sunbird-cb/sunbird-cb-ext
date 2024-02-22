package org.sunbird.ratings.model;

public class SummaryNodeModel {
    private String objectType;
    private String userId;
    private String date;
    private Double rating;
    private String review;

    public SummaryNodeModel() {

    }
    public SummaryNodeModel(String objectType, String userId, String date, Double rating, String review) {
        this.objectType = objectType;
        this.userId = userId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

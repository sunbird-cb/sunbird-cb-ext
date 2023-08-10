package org.sunbird.ratings.model;

import java.util.UUID;

public class LookupDataModel {
    private String activityId;
    private String review;
    private Float rating;
    private String updatedOn;
    private String activityType;
    private String userId;

    public LookupDataModel() {

    }

    public LookupDataModel(String activityId, String review, Float rating, UUID updatedOn, String activityType, String userId) {
        this.activityId = activityId;
        this.review = review;
        this.rating = rating;
        this.updatedOn = updatedOn.toString();
        this.activityType = activityType;
        this.userId = userId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }


    public String getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedon(UUID updatedOn) {
        this.updatedOn = updatedOn.toString();
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivitytype(String activityType) {
        this.activityType = activityType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "LookupDataModel{" +
                "activityId='" + activityId + '\'' +
                ", review='" + review + '\'' +
                ", rating=" + rating +
                ", updatedOn='" + updatedOn + '\'' +
                ", activityType='" + activityType + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}

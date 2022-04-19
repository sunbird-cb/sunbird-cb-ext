package org.sunbird.ratings.model;

public class LookupResponse {
    private String activityId;
    private String review;
    private String rating;
    private String updatedon;
    private String activityType;
    private String userId;
    private String firstName;
    private String lastName;

    public LookupResponse(String activityId, String review, String rating, String updatedon, String activityType, String userId, String firstName, String lastName) {
        this.activityId = activityId;
        this.review = review;
        this.rating = rating;
        this.updatedon = updatedon;
        this.activityType = activityType;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
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

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(String updatedon) {
        this.updatedon = updatedon;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}

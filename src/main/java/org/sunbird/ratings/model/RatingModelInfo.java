package org.sunbird.ratings.model;

public class RatingModelInfo {
    private String activityId;
    private String commentUpdatedOn;
    private String commentBy;
    private String review;
    private Float rating;
    private String comment;
    private String updatedOn;
    private String activityType;
    private String userId;
    private String createdOn;

    public RatingModelInfo() {
    }

    public RatingModelInfo(String activityId, String commentUpdatedOn, String commentBy, String review, Float rating, String comment, String updatedOn, String activityType, String userId, String createdOn) {
        this.activityId = activityId;
        this.commentUpdatedOn = commentUpdatedOn;
        this.commentBy = commentBy;
        this.review = review;
        this.rating = rating;
        this.comment = comment;
        this.updatedOn = updatedOn;
        this.activityType = activityType;
        this.userId = userId;
        this.createdOn = createdOn;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getCommentUpdatedOn() {
        return commentUpdatedOn;
    }

    public void setCommentUpdatedOn(String commentUpdatedOn) {
        this.commentUpdatedOn = commentUpdatedOn;
    }

    public String getCommentBy() {
        return commentBy;
    }

    public void setCommentBy(String commentBy) {
        this.commentBy = commentBy;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
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

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }
}

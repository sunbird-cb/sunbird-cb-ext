package org.sunbird.ratings.model;

import java.sql.Timestamp;

public class RatingModelInfo {
    private String activityId;
    private Timestamp commentUpdatedOn;
    private String commentBy;
    private String review;
    private Float rating;
    private String comment;
    private Timestamp updatedOn;
    private String activityType;
    private String userId;
    private Timestamp createdOn;

    public RatingModelInfo() {
    }

    public RatingModelInfo(String activityId, Timestamp commentUpdatedOn, String commentBy, String review, Float rating, String comment, Timestamp updatedOn, String activityType, String userId, Timestamp createdOn) {
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

    public Timestamp getCommentUpdatedOn() {
        return commentUpdatedOn;
    }

    public void setCommentUpdatedOn(Timestamp commentUpdatedOn) {
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

    public Timestamp getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Timestamp updatedOn) {
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

    public Timestamp getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }
}

package org.sunbird.ratings.model;

public class RatingMessage {
    private String action = "ratingUpdate";
    private String activityId;
    private String activityType;
    private String userId;
    private String createdDate;
    private UpdatedValues prevValues;
    private UpdatedValues updatedValues;

    public RatingMessage(String action, String activityId, String activityType, String userId, String createdDate) {
        this.action = action;
        this.activityId = activityId;
        this.activityType = activityType;
        this.userId = userId;
        this.createdDate = createdDate;
    }


    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public UpdatedValues getPrevValues() {
        return prevValues;
    }

    public void setPrevValues(UpdatedValues prevValues) {
        this.prevValues = prevValues;
    }

    public UpdatedValues getUpdatedValues() {
        return updatedValues;
    }

    public void setUpdatedValues(UpdatedValues updatedValues) {
        this.updatedValues = updatedValues;
    }

    public static class UpdatedValues {
        private String updatedOn;
        private Float rating;
        private String review;

        public String getUpdatedOn() {
            return updatedOn;
        }

        public void setUpdatedOn(String updatedOn) {
            this.updatedOn = updatedOn;
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

}



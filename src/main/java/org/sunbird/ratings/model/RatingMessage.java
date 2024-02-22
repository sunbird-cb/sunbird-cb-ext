package org.sunbird.ratings.model;

public class RatingMessage {
    public Integer version = 1;
    private String action = "ratingUpdate";
    private String activityId;
    private String activityType;
    private String userId;
    private String createdDate;
    private UpdatedValues prevValues;
    private UpdatedValues updatedValues;

    public RatingMessage(String action, String activity_id, String activity_Type, String user_id, String created_Date) {
        this.action = action;
        this.activityId = activity_id;
        this.activityType = activity_Type;
        this.userId = user_id;
        this.createdDate = created_Date;
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
        public String updatedOn;
        public Float rating;
        public String review;

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



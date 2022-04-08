package org.sunbird.ratings.model;

public class RatingMessage {
    public Integer version = 1;
    private String action = "ratingUpdate";
    private String activity_id;
    private String activity_Type;
    private String user_id;
    private String created_Date;
    private UpdatedValues prevValues;
    private UpdatedValues updatedValues;

    public RatingMessage(String action, String activity_id, String activity_Type, String user_id, String created_Date) {
        this.action = action;
        this.activity_id = activity_id;
        this.activity_Type = activity_Type;
        this.user_id = user_id;
        this.created_Date = created_Date;
    }


    public String getActivity_id() {
        return activity_id;
    }

    public void setActivity_id(String activity_id) {
        this.activity_id = activity_id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActivity_Type() {
        return activity_Type;
    }

    public void setActivity_Type(String activity_Type) {
        this.activity_Type = activity_Type;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCreated_Date() {
        return created_Date;
    }

    public void setCreated_Date(String created_Date) {
        this.created_Date = created_Date;
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



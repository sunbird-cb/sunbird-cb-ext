package org.sunbird.ratings.model;

import java.util.UUID;

public class LookupDataModel {
    private String activityid;
    private String review;
    private Float rating;
    private String updatedon;
    private String activitytype;
    private String userId;

    public LookupDataModel() {

    }

    public LookupDataModel(String activityid, String review, Float rating, UUID updatedon, String activitytype, String userId) {
        this.activityid = activityid;
        this.review = review;
        this.rating = rating;
        this.updatedon = updatedon.toString();
        this.activitytype = activitytype;
        this.userId = userId;
    }

    public String getActivityid() {
        return activityid;
    }

    public void setActivityid(String activityid) {
        this.activityid = activityid;
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


    public String getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(UUID updatedon) {
        this.updatedon = updatedon.toString();
    }

    public String getActivitytype() {
        return activitytype;
    }

    public void setActivitytype(String activitytype) {
        this.activitytype = activitytype;
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
                "activityid='" + activityid + '\'' +
                ", review='" + review + '\'' +
                ", rating=" + rating +
                ", updatedon='" + updatedon + '\'' +
                ", activitytype='" + activitytype + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}

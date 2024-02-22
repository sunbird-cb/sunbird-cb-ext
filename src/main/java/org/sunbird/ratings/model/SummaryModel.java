package org.sunbird.ratings.model;

import java.sql.Timestamp;

public class SummaryModel {
    private String activityId;
    private String activityType;
    private Float totalCount1Stars;
    private Float totalCount2Stars;
    private Float totalCount3Stars;
    private Float totalCount4Stars;
    private Float totalCount5Stars;
    private Float totalNumberOfRatings;
    private Float sumOfTotalRatings;
    private String latest50Reviews;



    public SummaryModel(String activityId, String activityType, Float totalCount1Stars, Float totalCount2Stars, Float totalCount3Stars, Float totalCount4Stars, Float totalCount5Stars, Float totalNumberOfRatings, Float sumOfTotalRatings, String latest50Reviews) {
        this.activityId = activityId;
        this.activityType = activityType;
        this.totalCount1Stars = totalCount1Stars;
        this.totalCount2Stars = totalCount2Stars;
        this.totalCount3Stars = totalCount3Stars;
        this.totalCount4Stars = totalCount4Stars;
        this.totalCount5Stars = totalCount5Stars;
        this.totalNumberOfRatings = totalNumberOfRatings;
        this.sumOfTotalRatings = sumOfTotalRatings;
        this.latest50Reviews = latest50Reviews;
    }

    public SummaryModel() {

    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public Float getTotalCount3Stars() {
        return totalCount3Stars;
    }

    public void setTotalCount3Stars(Float totalCount3Stars) {
        this.totalCount3Stars = totalCount3Stars;
    }

    public Float getTotalCount1Stars() {
        return totalCount1Stars;
    }

    public void setTotalCount1Stars(Float totalCount1Stars) {
        this.totalCount1Stars = totalCount1Stars;
    }

    public Float getTotalCount4Stars() {
        return totalCount4Stars;
    }

    public void setTotalCount4Stars(Float totalCount4Stars) {
        this.totalCount4Stars = totalCount4Stars;
    }

    public Float getTotalCount5Stars() {
        return totalCount5Stars;
    }

    public void setTotalCount5Stars(Float totalCount5Stars) {
        this.totalCount5Stars = totalCount5Stars;
    }

    public Float getTotalNumberOfRatings() {
        return totalNumberOfRatings;
    }

    public void setTotalNumberOfRatings(Float totalNumberOfRatings) {
        this.totalNumberOfRatings = totalNumberOfRatings;
    }

    public Float getTotalCount2Stars() {
        return totalCount2Stars;
    }

    public void setTotalCount2Stars(Float totalCount2Stars) {
        this.totalCount2Stars = totalCount2Stars;
    }

    public Float getSumOfTotalRatings() {
        return sumOfTotalRatings;
    }

    public void setSumOfTotalRatings(Float sumOfTotalRatings) {
        this.sumOfTotalRatings = sumOfTotalRatings;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getLatest50Reviews() {
        return latest50Reviews;
    }

    public void setLatest50Reviews(String latest50Reviews) {
        this.latest50Reviews = latest50Reviews;
    }

    static public class latestReviews {
        private String objectType;
        private String userId;
        private Timestamp date;
        private Float rating;
        private String review;
        private String firstName;

        public latestReviews(String objectType, String userId, Timestamp date, Float rating, String review, String firstName) {
            this.objectType = objectType;
            this.userId = userId;
            this.date = date;
            this.rating = rating;
            this.review = review;
            this.firstName = firstName;
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

        public Timestamp getDate() {
            return date;
        }

        public void setDate(Timestamp date) {
            this.date = date;
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

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
    }
}

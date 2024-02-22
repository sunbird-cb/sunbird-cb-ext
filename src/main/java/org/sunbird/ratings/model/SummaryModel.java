package org.sunbird.ratings.model;

import java.sql.Timestamp;

public class SummaryModel {
    private String activityId;
    private String activityType;
    private Float totalcount1stars;
    private Float totalcount2stars;
    private Float totalcount3stars;
    private Float totalcount4stars;
    private Float totalcount5stars;
    private Float totalNumberOfRatings;
    private Float sumOfTotalRatings;
    private String latest50Reviews;

    public SummaryModel(String activityId, String activityType, Float totalcount1stars, Float totalcount2stars, Float totalcount3stars, Float totalcount4stars, Float totalcount5stars, Float totalNumberOfRatings, Float sumOfTotalRatings, String latest50Reviews) {
        this.activityId = activityId;
        this.activityType = activityType;
        this.totalcount1stars = totalcount1stars;
        this.totalcount2stars = totalcount2stars;
        this.totalcount3stars = totalcount3stars;
        this.totalcount4stars = totalcount4stars;
        this.totalcount5stars = totalcount5stars;
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

    public Float getTotalcount3stars() {
        return totalcount3stars;
    }

    public void setTotalcount3stars(Float totalcount3stars) {
        this.totalcount3stars = totalcount3stars;
    }

    public Float getTotalcount1stars() {
        return totalcount1stars;
    }

    public void setTotalcount1stars(Float totalcount1stars) {
        this.totalcount1stars = totalcount1stars;
    }

    public Float getTotalcount4stars() {
        return totalcount4stars;
    }

    public void setTotalcount4stars(Float totalcount4stars) {
        this.totalcount4stars = totalcount4stars;
    }

    public Float getTotalcount5stars() {
        return totalcount5stars;
    }

    public void setTotalcount5stars(Float totalcount5stars) {
        this.totalcount5stars = totalcount5stars;
    }

    public Float getTotalNumberOfRatings() {
        return totalNumberOfRatings;
    }

    public void setTotalNumberOfRatings(Float totalNumberOfRatings) {
        this.totalNumberOfRatings = totalNumberOfRatings;
    }

    public Float getTotalcount2stars() {
        return totalcount2stars;
    }

    public void setTotalcount2stars(Float totalcount2stars) {
        this.totalcount2stars = totalcount2stars;
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

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
    private Float total_number_of_ratings;
    private Float sum_of_total_ratings;
    private String latest50Reviews;

    public SummaryModel(String activityId, String activityType, Float totalcount1stars, Float totalcount2stars, Float totalcount3stars, Float totalcount4stars, Float totalcount5stars, Float total_number_of_ratings, Float sum_of_total_ratings, String latest50Reviews) {
        this.activityId = activityId;
        this.activityType = activityType;
        this.totalcount1stars = totalcount1stars;
        this.totalcount2stars = totalcount2stars;
        this.totalcount3stars = totalcount3stars;
        this.totalcount4stars = totalcount4stars;
        this.totalcount5stars = totalcount5stars;
        this.total_number_of_ratings = total_number_of_ratings;
        this.sum_of_total_ratings = sum_of_total_ratings;
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

    public Float getTotal_number_of_ratings() {
        return total_number_of_ratings;
    }

    public void setTotal_number_of_ratings(Float total_number_of_ratings) {
        this.total_number_of_ratings = total_number_of_ratings;
    }

    public Float getTotalcount2stars() {
        return totalcount2stars;
    }

    public void setTotalcount2stars(Float totalcount2stars) {
        this.totalcount2stars = totalcount2stars;
    }

    public Float getSum_of_total_ratings() {
        return sum_of_total_ratings;
    }

    public void setSum_of_total_ratings(Float sum_of_total_ratings) {
        this.sum_of_total_ratings = sum_of_total_ratings;
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
        private String user_id;
        private Timestamp date;
        private Float rating;
        private String review;
        private String firstName;
        private String lastName;

        public latestReviews(String objectType, String user_id, Timestamp date, Float rating, String review, String firstName, String lastName) {
            this.objectType = objectType;
            this.user_id = user_id;
            this.date = date;
            this.rating = rating;
            this.review = review;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getObjectType() {
            return objectType;
        }

        public void setObjectType(String objectType) {
            this.objectType = objectType;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
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

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
}

package org.sunbird.ratings.service;

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.ratings.model.RequestRating;

public interface RatingService {
    public SBApiResponse getRatings(String activity_Id, String activity_Type, String userId) throws Exception; //To do

    public SBApiResponse getUsers(String activity_Id, String activity_Type, String userId) throws Exception;//To do

    public SBApiResponse upsertRating(String activity_Id, String activity_Type, String userId) throws Exception; //To do

    public SBApiResponse getRatingSummary(String activity_Id, String activity_Type) throws Exception; //To do

    public SBApiResponse upsert(RequestRating requestRating) throws Exception;

}

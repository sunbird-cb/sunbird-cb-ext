package org.sunbird.ratings.service;

import org.sunbird.common.model.Response;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.ratings.model.RequestRating;

public interface RatingService {
    public SBApiResponse addRating(RequestRating requestRating) throws Exception;
    public SBApiResponse getRatings(String activity_Id, String activity_Type, String userId) throws Exception; //To do
    public SBApiResponse getUsers(String activity_Id, String activity_Type, String userId) throws Exception;//To do
    public SBApiResponse upsertRating(String activity_Id, String activity_Type, String userId) throws Exception; //To do
    public SBApiResponse getRatingSummary(String activity_Id, String activity_Type) throws Exception; //To do

}

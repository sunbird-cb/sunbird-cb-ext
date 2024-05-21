package org.sunbird.ratings.service;

import java.util.Map;

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.ratings.model.LookupRequest;
import org.sunbird.ratings.model.RequestRating;

public interface RatingService {
    public SBApiResponse getRatings(String activityId, String activityType, String userId);

    public SBApiResponse upsertRating(RequestRating requestRating);

    public SBApiResponse getRatingSummary(String activityId, String activityType);

    public SBApiResponse ratingLookUp(LookupRequest request);

    public SBApiResponse readRatings(Map<String, Object> request);

    public SBApiResponse updateRatingsMetaData();

    public SBApiResponse updateAdditionalTag(String tag);

    SBApiResponse getTopReviewsForUserByOrgID(String userOrgId);
}

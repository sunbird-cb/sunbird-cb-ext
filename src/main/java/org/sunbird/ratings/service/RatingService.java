package org.sunbird.ratings.service;

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.ratings.model.LookupRequest;
import org.sunbird.ratings.model.RequestRating;

public interface RatingService {
    public SBApiResponse getRatings(String activity_Id, String activity_Type, String userId);

    public SBApiResponse upsertRating(RequestRating requestRating);

    public SBApiResponse getRatingSummary(String activity_Id, String activity_Type);

    public SBApiResponse search(LookupRequest request);
}

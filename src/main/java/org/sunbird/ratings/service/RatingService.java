package org.sunbird.ratings.service;

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.ratings.model.LookupRequest;
import org.sunbird.ratings.model.RequestRating;

public interface RatingService {
    public SBApiResponse getRatings(String activity_id, String activity_type, String user_id);

    public SBApiResponse upsertRating(RequestRating requestRating);

    public SBApiResponse getRatingSummary(String activity_id, String activity_type);

    public SBApiResponse ratingLookUp(LookupRequest request);
}

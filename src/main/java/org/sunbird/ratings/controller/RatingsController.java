package org.sunbird.ratings.controller;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.ratings.model.LookupRequest;
import org.sunbird.ratings.model.RequestRating;
import org.sunbird.ratings.service.RatingService;

@RestController
public class RatingsController {

    @Autowired
    RatingService ratingService;

    // ----------------- Public APIs --------------------

    @PostMapping("/ratings/v1/upsert")
    public ResponseEntity<?> upsertRating(@Valid @RequestBody RequestRating requestRatingBody) {
        SBApiResponse response = ratingService.upsertRating(requestRatingBody);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/ratings/v1/read/{activityId}/{activityType}/{userId}")
    public ResponseEntity<?> getRating(@PathVariable("activityId") String activityId,
                                       @PathVariable("activityType") String activityType,
                                       @PathVariable("userId") String userId) {
        SBApiResponse response = ratingService.getRatings(activityId, activityType, userId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/ratings/v1/summary/{activityId}/{activityType}")
    public ResponseEntity<?> getRatingSummary(@PathVariable("activityId") String activityId,
                                              @PathVariable("activityType") String activityType) {
        SBApiResponse response = ratingService.getRatingSummary(activityId, activityType);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/ratings/v1/ratingLookUp")
    public ResponseEntity<?> ratingLookUp(@RequestBody LookupRequest request) {
        SBApiResponse response = ratingService.ratingLookUp(request);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/ratings/v2/read")
    public ResponseEntity<?> readRating(@RequestBody Map<String, Object> request) {
        SBApiResponse response = ratingService.readRatings(request);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}


package org.sunbird.ratings.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.ratings.model.LookupRequest;
import org.sunbird.ratings.model.RequestRating;
import org.sunbird.ratings.service.RatingService;

import javax.validation.Valid;

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

    @GetMapping("/ratings/v1/read/{activity_Id}/{activity_Type}/{userId}")
    public ResponseEntity<?> getRating(@PathVariable("activity_Id") String activity_Id,
                                       @PathVariable("activity_Type") String activity_Type,
                                       @PathVariable("userId") String userId) {
        SBApiResponse response = ratingService.getRatings(activity_Id, activity_Type, userId);
        return new ResponseEntity<>(response, response.getResponseCode());

    }

    @GetMapping("/ratings/v1/summary/{activity_Id}/{activity_Type}")
    public ResponseEntity<?> getRatingSummary(@PathVariable("activity_Id") String activity_Id,
                                              @PathVariable("activity_Type") String activity_Type) {
        SBApiResponse response = ratingService.getRatingSummary(activity_Id, activity_Type);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/ratings/v1/search")
    public ResponseEntity<?> search(@RequestBody LookupRequest request) {
        SBApiResponse response = ratingService.search(request);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}


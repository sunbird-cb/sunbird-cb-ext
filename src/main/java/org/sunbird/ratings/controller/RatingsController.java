package org.sunbird.ratings.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
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

    @GetMapping("/ratings/v1/read/{activity_Type}/{activity_Id}/{userId}")
    public ResponseEntity<?> getRating(@PathVariable("activity_Id") String activity_Id,
                                       @PathVariable("activity_Type") String activity_Type,
                                       @PathVariable("userId") String userId) {
        SBApiResponse response = ratingService.getRatings(activity_Id, activity_Type, userId);
        return new ResponseEntity<>(response, response.getResponseCode());

    }
//    @GetMapping("/ratings/v1/search")
//    public ResponseEntity<List<String>> getUsersList() {
//        return new ResponseEntity<>(ratingService.getUsers(activity_Id, activity_Type, userId), HttpStatus.OK);
//    }


//    @PostMapping("/ratings/v1/summary")
//    public ResponseEntity<?> getRatingSummary(
//            @RequestParam( String activity_Id) throws Exception {
//         return new ResponseEntity<>(response, response.getResponseCode());
//    }
}


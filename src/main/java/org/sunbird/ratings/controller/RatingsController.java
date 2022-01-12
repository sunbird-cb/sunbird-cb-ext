package org.sunbird.ratings.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.ratings.model.RequestRating;
import org.sunbird.ratings.service.RatingService;
import javax.validation.Valid;

@RestController
public class RatingsController {

    @Autowired
    RatingService ratingService;

    // ----------------- Public APIs --------------------

    @PostMapping("/ratings/v1/add")
    public ResponseEntity<?> addRatings(@Valid @RequestBody RequestRating requestRatingBody) throws Exception {

        SBApiResponse response = ratingService.addRating(requestRatingBody);
        return new ResponseEntity<>(response, response.getResponseCode());

    }

//    @GetMapping("/ratings/v1/read")
//    public ResponseEntity<List<String>> getRatingsList() {
//        return new ResponseEntity<>(ratingService.getRatings(activity_Id, activity_Type, userId), HttpStatus.OK);
//    }
//    @GetMapping("/ratings/v1/search")
//    public ResponseEntity<List<String>> getUsersList() {
//        return new ResponseEntity<>(ratingService.getUsers(activity_Id, activity_Type, userId), HttpStatus.OK);
//    }

//    @PostMapping("/ratings/v1/update")
//    public ResponseEntity<?> upsertRatings(@Valid @RequestBody RequestRating requestRatingBody) throws Exception {
//         return new ResponseEntity<>(response, response.getResponseCode());
//    }
//    @PostMapping("/ratings/v1/summary")
//    public ResponseEntity<?> getRatingSummary(
//            @RequestParam( String activity_Id) throws Exception {
//         return new ResponseEntity<>(response, response.getResponseCode());
//    }
}


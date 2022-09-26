package org.sunbird.enrollment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.enrollment.service.EnrollmentService;
import org.sunbird.ratings.service.RatingService;

import javax.validation.Valid;
import java.util.Map;

@RestController
public class EnrollmentController {

    @Autowired
    EnrollmentService enrollmentService;


    @PostMapping("/enrollment/event")
    public ResponseEntity<?> generateEvent(@Valid @RequestBody Map<String, Object> requestBody) {
        SBApiResponse response = enrollmentService.generateEvent(requestBody);
        return new ResponseEntity<>(response, response.getResponseCode());

    }
}

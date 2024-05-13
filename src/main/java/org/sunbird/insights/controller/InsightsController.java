package org.sunbird.insights.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import static org.sunbird.common.util.Constants.*;
import org.sunbird.insights.controller.service.InsightsService;

import java.util.*;

@RestController
public class InsightsController {

    @Autowired
    private InsightsService insightsService;

    @PostMapping("/user/v2/insights")
    public ResponseEntity<?> insights(
            @RequestBody Map<String, Object> requestBody,@RequestHeader("x-authenticated-userid") String userId) throws Exception {
        SBApiResponse response = insightsService.insights(requestBody,userId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/microsite/read/insights")
    public ResponseEntity<SBApiResponse> readInsights(
            @RequestBody Map<String, Object> requestBody,@RequestHeader(X_AUTH_USER_ID) String userId) throws Exception {
        SBApiResponse response = insightsService.readInsightsForOrganisation(requestBody,userId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}

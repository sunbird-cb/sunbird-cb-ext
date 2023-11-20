package org.sunbird.insights.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.insights.controller.service.InsightsService;

import java.util.*;

@RestController
public class InsightsController {

    @Autowired
    private InsightsService insightsService;

    @PostMapping("/user/v2/insights")
    public ResponseEntity<?> insights(
            @RequestBody Map<String, Object> requestBody,@RequestHeader("wid") String userId) throws Exception {
        SBApiResponse response = insightsService.insights(requestBody,userId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}

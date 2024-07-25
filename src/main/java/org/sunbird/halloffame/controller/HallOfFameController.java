package org.sunbird.halloffame.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.halloffame.service.HallOfFameService;

import java.util.Map;

/**
 * @author mahesh.vakkund & Deepak kr Thakur
 */
@RestController
public class HallOfFameController {
    @Autowired
    private HallOfFameService hallOfFameService;

    @PostMapping("/v1/halloffame/read")
    public ResponseEntity<Map<String, Object>> fetchHallOfFameData() {
        Map<String, Object> hallOfFameDataMap = hallOfFameService.fetchHallOfFameData();
        return new ResponseEntity<>(hallOfFameDataMap, HttpStatus.OK);
    }

    @GetMapping("/v1/halloffame/learnerleaderboard")
    public ResponseEntity <SBApiResponse> learnerLeaderBoard
            (@RequestHeader(Constants.X_AUTH_TOKEN) String authToken,
             @RequestHeader(Constants.X_AUTH_USER_ORG_ID) String rootOrgId) throws Exception {
        SBApiResponse response = hallOfFameService.learnerLeaderBoard(rootOrgId, authToken);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/v1/top/learners/{ministryOrgId}")
    public ResponseEntity<SBApiResponse> fetchingTopLearners
            (@RequestHeader(Constants.X_AUTH_TOKEN) String authToken,
            @PathVariable String ministryOrgId) throws Exception {
        SBApiResponse response = hallOfFameService.fetchingTop10Learners(ministryOrgId, authToken);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}

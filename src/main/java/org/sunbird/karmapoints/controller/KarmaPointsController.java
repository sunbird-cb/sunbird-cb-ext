package org.sunbird.karmapoints.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.karmapoints.service.KarmaPointsService;

import java.util.Map;

@RestController
public class KarmaPointsController {
    @Autowired
    private KarmaPointsService karmaPointsService;

    @PostMapping("/v1/karmapoints/read")
    public ResponseEntity<Map<String, Object>> fetchKarmaPointsData(@RequestHeader("x-authenticated-user-orgid") String userOrgId) throws Exception {
        Map<String, Object> karmaPointsDataMap = karmaPointsService.fetchKarmaPointsData(userOrgId);
        return new ResponseEntity<>(karmaPointsDataMap, HttpStatus.OK);
    }
}
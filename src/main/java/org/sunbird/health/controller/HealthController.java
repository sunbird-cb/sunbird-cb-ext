package org.sunbird.health.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.health.service.HealthService;

@RestController
public class HealthController {
    private final  HealthService healthService;
    @Autowired
    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }
    @GetMapping("/health")
    public ResponseEntity<SBApiResponse> healthCheck() throws Exception {
        SBApiResponse response = healthService.checkHealthStatus();
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}

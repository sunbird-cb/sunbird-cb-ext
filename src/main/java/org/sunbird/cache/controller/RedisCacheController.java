package org.sunbird.cache.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.cache.service.RedisCacheService;
import org.sunbird.common.model.SBApiResponse;

@RestController
public class RedisCacheController {

    RedisCacheService redisCacheService;
    @Autowired

    public RedisCacheController(RedisCacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    @DeleteMapping("/redis")
    public ResponseEntity<SBApiResponse> deleteCache() {
        SBApiResponse response = redisCacheService.deleteCache();
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/redis")
    public ResponseEntity<SBApiResponse> getKeys() {
        SBApiResponse response = redisCacheService.getKeys();
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/redis/values")
    public ResponseEntity<SBApiResponse> getKeysAndValues() {
        SBApiResponse response = redisCacheService.getKeysAndValues();
        return new ResponseEntity<>(response, response.getResponseCode());
    }

}
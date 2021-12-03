package org.sunbird.cache.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.cache.service.RedisCacheService;
import org.sunbird.common.model.SBApiResponse;

@RestController
public class RedisCacheController {

    @Autowired
    RedisCacheService redisCacheService;


    @DeleteMapping("/redis")
    public ResponseEntity<?> deleteCache() throws Exception {
        SBApiResponse response = redisCacheService.deleteCache();
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/redis")
    public ResponseEntity<?> getKeys() throws Exception {
        SBApiResponse response = redisCacheService.getKeys();
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/redis/values")
    public ResponseEntity<?> getKeysAndValues() throws Exception {
        SBApiResponse response = redisCacheService.getKeysAndValues();
        return new ResponseEntity<>(response, response.getResponseCode());
    }

}
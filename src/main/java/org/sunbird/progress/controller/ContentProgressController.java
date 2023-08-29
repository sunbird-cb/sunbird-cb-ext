package org.sunbird.progress.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.util.Constants;
import org.sunbird.progress.service.ContentProgressService;

@RestController
@RequestMapping("/content/progress")
public class ContentProgressController {

    @Autowired
    private ContentProgressService service;

    @PostMapping("/v1/ext/update")
    public ResponseEntity<?> updateContentProgress(@RequestBody SunbirdApiRequest requestBody,
                                                   @RequestHeader(Constants.USER_TOKEN) String authUserToken) {

        SBApiResponse response = service.updateContentProgress(authUserToken, requestBody);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}

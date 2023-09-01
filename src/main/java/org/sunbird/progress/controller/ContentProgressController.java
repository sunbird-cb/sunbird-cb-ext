package org.sunbird.progress.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.util.Constants;
import org.sunbird.progress.service.ContentProgressService;

import java.io.IOException;

/**
 * This controller is responsible for handling the request wrt content progress.
 */
@RestController
@RequestMapping("/content/progress")
public class ContentProgressController {

    @Autowired
    private ContentProgressService service;

    /**
     * @param requestBody   -Request body of the API which needs to be processed.
     * @param authUserToken - It's authorization token received in request header.
     * @return - Return the response of success/failure after processing the request.
     */
    @PostMapping("/v1/ext/update")
    public ResponseEntity<?> updateContentProgress(@RequestBody SunbirdApiRequest requestBody,
                                                   @RequestHeader(Constants.USER_TOKEN) String authUserToken) {

        SBApiResponse response = service.updateContentProgress(authUserToken, requestBody);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    /**
     * @param requestBody   -Request body of the API which needs to be processed.
     * @param authUserToken - It's authorization token received in request header.
     * @return - Return the response of success/failure after processing the request.
     */
    @GetMapping("/v1/read/getUserDetails")
    public ResponseEntity<?> getUserSessionDetailsAndCourseProgress(@RequestBody SunbirdApiRequest requestBody,
                                                                         @RequestHeader(Constants.USER_TOKEN) String authUserToken) throws IOException {

        SBApiResponse response = service.getUserSessionDetailsAndCourseProgress(authUserToken, requestBody);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}

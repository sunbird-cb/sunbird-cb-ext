package org.sunbird.profile.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.codehaus.jettison.json.JSONString;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.profile.service.ProfileService;

import java.io.IOException;

@RestController
@RequestMapping("/v1/workflow")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @PostMapping("/transition")
    public ResponseEntity<?>  profileUpdate(@RequestBody String request) throws Exception {
        SBApiResponse response = profileService.profileUpdate(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

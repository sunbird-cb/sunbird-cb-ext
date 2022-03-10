package org.sunbird.profile.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.profile.service.ProfileService;
import java.util.Map;

@RestController
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @PostMapping("/user/patch")
    public ResponseEntity<?>  profileUpdate(@RequestHeader("x-authenticated-userid") String XAuthToken,
                                            @RequestHeader("Authorization") String AuthToken,
                                            @RequestBody Map<String,Object> request) throws Exception {
        SBApiResponse response = profileService.profileUpdate(request,XAuthToken,AuthToken);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

}

package org.sunbird.profile.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.profile.service.ProfileService;

@RestController
@RequestMapping("/v1/workflow")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @PostMapping("/transition")
    public ResponseEntity<?>  wfTransition(@RequestBody String wfRequest) throws JsonProcessingException {
        SBApiResponse response = profileService.workflowTransition(wfRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

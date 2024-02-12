package org.sunbird.digilocker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.digilocker.model.PullDocRequest;
import org.sunbird.digilocker.model.PullDocResponse;
import org.sunbird.digilocker.model.PullURIRequest;
import org.sunbird.digilocker.model.PullURIResponse;
import org.sunbird.digilocker.service.DigiLockerIntegrationService;

@RestController
@RequestMapping("digilocker")
public class DigiLockerIntegrationController {
    @Autowired
    DigiLockerIntegrationService digiLockerIntegrationService;

    @PostMapping(value = "/v1/retrieveURI", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody ResponseEntity<PullURIResponse> getURIRequestV1(@RequestBody PullURIRequest request) {

        PullURIResponse response = digiLockerIntegrationService.generateURIResponse(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping(value = "/v1/retrieveDoc", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody ResponseEntity<PullDocResponse> getDocRequestV1(@RequestBody PullDocRequest request) {
        PullDocResponse response = digiLockerIntegrationService.generateDocResponse(request);
        return ResponseEntity.ok().body(response);
    }
}

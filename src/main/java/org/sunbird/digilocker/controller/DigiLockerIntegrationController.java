package org.sunbird.digilocker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.util.Constants;
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
    public @ResponseBody ResponseEntity<PullURIResponse> getURIRequestV1(@RequestHeader(value= Constants.X_DIGILOCKER_HMAC) String digiLockerHmac, @RequestBody String requestBody) {

        PullURIResponse response = digiLockerIntegrationService.generateURIResponse(digiLockerHmac, requestBody);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping(value = "/v1/retrieveDoc", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody ResponseEntity<PullDocResponse> getDocRequestV1(@RequestHeader(value= Constants.X_DIGILOCKER_HMAC) String digiLockerHmac, @RequestBody String requestBody) {
        PullDocResponse response = digiLockerIntegrationService.generateDocResponse(digiLockerHmac, requestBody);
        return ResponseEntity.ok().body(response);
    }
}

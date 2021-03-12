package org.sunbird.workallocation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.Response;
import org.sunbird.workallocation.model.SearchCriteria;
import org.sunbird.workallocation.service.AllocationService;

@RestController
@RequestMapping("/v1/workallocation")
public class AllocationController {

    @Autowired
    private AllocationService allocationService;

    @PostMapping("/getUsers")
    public ResponseEntity<Response> getUsers(@RequestBody SearchCriteria searchCriteria) {
        Response response = allocationService.getUsers(searchCriteria);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

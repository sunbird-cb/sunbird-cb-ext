package org.sunbird.halloffame.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.halloffame.service.HallOfFameService;

import java.util.Map;

/**
 * @author mahesh.vakkund
 */
@RestController
public class HallOfFameController {
    @Autowired
    private HallOfFameService hallOfFameService;

    @PostMapping("/v1/halloffame/read")
    public ResponseEntity<Map<String, Object>> fetchHallOfFameData() {
        Map<String, Object> hallOfFameDataMap = hallOfFameService.fetchHallOfFameData();
        return new ResponseEntity<>(hallOfFameDataMap, HttpStatus.OK);
    }
}

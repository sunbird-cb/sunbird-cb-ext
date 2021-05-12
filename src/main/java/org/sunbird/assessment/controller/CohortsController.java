package org.sunbird.assessment.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.assessment.repo.CohortUsers;
import org.sunbird.assessment.service.CohortsService;

@RestController
public class CohortsController {

	@Autowired
	CohortsService cohortsServ;

	/**
	 * gets all top-performers
	 * 
	 * @param resourceId
	 * @param userEmail
	 * @param count
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/v2/resources/{resourceId}/user/{userUUID}/cohorts/top-performers")
	public ResponseEntity<List<CohortUsers>> getTopPerformers(@PathVariable("resourceId") String resourceId,
			@RequestHeader("rootOrg") String rootOrg, @PathVariable("userUUID") String userUUID,
			@RequestParam(value = "count", defaultValue = "20", required = false) Integer count) throws Exception {

		return new ResponseEntity<List<CohortUsers>>(cohortsServ.getTopPerformers(rootOrg, resourceId, userUUID, count),
				HttpStatus.OK);
	}
	
	/**
	 * gets all active users
	 * 
	 * @param resourceId
	 * @param userEmail
	 * @param count
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/v2/resources/{resourceId}/user/{userUUID}/cohorts/activeusers")
	public ResponseEntity<List<CohortUsers>> getActiveUsers(@PathVariable("resourceId") String contentId,
			@RequestHeader("rootOrg") String rootOrg, @PathVariable("userUUID") String userUUID,
			@RequestParam(value = "count", required = false, defaultValue = "50") Integer count,
			@RequestParam(value = "filter",required = false,defaultValue = "false")Boolean toFilter) throws Exception {
			return new ResponseEntity<List<CohortUsers>>(cohortsServ.getActiveUsers(rootOrg, contentId, userUUID, count, toFilter),
					HttpStatus.OK);
		
	}
}

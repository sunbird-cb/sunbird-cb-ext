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
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;


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
															  @RequestParam(value = "count", defaultValue = "20", required = false) Integer count) {

		return new ResponseEntity<>(cohortsServ.getTopPerformers(rootOrg, resourceId, userUUID, count),
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
	public ResponseEntity<List<CohortUsers>> getActiveUsers(@RequestHeader("Authorization") String authUserToken,
			@RequestHeader(name = Constants.X_AUTH_USER_ORG_ID, required = false) String rootOrgId,
			@PathVariable("resourceId") String contentId, @RequestHeader("rootOrg") String rootOrg,
			@PathVariable("userUUID") String userUUID,
			@RequestParam(value = "count", required = false, defaultValue = "50") Integer count,
			@RequestParam(value = "filter", required = false, defaultValue = "false") Boolean toFilter) {
		if (authUserToken.contains(" ")) {
			authUserToken = authUserToken.split(" ")[1];
		}
		return new ResponseEntity<>(
				cohortsServ.getActiveUsers(authUserToken, rootOrgId, rootOrg, contentId, userUUID, count, toFilter),
				HttpStatus.OK);
	}

	/**
	 *
	 * @param authUserToken
	 * @param contentId
	 * @param rootOrg
	 * @param userUUID
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/v1/autoenrollment/{userUUID}/{courseId}")
	public ResponseEntity<SBApiResponse> autoEnrollmentInCourse(@RequestHeader("Authorization") String authUserToken,
																@RequestHeader(name = Constants.X_AUTH_USER_ORG_ID, required = false) String rootOrgId,
																@PathVariable("courseId") String contentId,
																@RequestHeader("rootOrg") String rootOrg,
																@PathVariable("userUUID") String userUUID) {
		if (authUserToken.contains(" ")) {
			authUserToken = authUserToken.split(" ")[1];
		}
		SBApiResponse response = cohortsServ.autoEnrollmentInCourseV2(authUserToken, rootOrgId, rootOrg, contentId, userUUID);
		return new ResponseEntity<>(response, response.getResponseCode());
	}


//	====================================
//	KONG API ROUTES CHANGES
	/**
	 *
	 * @param resourceId
	 * @param rootOrg
	 * @param userUUID
	 * @param count
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/v2/resources/user/cohorts/top-performers")
	public ResponseEntity<List<CohortUsers>> getTopPerformersForResource(@RequestHeader("resourceId") String resourceId,
															  @RequestHeader("rootOrg") String rootOrg, @RequestHeader("userUUID") String userUUID,
															  @RequestParam(value = "count", defaultValue = "20", required = false) Integer count) {

		return new ResponseEntity<>(cohortsServ.getTopPerformers(rootOrg, resourceId, userUUID, count),
				HttpStatus.OK);
	}

	/**
	 * gets all active users
	 *
	 * @param authUserToken
	 * @param contentId
	 * @param rootOrg
	 * @param userUUID
	 * @param count
	 * @param toFilter
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/v2/resources/user/cohorts/activeusers")
	public ResponseEntity<List<CohortUsers>> getActiveUsersForResource(@RequestHeader("x-authenticated-user-token") String authUserToken,
															@RequestHeader(name = Constants.X_AUTH_USER_ORG_ID, required = false) String rootOrgId,
															@RequestHeader("resourceId") String contentId, @RequestHeader("rootOrg") String rootOrg,
															@RequestHeader("userUUID") String userUUID,
															@RequestParam(value = "count", required = false, defaultValue = "50") Integer count,
															@RequestParam(value = "filter", required = false, defaultValue = "false") Boolean toFilter) {
		return new ResponseEntity<>(
				cohortsServ.getActiveUsers(authUserToken, rootOrgId, rootOrg, contentId, userUUID, count, toFilter),
				HttpStatus.OK);
	}

	/**
	 *
	 * @param authUserToken
	 * @param contentId
	 * @param rootOrg
	 * @param userUUID
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/v1/autoenrollment")
	public ResponseEntity<SBApiResponse> userAutoEnrollment(@RequestHeader("x-authenticated-user-token") String authUserToken,
															@RequestHeader(name = Constants.X_AUTH_USER_ORG_ID, required = false) String rootOrgId,
															@RequestHeader("courseId") String contentId,
															@RequestHeader("rootOrg") String rootOrg,
															@RequestHeader("userUUID") String userUUID) {

		SBApiResponse response = cohortsServ.autoEnrollmentInCourseV2(authUserToken, rootOrgId, rootOrg, contentId, userUUID);
		return new ResponseEntity<>(response, response.getResponseCode());

	}
}

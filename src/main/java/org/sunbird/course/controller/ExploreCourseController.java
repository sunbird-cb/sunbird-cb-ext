package org.sunbird.course.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.course.service.ExploreCourseService;

/**
 * RESTController which provides API to handle course details which are exposed
 * to public users.
 * 
 * @author karthik
 *
 */
@RestController
public class ExploreCourseController {

	@Autowired
	ExploreCourseService courseService;

	@GetMapping("/course/v1/explore")
	public ResponseEntity<SBApiResponse> getPublicCourseList() {
		SBApiResponse response = courseService.getExploreCourseList();
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("course/v1/refreshCache")
	public ResponseEntity<SBApiResponse> refreshCourseListInCache() {
		SBApiResponse response = courseService.refreshCache();
		return new ResponseEntity<>(response, response.getResponseCode());
	}
}

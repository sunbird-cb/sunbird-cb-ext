package org.sunbird.course.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

	@GetMapping("/course/v2/explore")
	public ResponseEntity<SBApiResponse> getPublicCourseListV2() {
		SBApiResponse response = courseService.getExploreCourseListV2();
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/course/v1/explore/upsert")
	public ResponseEntity<SBApiResponse> upsertCourse(@RequestBody Map<String, Object> request) {
		SBApiResponse response = courseService.upsertExploreCourse(request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@DeleteMapping("/course/v1/explore/delete/{courseId}")
	public ResponseEntity<SBApiResponse> deleteExploreCourse(@PathVariable String courseId) {
		SBApiResponse response = courseService.deleteExploreCourse(courseId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}
}

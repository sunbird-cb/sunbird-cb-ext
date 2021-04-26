package org.sunbird.assessment.controller;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.assessment.dto.AssessmentSubmissionDTO;
import org.sunbird.assessment.service.AssessmentService;

@RestController
public class AssessmentController {

	@Autowired
	AssessmentService assessmentService;

	/**
	 * validates, submits and inserts assessments and quizzes into the db
	 * 
	 * @param requestBody
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/v2/user/{userId}/assessment/submit")
	public ResponseEntity<Map<String, Object>> submitAssessment(@Valid @RequestBody AssessmentSubmissionDTO requestBody,
			@PathVariable("userId") String userId, @RequestHeader("rootOrg") String rootOrg) throws Exception {

		return new ResponseEntity<Map<String, Object>>(assessmentService.submitAssessment(rootOrg, requestBody, userId),
				HttpStatus.CREATED);
	}

	/**
	 * Controller to a get request to Fetch AssessmentData the request requires
	 * user_id and course_id returns a JSON of processesd data and list of
	 * Assessments Given
	 * 
	 * @param courseId
	 * @param user_id
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/v2/content/{courseId}/user/{userId}/assessment")
	public ResponseEntity<Map<String, Object>> getAssessmentByContentUser(@PathVariable String courseId,
			@PathVariable("userId") String userId, @RequestHeader("rootOrg") String rootOrg) throws Exception {
		return new ResponseEntity<Map<String, Object>>(
				assessmentService.getAssessmentByContentUser(rootOrg, courseId, userId), HttpStatus.OK);
	}

}

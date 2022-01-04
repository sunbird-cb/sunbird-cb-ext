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

		return new ResponseEntity<>(assessmentService.submitAssessment(rootOrg, requestBody, userId),
				HttpStatus.CREATED);
	}

	/**
	 * Controller to a get request to Fetch AssessmentData the request requires
	 * user_id and course_id returns a JSON of processed data and list of
	 * Assessments Given
	 *
	 * @param courseId
	 * @param userId
	 * @param rootOrg
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/v2/content/{courseId}/user/{userId}/assessment")
	public ResponseEntity<Map<String, Object>> getAssessmentByContentUser(@PathVariable String courseId,
			@PathVariable("userId") String userId, @RequestHeader("rootOrg") String rootOrg) throws Exception {
		return new ResponseEntity<>(assessmentService.getAssessmentByContentUser(rootOrg, courseId, userId),
				HttpStatus.OK);
	}

	// =======================
	// KONG API Changes
	/**
	 * validates, submits and inserts assessments and quizzes into the db
	 *
	 * @param requestBody
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/v2/user/assessment/submit")
	public ResponseEntity<Map<String, Object>> submitUserAssessment(
			@Valid @RequestBody AssessmentSubmissionDTO requestBody, @RequestHeader("userId") String userId,
			@RequestHeader("rootOrg") String rootOrg) throws Exception {

		return new ResponseEntity<>(assessmentService.submitAssessment(rootOrg, requestBody, userId),
				HttpStatus.CREATED);
	}

	/**
	 * Controller to a get request to Fetch AssessmentData the request requires
	 * user_id and course_id returns a JSON of processed data and list of
	 * Assessments Given
	 *
	 * @param courseId
	 * @param userId
	 * @param rootOrg
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/v2/content/user/assessment")
	public ResponseEntity<Map<String, Object>> getUserAssessmentByContent(@RequestHeader("courseId") String courseId,
			@RequestHeader("userId") String userId, @RequestHeader("rootOrg") String rootOrg) throws Exception {
		return new ResponseEntity<>(assessmentService.getAssessmentByContentUser(rootOrg, courseId, userId),
				HttpStatus.OK);
	}

	/**
	 * To get the assessment question sets using the course and the assessment id
	 * 
	 * @param courseId
	 * @param assessmentContentId
	 * @param rootOrg
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/v2/{courseId}/assessment/{assessmentContentId}")
	public ResponseEntity<Map<String, Object>> getAssessmentContent(@PathVariable("courseId") String courseId,
			@PathVariable("assessmentContentId") String assessmentContentId, @RequestHeader("rootOrg") String rootOrg) {
		return new ResponseEntity<>(assessmentService.getAssessmentContent(courseId, assessmentContentId),
				HttpStatus.OK);
	}
}

package org.sunbird.budget.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.budget.model.BudgetInfo;
import org.sunbird.budget.service.BudgetService;
import org.sunbird.common.model.Response;

@RestController
@RequestMapping("/budget/scheme")
public class BudgetController {

	@Autowired
	BudgetService budgetService;

	@PostMapping
	public ResponseEntity<Response> createBudgetDetails(@RequestHeader("userId") String userId,
			@Valid @RequestBody BudgetInfo requestBody) throws Exception {
		return new ResponseEntity<>(budgetService.submitBudgetDetails(requestBody, userId), HttpStatus.CREATED);
	}

	@GetMapping("/{orgId}")
	public ResponseEntity<Response> getStaffDetails(@PathVariable("orgId") String orgId) throws Exception {
		return new ResponseEntity<Response>(budgetService.getBudgetDetails(orgId), HttpStatus.OK);
	}

	@PatchMapping
	public ResponseEntity<Response> updateBudgetDetails(@RequestHeader("userId") String userId,
			@Valid @RequestBody BudgetInfo requestBody) throws Exception {
		return new ResponseEntity<>(budgetService.updateBudgetDetails(requestBody, userId), HttpStatus.OK);
	}

	@DeleteMapping
	public ResponseEntity<Response> deleteBudgetDetails(@RequestParam String orgId,
			@RequestParam(name = "id", required = true) String budgetDetailsId, @RequestParam String budgetYear)
			throws Exception {
		return new ResponseEntity<>(budgetService.deleteBudgetDetails(orgId, budgetDetailsId, budgetYear),
				HttpStatus.OK);
	}

}


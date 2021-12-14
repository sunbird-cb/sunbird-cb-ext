package org.sunbird.budget.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.budget.model.BudgetDocInfo;
import org.sunbird.budget.model.BudgetInfo;
import org.sunbird.budget.service.BudgetService;
import org.sunbird.common.model.SBApiResponse;

@RestController
public class BudgetController {

	@Autowired
	BudgetService budgetService;

	@PostMapping("/budget/scheme")
	public ResponseEntity<?> createBudgetDetails(@RequestHeader("x-authenticated-userid") String userId,
			@Valid @RequestBody BudgetInfo requestBody) throws Exception {
		SBApiResponse response = budgetService.submitBudgetDetails(requestBody, userId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/budget/scheme/proof")
	public ResponseEntity<?> createBudgeProofDetails(@RequestHeader("x-authenticated-userid") String userId,
			@Valid @RequestBody BudgetDocInfo requestBody) throws Exception {
		SBApiResponse response = budgetService.submitBudgetDocDetails(requestBody, userId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("budget/scheme/{orgId}/{budgetYear}")
	public ResponseEntity<?> getBudgetDetails(@PathVariable("orgId") String orgId,
			@PathVariable("budgetYear") String budgetYear) throws Exception {
		SBApiResponse response = budgetService.getBudgetDetails(orgId, budgetYear);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PatchMapping("/budget/scheme")
	public ResponseEntity<?> updateBudgetDetails(@RequestHeader("x-authenticated-userid") String userId,
			@Valid @RequestBody BudgetInfo requestBody) throws Exception {
		SBApiResponse response = budgetService.updateBudgetDetails(requestBody, userId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@DeleteMapping("/budget/scheme")
	public ResponseEntity<?> deleteBudgetDetails(@RequestParam(name = "orgId", required = true) String orgId,
			@RequestParam(name = "id", required = true) String budgetDetailsId,
			@RequestParam(name = "budgetYear", required = false) String budgetYear) throws Exception {
		SBApiResponse response = budgetService.deleteBudgetDetails(orgId, budgetDetailsId, budgetYear);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@DeleteMapping("/budget/scheme/proof")
	public ResponseEntity<?> deleteBudgetDocDetails(@RequestParam(name = "orgId", required = true) String orgId,
			@RequestParam(name = "id", required = true) String budgetDetailsId,
			@RequestParam(name = "budgetYear", required = true) String budgetYear,
			@RequestParam(name = "proofDocId", required = true) String proofDocId) throws Exception {
		SBApiResponse response = budgetService.deleteDocBudgetDetails(orgId, budgetDetailsId, budgetYear, proofDocId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@GetMapping("/orghistory/{orgId}/budget")
	public ResponseEntity<?> getBudgetHistoryDetails(@PathVariable("orgId") String orgId) throws Exception {
		SBApiResponse response = budgetService.getBudgetAudit(orgId);
		return new ResponseEntity<>(response, response.getResponseCode());
	}
}

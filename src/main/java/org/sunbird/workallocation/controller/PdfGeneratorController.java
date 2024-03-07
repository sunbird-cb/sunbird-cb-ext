package org.sunbird.workallocation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.workallocation.model.PdfGeneratorRequest;
import org.sunbird.workallocation.service.PdfGeneratorService;

import java.io.IOException;

@RestController
public class PdfGeneratorController {

    private  final PdfGeneratorService pdfGeneratorService;
	@Autowired
	public PdfGeneratorController(PdfGeneratorService pdfGeneratorService) {
		this.pdfGeneratorService = pdfGeneratorService;
	}

	@PostMapping("/generatePdf")
    public ResponseEntity<byte[]> generatePdf(@RequestBody PdfGeneratorRequest request) throws Exception {
        return new ResponseEntity<>(pdfGeneratorService.generatePdf(request), HttpStatus.OK);
    }
    
    @GetMapping(value = "/getWOPdf/{woId}", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<?> getWAPdf(@PathVariable("woId") String woId)
			throws Exception {
		byte[] out = null;
		try {
			out = pdfGeneratorService.generatePdf(woId);
		} catch (Exception e) {
		}

		if (out == null) {
			throw new InternalError("Failed to generate PDF file.");
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.add("Content-Disposition", "inline; filename=wo_report.pdf");

		ResponseEntity<?> response = new ResponseEntity<>(out, headers, HttpStatus.OK);
		return response;
	}

	@GetMapping(value = "/getWOPublishedPdf/{woId}")
	public ResponseEntity<String> getWOPublishedPdf(@PathVariable("woId") String woId) {
		return new ResponseEntity<>(pdfGeneratorService.getPublishedPdfLink(woId), HttpStatus.OK);
	}

	@GetMapping(value = "/getBatchSessionQRPdf/{courseId}/{batchId}", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<?> getBatchSessionQRPdf(@RequestHeader("x-authenticated-user-token") String authUserToken, @PathVariable("courseId") String courseId, @PathVariable("batchId") String batchId) throws IOException {
		return new ResponseEntity<>(pdfGeneratorService.getBatchSessionQRPdf(authUserToken,courseId,batchId), HttpStatus.OK);
	}

}

package org.sunbird.workallocation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.workallocation.model.PdfGeneratorRequest;
import org.sunbird.workallocation.service.PdfGeneratorService;

import java.io.IOException;

@RestController
public class PdfGeneratorController {
    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @PostMapping("/generatePdf")
    public ResponseEntity<byte[]> generatePdf(@RequestBody PdfGeneratorRequest request) throws IOException {
        return new ResponseEntity<>(pdfGeneratorService.generatePdf(request), HttpStatus.OK);
    }
}

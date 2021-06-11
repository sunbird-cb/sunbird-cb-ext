package org.sunbird.workallocation.service;

import org.sunbird.workallocation.model.PdfGeneratorRequest;

import java.io.IOException;

public interface PdfGeneratorService {
    public byte[] generatePdf(PdfGeneratorRequest request) throws IOException;
    public byte[] generatePdf(String woId) throws Exception;
}

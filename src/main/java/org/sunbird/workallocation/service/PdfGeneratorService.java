package org.sunbird.workallocation.service;

import org.sunbird.workallocation.model.PdfGeneratorRequest;

public interface PdfGeneratorService {
    public byte[] generatePdf(PdfGeneratorRequest request) throws Exception;
    public byte[] generatePdf(String woId) throws Exception;
    public String getPublishedPdfLink(String woId);
    public String generatePdfAndGetFilePath(String woId);
}

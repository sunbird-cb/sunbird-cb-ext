package org.sunbird.workallocation.service;

import java.io.IOException;

import org.sunbird.workallocation.model.PdfGeneratorRequest;

public interface PdfGeneratorService {
	public byte[] generatePdf(PdfGeneratorRequest request) throws IOException;

	public byte[] generatePdf(String woId);

	public String getPublishedPdfLink(String woId);

	public String generatePdfAndGetFilePath(String woId);
}

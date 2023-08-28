package org.sunbird.storage.service;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.common.model.SBApiResponse;

public interface StorageService {
	public SBApiResponse uploadFile(MultipartFile file, String containerName) throws IOException;

	SBApiResponse uploadFile(File file, String containerName);

	public SBApiResponse deleteFile(String fileName, String containerName);

    SBApiResponse downloadFile(String fileName);

	ResponseEntity<Resource> downloadFile(String reportType, String date, String orgId, String fileName);
}

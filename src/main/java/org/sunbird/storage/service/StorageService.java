package org.sunbird.storage.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;
import org.sunbird.common.model.SBApiResponse;

public interface StorageService {
	public SBApiResponse deleteFile(String fileName);

	public SBApiResponse uploadFile(MultipartFile file) throws IOException;
}

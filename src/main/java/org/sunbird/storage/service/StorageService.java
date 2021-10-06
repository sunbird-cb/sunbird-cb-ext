package org.sunbird.storage.service;

import org.springframework.web.multipart.MultipartFile;
import org.sunbird.common.model.SBApiResponse;

import java.io.IOException;

public interface StorageService {

    public SBApiResponse uploadFile(String folderName, MultipartFile file) throws IOException;

    public SBApiResponse deleteFile(String fileName);

}

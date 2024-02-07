package org.sunbird.digilocker.service;

import org.sunbird.digilocker.model.PullDocRequest;
import org.sunbird.digilocker.model.PullDocResponse;
import org.sunbird.digilocker.model.PullURIRequest;
import org.sunbird.digilocker.model.PullURIResponse;

public interface DigiLockerIntegrationService {

    public PullURIResponse generateURIResponse(PullURIRequest request);
    public PullDocResponse generateDocResponse(PullDocRequest request);
}

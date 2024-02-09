package org.sunbird.digilocker.service;

import org.sunbird.digilocker.model.PullDocRequest;
import org.sunbird.digilocker.model.PullDocResponse;
import org.sunbird.digilocker.model.PullURIRequest;
import org.sunbird.digilocker.model.PullURIResponse;

public interface DigiLockerIntegrationService {

    PullURIResponse generateURIResponse(PullURIRequest request);

    PullDocResponse generateDocResponse(PullDocRequest request);
}

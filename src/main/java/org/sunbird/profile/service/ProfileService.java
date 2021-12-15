package org.sunbird.profile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.sunbird.common.model.SBApiResponse;

public interface ProfileService {
    SBApiResponse workflowTransition(String wfRequest) throws JsonProcessingException;
}

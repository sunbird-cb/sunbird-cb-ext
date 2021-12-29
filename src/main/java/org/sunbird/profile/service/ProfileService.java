package org.sunbird.profile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.codehaus.jettison.json.JSONString;
import org.json.JSONObject;
import org.sunbird.common.model.SBApiResponse;

import java.io.IOException;

public interface ProfileService {
    SBApiResponse profileUpdate(String request) throws Exception;
}

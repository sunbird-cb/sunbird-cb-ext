package org.sunbird.profile.service;

import org.sunbird.common.model.SBApiResponse;
import java.util.Map;

public interface ProfileService {
    SBApiResponse profileUpdate(Map<String,Object> request, String XAuthToken, String AuthToken) throws Exception;
}

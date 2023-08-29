package org.sunbird.progress.service;

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;

public interface ContentProgressService {

    public SBApiResponse updateContentProgress(String authUserToken, SunbirdApiRequest requestBody);
}

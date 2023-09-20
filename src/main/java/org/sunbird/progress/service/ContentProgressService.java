package org.sunbird.progress.service;

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;

import java.io.IOException;

public interface ContentProgressService {

    public SBApiResponse updateContentProgress(String authUserToken, SunbirdApiRequest requestBody);

    /**
     * @param requestBody   -Request body of the API which needs to be processed.
     * @param authUserToken - It's authorization token received in request header.
     * @return - Return the response of success/failure after processing the request.
     */
    SBApiResponse getUserSessionDetailsAndCourseProgress(String authUserToken, SunbirdApiRequest requestBody);
}

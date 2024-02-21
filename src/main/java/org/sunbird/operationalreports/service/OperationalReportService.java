package org.sunbird.operationalreports.service;

/**
 * @author Deepak kumar Thakur
 */

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;

public interface OperationalReportService {
    public SBApiResponse grantReportAccessToMDOAdmin(SunbirdApiRequest request, String userOrgId, String authToken)throws Exception;

}
package org.sunbird.operationalreports.service;

/**
 * @author Deepak kumar Thakur
 */

import org.sunbird.common.model.SBApiResponse;

public interface OperationalReportService {
    public SBApiResponse grantAccessToMDOAdmin(String userOrgId, String authToken);

}
package org.sunbird.cbp.service;

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;

public interface CbPlanService {
    SBApiResponse createCbPlan(SunbirdApiRequest request, String userOrgId, String token);

    SBApiResponse updateCbPlan(SunbirdApiRequest request, String userOrgId, String token);

    SBApiResponse publishCbPlan(SunbirdApiRequest request, String userOrgId, String token);

    SBApiResponse retireCbPlan(SunbirdApiRequest request, String userOrgId, String token);

    SBApiResponse readCbPlan(String cbPlanId, String userOrgId, String token);

    SBApiResponse listCbPlan(SunbirdApiRequest request, String userOrgId, String token);
}

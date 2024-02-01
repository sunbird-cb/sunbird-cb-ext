package org.sunbird.cbp.service;

import org.sunbird.cbp.model.dto.CbPlanDto;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;

import java.util.List;

public interface CbPlanService {
    SBApiResponse createCbPlan(SunbirdApiRequest request, String userOrgId, String token, boolean isMigrate);

    SBApiResponse updateCbPlan(SunbirdApiRequest request, String userOrgId, String token, List<String> userRoles);

    SBApiResponse publishCbPlan(SunbirdApiRequest request, String userOrgId, String token, List<String> userRoles, boolean isMigrate);

    SBApiResponse retireCbPlan(SunbirdApiRequest request, String userOrgId, String token, List<String> userRoles);

    SBApiResponse readCbPlan(String cbPlanId, String userOrgId, String token);

    SBApiResponse listCbPlan(SunbirdApiRequest request, String userOrgId, String token);

    SBApiResponse getCBPlanListForUser(String userOrgId, String token);

     SBApiResponse getCBPlanListForUser(String userOrgId, String authUserToken,boolean isPrivate);

     SBApiResponse requestCbplanContent(SunbirdApiRequest request, String userOrgId, String token);
}

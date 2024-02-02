package org.sunbird.ehrms.service;

/**
 * @author Deepak kumar Thakur
 */

import org.sunbird.common.model.SBApiResponse;

public interface EhrmsService {
   public SBApiResponse fetchEhrmsProfileDetail(String rootOrgId, String authToken) ;

}
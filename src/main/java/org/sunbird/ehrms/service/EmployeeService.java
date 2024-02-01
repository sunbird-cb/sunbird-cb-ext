package org.sunbird.ehrms.service;

/**
 * @author Deepak kumar Thakur
 */

import org.sunbird.common.model.SBApiResponse;

public interface EmployeeService {
   public SBApiResponse fetchEmployeeDetail(String rootOrgId, String authToken) ;

}
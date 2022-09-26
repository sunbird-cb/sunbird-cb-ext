package org.sunbird.enrollment.service;

import org.sunbird.common.model.SBApiResponse;

import java.util.Map;

public interface EnrollmentService {

    public SBApiResponse generateEvent( Map<String,Object> request);

}

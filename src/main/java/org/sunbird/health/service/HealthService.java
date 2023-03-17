package org.sunbird.health.service;

import org.sunbird.common.model.SBApiResponse;

public interface HealthService {

    SBApiResponse checkHealthStatus() throws Exception;

}
